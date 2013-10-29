/*
 * Copyright 2013 Esri.
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
package com.esri.gpt.control.webharvest.engine;

/**
 * Worker interface.
 */
public interface IWorker {

  /**
   * Checks if worker is active.
   * @return <code>true</code> if worker is active
   */
  boolean isActive();

  /**
   * Checks if is executing synchronization of the specific repository.
   * @param uuid repository UUID
   * @return <code>true</code> if is executing synchronization of the specific repository
   */
  boolean isExecuting(String uuid);

  /**
   * Gets shutdown flag.
   * @return shutdown flag
   */
  boolean isShutdown();

  /**
   * Gets suspended flag.
   * @return suspended flag
   */
  boolean isSuspended();
  
}
