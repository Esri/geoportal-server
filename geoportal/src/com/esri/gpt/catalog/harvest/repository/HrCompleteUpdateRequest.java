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
package com.esri.gpt.catalog.harvest.repository;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp2Agp;
import com.esri.gpt.catalog.publication.PublicationRequest;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.control.view.SelectablePublishers;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.util.UuidUtil;
import java.util.ArrayList;
import java.util.Date;

/**
 * Complete update request.
 */
public class HrCompleteUpdateRequest extends HrRequest {

  private HrRecord record;

  /**
   * Creates instance of the request.
   * @param context request context
   * @param record record
   */
  public HrCompleteUpdateRequest(RequestContext context, HrRecord record) {
    super(context, new HrCriteria(), new HrResult());
    this.record = record;
  }

  /**
   * Creates native resource based on the information from the repository.
   * @param repository repository
   * @return native resource or <code>null</code> if no native resource
   * @throws Exception if accessing resource fails
   */
  private Native createNativeResource(HrRecord repository) throws Exception {
    // declare placeholder for any exception thrown by query builder
    final ArrayList<Exception> exceptions = new ArrayList<Exception>();
    // create such an instance of the iteration context so it will store each
    // exception in the placeholder
    IterationContext iterationContext = new IterationContext() {

      public void onIterationException(Exception ex) {
        exceptions.add(ex);
      }
    };
    // create new query builder specific for the repository
    QueryBuilder queryBuilder = repository.newQueryBuilder(iterationContext);
    // get native resource; this may throw exception(s) stored later in the
    // placeholder
    Native nativeResource = queryBuilder.getNativeResource();
    // assure there are no exceptions; if there are any, throw the first one
    if (exceptions.size() > 0) {
      throw exceptions.get(0);
    }
    return nativeResource;
  }

  /**
   * Executes request.
   * @throws Exception if any error occurred
   */
  public boolean execute() throws Exception {
    RequestContext context = getRequestContext();

    SelectablePublishers selectablePublishers = new SelectablePublishers();
    selectablePublishers.build(context, isAdministrator(context));
    Publisher user = selectablePublishers.selectedAsPublisher(context, isAdministrator(context));

    RoleSet roleSet = new RoleSet();
    roleSet.add("gptAdministrator");
    roleSet.add("gptPublisher");
    user.getAuthenticationStatus().authorizeAction(roleSet);

    boolean hostUrlChanged = true;
    boolean titleChanged = true;
    boolean findableChanged = true;

    boolean creating = !UuidUtil.isUuid(record.getUuid());
    if (!creating) {
      HrSelectRequest select = new HrSelectRequest(context, record.getUuid());
      select.execute();
      HrRecords records = select.getQueryResult().getRecords();
      if (records.size() > 0) {
        HrRecord oldRecord = records.get(0);
        if (user.getLocalID()!=oldRecord.getOwnerId()) {
          if (!user.getIsAdministrator()) {
            throw new NotAuthorizedException("Not authorized.");
          }
        }
        if (record.getHostUrl().equals(oldRecord.getHostUrl())) {
          hostUrlChanged = false;
        }
        if (record.getName().equals(oldRecord.getName())) {
          titleChanged = false;
        }
        if (record.getFindable()==oldRecord.getFindable()) {
          findableChanged = false;
        }
      }
    }

    if (hostUrlChanged) {
      // if creaing new repository, make sure that no record with identical
      // host URL exist in the database
      HrAssertUrlRequest assertUrlRequest = new HrAssertUrlRequest(context, record.getHostUrl());
      assertUrlRequest.executeAssert();
    }

    // create native resource
    Native nativeResource = null;

    // check if either host URL or title has changed
    if (hostUrlChanged || titleChanged || (findableChanged && record.getFindable())) {
      record.setUpdateDate(new Date());
      // if hot URL or title has changed than proceed with generation of the
      // native resource
      String title = "";
      // First, try to create native resource from the response
      nativeResource = createNativeResource(record);
      if (nativeResource == null) {
        // if no native resource available, create an artificial one based on
        //the information (host URL & title) from the repository.
        nativeResource = record.generateNativeResource();
        // get title
        title = record.getName();
        ProtocolInvoker.setLockTitle(record.getProtocol(), false);
      } else {
        // if the native resource is available, make sure it's a valid metadata resource
        // prepare record for publication; it will validate schema
        PublicationRequest pubReq = new PublicationRequest(context, user, nativeResource.getContent());
        Schema schema = pubReq.prepareForPublication();
        boolean lockTitle = false;
        if (record.getName().length() == 0) {
          title = schema.getMeaning().getTitle();
        } else {
          if (!ProtocolInvoker.getLockTitle(record.getProtocol())) {
            lockTitle = titleChanged;
          } else {
            lockTitle = true;
          }
          title = record.getName();
        }
        ProtocolInvoker.setLockTitle(record.getProtocol(), lockTitle);
      }
      if (title.length() == 0) {
        // if no title, create one using host URL
        if (!(record.getProtocol() instanceof HarvestProtocolAgp2Agp)) {
          title = record.getHostUrl();
        } else {
          HarvestProtocolAgp2Agp p = (HarvestProtocolAgp2Agp)record.getProtocol();
          title = p.getSourceHost() + " -> " + p.getDestinationHost();
        }
        ProtocolInvoker.setLockTitle(record.getProtocol(), false);
      }
      record.setName(title);
    }

    HrUpdateRequest request = new HrUpdateRequest(context, user, record);
    request.executeUpdate(nativeResource);

    return creating;
  }

  /**
   * Checks if user is administrator.
   * @param context request context
   * @return <code>true</code> if user is administrator
   */
  private boolean isAdministrator(RequestContext context) {
    return context.getUser() != null
      && context.getUser().getAuthenticationStatus().getWasAuthenticated()
      && context.getUser().getAuthenticationStatus().
      getAuthenticatedRoles().hasRole("gptAdministrator");
  }
}
