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
package com.esri.gpt.control.georss.dcatdef;

import static com.esri.gpt.framework.util.Val.escapeStrForJson;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.List;

/**
 *
 * @author Esri, Inc.
 */
public class DcatPrinter {
  private final static int TAB_SIZE = 4;
  private final PrintWriter writer;
  private final ArrayDeque<Level> levels = new ArrayDeque<Level>();
  {
    levels.add(new Level());
  }

  public DcatPrinter(PrintWriter writer) {
    this.writer = writer;
  }

  public void flush() {
    writer.flush();
  }

  public void close() {
    writer.close();
  }
  
  private void levelUp() {
    levels.push(new Level());
  }
  
  private void levelDown() {
    if (levels.size()>1) {
      levels.pop();
    }
  }
  
  private void printTab() {writer.print(" ");
    for (int i=0; i<TAB_SIZE; i++) {
      
    }
  }
  
  private void printTabs() {
    for (int i=0; i<(levels.size()-1); i++) {
      printTab();
    }
  }

  void startObject() {
    if (getAnythingPrinted()) {
      writer.println(",");
    }
    printTabs();
    writer.println("{");
    levelUp();
  }

  void startObject(String name) {
    if (getAnythingPrinted()) {
      writer.println(",");
    }
    levels.push(new Level());
    printTabs();
    writer.println("\""+name+"\": {");
    levelUp();
  }

  void startArray(String name) {
    if (getAnythingPrinted()) {
      writer.println(",");
    }
    printTabs();
    writer.println("\""+name+"\": [");
    levelUp();
  }

  void endObject() {
    levelDown();
    writer.println();
    printTabs();
    writer.print("}");
    setAnythingPrinted();
  }

  void endArray() {
    levelDown();
    writer.println();
    printTabs();
    writer.print("]");
    setAnythingPrinted();
  }

  void printAttribute(String outName, String value) {
    if (getAnythingPrinted()) {
      writer.println(",");
    }
    printTabs();
    writer.print("\""+outName+"\": \"" +escapeStrForJson(value)+ "\"");
    setAnythingPrinted();
  }

  void printAttribute(String outName, List<String> value) {
    if (getAnythingPrinted()) {
      writer.println(",");
    }
    printTabs();
    writer.print("\""+outName+"\": [");
    if (value.isEmpty()) {
      writer.print("]");
    } else {
      writer.println();
      for (int i=0; i<value.size(); i++) {
        if (i>0) {
          writer.println(",");
        }
        printTabs();
        printTab();
        printTab();
        printTab();
        writer.print("\"");
        writer.print(escapeStrForJson(value.get(i)));
        writer.print("\"");
      }
      writer.println();
      printTabs();
      writer.print("]");
    }
    setAnythingPrinted();
  }
  
  private boolean getAnythingPrinted() {
    return getLevel().anythingPrinted;
  }
  
  private void setAnythingPrinted() {
    getLevel().anythingPrinted = true;
  }
  
  private Level getLevel() {
    return levels.peek();
  }
  
  private static class Level {
    public boolean anythingPrinted = false;
  }
}
