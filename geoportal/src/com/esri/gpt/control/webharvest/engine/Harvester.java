/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.catalog.harvest.jobs.HjResetRunningRequest;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.control.rest.writer.ResponseWriter;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.resource.api.SourceUri;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Harvesting engine.
 */
public class Harvester implements HarvesterMBean {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(Harvester.class.getCanonicalName());
  /** listeners */
  private HarvesterListenerArray listenerArray = new HarvesterListenerArray();
  /** message broker */
  private MessageBroker messageBroker;
  /** harvester configuration */
  private HarvesterConfiguration cfg;
  /** task queue */
  private TaskQueue taskQueue;
  /** pool of threads */
  private Pool pool;
  /** resource autoSelector */
  private AutoSelector autoSelector;
  /** watch-dog */
  private WatchDog watchDog;
  /** MBean name */
  private ObjectName name = null;

  /**
   * Creates instance of the engine.
   * @param messageBroker message broker
   * @param cfg harvest configuration
   */
  public Harvester(MessageBroker messageBroker, HarvesterConfiguration cfg) {
    if (messageBroker == null) {
      throw new IllegalArgumentException("No message broker provided.");
    }
    if (cfg == null) {
      throw new IllegalArgumentException("No configuration provided.");
    }
    this.messageBroker = messageBroker;
    this.cfg = cfg;

    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      name = new ObjectName("com.esri.gpt:type=Synchronizer");
      if (mbs.isRegistered(name)) {
        mbs.unregisterMBean(name);
      }
      mbs.registerMBean(this, name);
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Error creating managed bean for Synchronizer.", ex);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (name != null) {
      try {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (mbs.isRegistered(name)) {
          mbs.unregisterMBean(name);
        }
      } catch (Exception ex) {
        LOGGER.log(Level.SEVERE, "Error destroying managed bean for Synchronizer.", ex);
      }
    }
    super.finalize();
  }

  @Override
  public void startup() {
    if (!getRunning()) {
      init();
    } else {
      LOGGER.info("[SYNCHRONIZER] Synchronizer is running already.");
    }
  }

  /**
   * Initializes engine.
   */
  public void init() {
    if (cfg.getQueueEnabled()) {
      LOGGER.info("[SYNCHRONIZER] Initializing synchronizer queue.");
      this.taskQueue = new TaskQueue();
    }
    if (cfg.getActive()) {
      LOGGER.info("[SYNCHRONIZER] Initializing synchronizer engine.");
      this.taskQueue = new TaskQueue();
      this.pool = new Pool(
        new DataProcessorDispatcher(createDataProcessors(messageBroker, cfg.getBaseContextPath(), listenerArray)),
        taskQueue, cfg.getPoolSize());

      if (cfg.getAutoSelectFrequency() > 0) {
        this.autoSelector = new AutoSelector(cfg.getAutoSelectFrequency()) {

          @Override
          protected void onSelect(HrRecord resource) {
            if (ProtocolInvoker.getUpdateContent(resource.getProtocol())) {
              RequestContext context = RequestContext.extract(null);
              try {
                submit(context, resource, null, resource.getLastSyncDate()==null || HarvestPolicy.getInstance().getForceFullHarvest(resource)? null: resource.getLastSyncDate());
              } finally {
                context.onExecutionPhaseCompleted();
              }
            }
          }
        };
        Thread thread = new Thread(autoSelector, "Auto-selector");
        thread.start();
      }

      if (cfg.getWatchDogFrequency() > 0) {
        this.watchDog = new WatchDog(cfg.getWatchDogFrequency()) {

          @Override
          protected String[] getCurrentlyHarvesterResourceUuids() {
            ArrayList<String> uuids = new ArrayList<String>();
            for (ExecutionUnit u : pool.getAllExecutedUnits()) {
              uuids.add(u.getRepository().getUuid());
            }
            return uuids.toArray(new String[uuids.size()]);
          }

          @Override
          protected void cancelByResourceUuids(String[] uuids) {
            for (String uuid : uuids) {
              pool.drop(uuid);
            }
          }
        };
        Thread thread = new Thread(watchDog, "Watch-dog");
        thread.start();
      }

      // resetRunning from the last fail/shutdown
      resetRunning();
    }
  }

  @Override
  public boolean getRunning() {
    return pool != null && taskQueue != null;
  }

  /**
   * Shuts down harvesting engine.
   */
  @Override
  public void shutdown() {
    if (getRunning()) {
      LOGGER.info("[SYNCHRONIZER] Shutting down synchronizer.");
      if (autoSelector != null)
        autoSelector.shutdown();
      autoSelector = null;
      if (watchDog != null)
        watchDog.shutdown();
      watchDog = null;
      if (pool != null)
        pool.shutdown();
      pool = null;
      taskQueue = null;
    } else {
      LOGGER.info("[SYNCHRONIZER] Synchronizer shutted down already.");
    }
  }

  @Override
  public void setPoolSize(int size) {
    if (pool != null)
      pool.resize(size);
  }

  @Override
  public int getPoolSize() {
    return pool != null ? pool.size() : 0;
  }

  /**
   * Reselects harvesting sites.
   */
  public void reselect() {
    LOGGER.finer("[SYNCHRONIZER] Reselects resources.");
    if (autoSelector != null)
      autoSelector.reselect();
  }

  /**
   * Checks if is executing locally.
   * @param uuid resource UUID
   * @return <code>true</code> if is executing locally
   */
  public boolean isExecutingLocally(String uuid) {
    return pool != null ? pool.isExecuting(uuid) : false;
  }

  /**
   * Gets statistics for given repository.
   * @param uuid repository uuid.
   * @return statistics or <code>null</code> if statistics unavailable
   */
  public Statistics getStatistics(String uuid) {
    ExecutionUnit unit = pool != null ? pool.getExecutionUnitFor(uuid) : null;
    ExecutionUnitHelper helper = new ExecutionUnitHelper(unit);
    return helper.getReportBuilder();
  }

  /**
   * Submits new harvesting task for partial harvest.
   * @param context request context
   * @param resource resource to harvest
   * @param maxRecs maximum number of records to harvest (<code>null</code> for no maximum limit)
   * @param fromDate to harvest only from the specific date (<code>null</code> for no from date)
   * @return <code>true</code> if task has been submitted
   */
  public boolean submit(RequestContext context, HrRecord resource, Integer maxRecs, Date fromDate) {
    if (resource == null)
      throw new IllegalArgumentException("No resource to harvest provided.");
    // create instance of the task
    // add only if no similar task currently executing
    boolean submitted = false;
    if (ApprovalStatus.isPubliclyVisible(resource.getApprovalStatus().name()) && resource.getSynchronizable()) {
      CommonCriteria criteria = new CommonCriteria();
      criteria.setMaxRecords(maxRecs);
      if (fromDate != null) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fromDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        fromDate = cal.getTime();
      }
      criteria.setFromDate(fromDate);
      submitted = !isExecutingLocally(resource.getUuid()) && (taskQueue != null ? taskQueue.add(context, resource, criteria) : false);
      LOGGER.log(Level.FINER, "[SYNCHRONIZER] Submitted resource: {0} ({1})", new Object[]{resource.getUuid(), resource.getName()});
    }
    return submitted;
  }

  /**
   * Spans new, separate thread exclusively for the resource.
   * @param context request context
   * @param resource resource to harvest
   * @param maxRecs maximum number of records to harvest (<code>null</code> for no maximum limit)
   * @param fromDate to harvest only from the specific date (<code>null</code> for no from date)
   * @return <code>true</code> if task has been sumbited
   */
  public boolean span(RequestContext context, HrRecord resource, Integer maxRecs, Date fromDate) {
    if (resource == null)
      throw new IllegalArgumentException("No resource to harvest provided.");
    // create instance of the task
    // add only if no similar task currently executing
    boolean submitted = false;
    if (resource.getApprovalStatus() == ApprovalStatus.approved && resource.getSynchronizable()) {
      CommonCriteria criteria = new CommonCriteria();
      criteria.setMaxRecords(maxRecs);
      criteria.setFromDate(fromDate);
      submitted = pool != null && taskQueue != null ? !pool.isExecuting(resource.getUuid()) && taskQueue.register(context, resource, criteria) : false;
      if (submitted)
        pool.span(resource, criteria);
      LOGGER.log(Level.FINER, "[SYNCHRONIZER] Submitted resource: {0} ({1})", new Object[]{resource.getUuid(), resource.getName()});
    }
    return submitted;
  }

  /**
   * Cancels pending tasks for given repository.
   * @param context request context
   * @param uuid repository uuid
   * @return <code>true</code> if has been canceled properly
   */
  public boolean cancel(RequestContext context, String uuid) {
    LOGGER.log(Level.FINER, "[SYNCHRONIZER] Canceled resource: {0}", uuid);
    // drop resource already being harvested
    boolean dropped = pool != null? pool.drop(uuid): false;
    // withdraw from the queue
    boolean canceled = taskQueue != null? taskQueue.cancel(context, uuid): false;
    // exit with status
    return canceled || dropped;
  }

  /**
   * Registers listener.
   * @param listener listener
   */
  public void registerListener(Listener listener) {
    this.listenerArray.add(listener);
  }

  /**
   * Removes listener.
   * @param listener listener
   */
  public void unregisterListener(Listener listener) {
    this.listenerArray.remove(listener);
  }

  /**
   * Removes all listeners.
   */
  public void removeAllListeners() {
    this.listenerArray.clear();
  }

  /**
   * Recovers records.
   */
  private void resetRunning() {
    RequestContext context = RequestContext.extract(null);
    try {
      HjResetRunningRequest recover = new HjResetRunningRequest(context);
      recover.execute();
      if (taskQueue!=null) {
        taskQueue.notifyChange();
      }
    } catch (SQLException ex) {
      LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error recovering from the previous failout", ex);
    } finally {
      context.onExecutionPhaseCompleted();
    }
  }

  /**
   * Listener of events.
   */
  public static interface Listener {

    /**
     * Called on start of harvesting of the specific repository.
     * @param repository repository
     */
    void onHarvestStart(HrRecord repository);

    /**
     * Called on end of harvesting of the specific repository.
     * @param repository repository
     */
    void onHarvestEnd(HrRecord repository);

    /**
     * Called on harvest a single metadata from the repository.
     * @param repository repository
     * @param sourceUri metadata source URI
     * @param metadata metadata full text
     */
    void onHarvestMetadata(HrRecord repository, SourceUri sourceUri, String metadata);

    /**
     * Called on publish a single metadata from the repository.
     * @param repository repository
     * @param sourceUri metadata source URI
     * @param uuid metadata UUID
     * @param metadata metadata full text
     */
    void onPublishMetadata(HrRecord repository, SourceUri sourceUri, String uuid, String metadata);

    /**
     * Called on iteration exception.
     * @param repository repository
     * @param ex exception
     */
    void onIterationException(HrRecord repository, Exception ex);

    /**
     * Called on harvest exception.
     * @param repository repository
     * @param sourceUri metadata source URI
     * @param ex exception
     */
    void onHarvestException(HrRecord repository, SourceUri sourceUri, Exception ex);

    /**
     * Called on publish exception.
     * @param repository repository
     * @param sourceUri metadata source URI
     * @param metadata metadata full text
     * @param ex exception
     */
    void onPublishException(HrRecord repository, SourceUri sourceUri, String metadata, Exception ex);
  }

  /**
   * Array of listeners.
   */
  private class HarvesterListenerArray extends ArrayList<Harvester.Listener> implements Harvester.Listener {

    /**
     * Called on start of harvesting ofthe specific repository.
     * @param repository repository
     */
    @Override
    public void onHarvestStart(HrRecord repository) {
      for (Harvester.Listener l : this) {
        l.onHarvestStart(repository);
      }
    }

    /**
     * Called on end of harvesting ofthe specific repository.
     * @param repository repository
     */
    @Override
    public void onHarvestEnd(HrRecord repository) {
      for (Harvester.Listener l : this) {
        l.onHarvestEnd(repository);
      }
    }

    /**
     * Called on publish a single metadata from the repository.
     * @param repository repository
     * @param sourceUri metadata source URI
     * @param uuid metadata UUID
     * @param metadata metadata full text
     */
    @Override
    public void onHarvestMetadata(HrRecord repository, SourceUri sourceUri, String metadata) {
      for (Harvester.Listener l : this) {
        l.onHarvestMetadata(repository, sourceUri, metadata);
      }
    }

    /**
     * Called on publish a single metadata from the repository.
     * @param repository repository
     * @param sourceUri metadata source URI
     * @param uuid metadata UUID
     * @param metadata metadata full text
     */
    @Override
    public void onPublishMetadata(HrRecord repository, SourceUri sourceUri, String uuid, String metadata) {
      for (Harvester.Listener l : this) {
        l.onPublishMetadata(repository, sourceUri, uuid, metadata);
      }
    }

    /**
     * Called on iteration exception of a single metadata from the repository.
     * @param repository repository
     * @param sourceUri metadata source URI
     */
    @Override
    public void onIterationException(HrRecord repository, Exception ex) {
      for (Harvester.Listener l : this) {
        l.onIterationException(repository, ex);
      }
    }

    /**
     * Called on harvest exception of a single metadata from the repository.
     * @param repository repository
     * @param sourceUri metadata source URI
     */
    @Override
    public void onHarvestException(HrRecord repository, SourceUri sourceUri, Exception ex) {
      for (Harvester.Listener l : this) {
        l.onHarvestException(repository, sourceUri, ex);
      }
    }

    /**
     * Called on publish exception of a single metadata from the repository.
     * @param repository repository
     * @param sourceUri metadata source URI
     * @param metadata metadata full text
     */
    @Override
    public void onPublishException(HrRecord repository, SourceUri sourceUri, String metadata, Exception ex) {
      for (Harvester.Listener l : this) {
        l.onPublishException(repository, sourceUri, metadata, ex);
      }
    }
  }
  
  public void safeSuspend() {
    LOGGER.info("[SYNCHRONIZER] Suspending harvester");
    if (pool!=null) {
      pool.safeSuspend();
    }
    if (watchDog!=null) {
      watchDog.safeSuspend();
    }
    if (autoSelector!=null) {
      autoSelector.safeSuspend();
    }
  }
  
  public void safeResume() {
    LOGGER.info("[SYNCHRONIZER] Resuming harvester");
    if (autoSelector!=null) {
      autoSelector.safeResume();
    }
    if (watchDog!=null) {
      watchDog.safeResume();
    }
    if (pool!=null) {
      pool.safeResume();
    }
  }
  
  private List<DataProcessor> createDataProcessors(MessageBroker messageBroker, String baseContextPath, Harvester.Listener listener) {
    List<DataProcessor> processors = new ArrayList<DataProcessor>();
    for (DataProcessorFactory factory : cfg.getDataProcessorFactories()) {
      processors.add(factory.newProcessor(messageBroker, baseContextPath, listener));
    }
    return processors;
  }
  
  /**
   * Writes harvester engine statistics
   * @param writer the response writer
   * @param sb the response string builder
   * @throws Exception if exception occurs
   */
  public void writeStatistics(ResponseWriter writer,StringBuilder sb) throws Exception {
	  HarvesterStatisticsCollector hsc = new HarvesterStatisticsCollector(pool, watchDog, taskQueue, messageBroker);
	  hsc.writeStatistics(writer, sb);
  }
}
