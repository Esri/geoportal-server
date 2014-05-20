/*
 * Copyright 2014 Esri, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.gpt.framework.context;

import java.util.ArrayList;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application context listener.
 */
public class ApplicationContextListener implements ServletContextListener {
  private static final ArrayList<ShuttdownListener> shutdownListeners = new ArrayList<ShuttdownListener>();
  private static volatile boolean alive;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    alive = true;
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    alive = false;
    synchronized(ApplicationContextListener.class) {
      for (ShuttdownListener l: shutdownListeners) {
        l.onShutdown();
      }
    }
  }
  
  public static boolean isAlive() {
    return alive;
  }
  
  public static synchronized void registerShutdownListener(ShuttdownListener l) {
    shutdownListeners.add(l);
  }
  
  public static synchronized void unregisterShutdownListener(ShuttdownListener l) {
    shutdownListeners.remove(l);
  }
  
  public static interface ShuttdownListener {
    void onShutdown();
  }
}
