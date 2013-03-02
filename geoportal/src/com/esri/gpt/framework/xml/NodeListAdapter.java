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
package com.esri.gpt.framework.xml;

import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handy implmenetation of {@link java.util.Iterator} over a {@link org.w3c.dom.NodeList}.
 * Allows to create iteration mechanizm utilizing <code>for( element : elements )</code> loop. Example:</br>
 * <div style="border-style: solid; border-width: thin; border-color: #A4A4A4; background-color: #F5F6CE">
 * <code><pre>
 *     NodeList ndList = ...; <i>// obtain a list of nodes</i>
 *     for (Node nd : <b>new NodeListAdapter(ndList)</b>) {
 *        <i>// do something usefull with nd </i>
 *     }
 * </pre></code>
 * </div>
 */
public class NodeListAdapter implements Iterable<Node> {

/** node list */
private NodeList nodes;

/**
 * Creates instance of the adapter.
 * @param nodes nodes to iterate over (can be <code>null</code>)
 */
public NodeListAdapter(NodeList nodes) {
  this.nodes = nodes;
}

/**
 * Returns an iterator over a set of nodes. This is always instance of
 * {@link com.esri.gpt.framework.util.ReadOnlyIterator}.
 * @return iterator
 */
@Override
public Iterator<Node> iterator() {
  return new NodeListIterator();
}

/**
 * Iterator over the list of nodes.
 */
private class NodeListIterator extends ReadOnlyIterator<Node> {
/** index of the current node */
private int index = -1;

@Override
public boolean hasNext() {
  return nodes!=null? index+1 < nodes.getLength(): false;
}

@Override
public Node next() {
  if (!hasNext()) {
    throw new NoSuchElementException("No more elements available.");
  }
  return nodes.item(++index);
}
}

}
