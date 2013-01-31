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
package com.esri.gpt.control.livedata.selector;

import com.esri.gpt.control.livedata.IRenderer;

/**
 * Pair of setters.
 */
class Setters {
  /** definitive setter */
  private final ISetter definitiveSetter = new DefinitiveSetter();
  /** non-definitive setter */
  private final ISetter nonDefinitiveSetter = new NonDefinitiveSetter();

  /** definitive renderer */
  private IRenderer definitveRenderer;
  /** non-definitive renderer */
  private IRenderer nonDefinitiveRenderer;

  /**
   * Gets definitive setter.
   * @return definitive setter
   */
  public ISetter getDefinitiveSetter() {
    return definitiveSetter;
  }

  /**
   * Gets non-definitive setter.
   * @return non-definitive setter
   */
  public ISetter getNonDefinitiveSetter() {
    return nonDefinitiveSetter;
  }

  /**
   * Gets renderer.
   * @return renderer
   */
  public synchronized IRenderer getRenderer() {
    return definitveRenderer != null ? definitveRenderer : nonDefinitiveRenderer;
  }

  /**
   * Definitive setter.
   */
  private class DefinitiveSetter implements ISetter {

    public void set(IRenderer renderer) {
      synchronized (Setters.this) {
        definitveRenderer = renderer;
        Setters.this.notifyAll();
      }
    }
  }

  /**
   * Non-definitive setter.
   */
  private class NonDefinitiveSetter implements ISetter {

    public void set(IRenderer renderer) {
      synchronized (Setters.this) {
        nonDefinitiveRenderer = renderer;
      }
    }
  }
}
