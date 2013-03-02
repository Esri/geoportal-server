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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.framework.util.Val;
import org.apache.lucene.document.Field;

/**
 * Provides some generic supporting utilities.
 */
public class Utilities { 
   
  /** methods ================================================================= */
  
  /**
   * Makes the Field.Index option from a String value.
   * <br/>TOKENIZED, UN_TOKENIZED, NO, NO_NORMS
   * @param option the Field.Index option
   */
  protected Field.Index indexingOptionFromString(String option) {
    option = Val.chkStr(option).toUpperCase();
    if (option.equals("") || option.equals("TOKENIZED")) {
      return Field.Index.ANALYZED;
    } else if (option.equals("UN_TOKENIZED")) {
      return Field.Index.NOT_ANALYZED;
    } else if (option.equals("NO")) {
      return Field.Index.NO;
    } else if (option.equals("NO_NORMS")) {
      return Field.Index.ANALYZED_NO_NORMS;
    } else {
      return Field.Index.ANALYZED;
    }
  }
  
  /**
   * Makes the Field.Store option from a String value.
   * <br/>YES, NO, COMPRESS
   * @param option the Field.Store option
   */
  protected Field.Store storageOptionFromString(String option) {
    option = Val.chkStr(option).toUpperCase();
    if (option.equals("") || option.equals("YES")) {
      return Field.Store.YES;
    } else if (option.equals("NO")) {
      return Field.Store.NO;
    } else if (option.equals("COMPRESS")) {
      return Field.Store.YES;
    } else {
      return Field.Store.YES;
    }
  }
  
  /**
   * Makes the Field.TermVector option from a String value.
   * <br/>NO, YES, WITH_OFFSETS, WITH_POSITIONS
   * @param option the Field.TermVector option
   */
  protected Field.TermVector termVectorOptionFromString(String option) {
    option = Val.chkStr(option).toUpperCase();
    if (option.equals("") || option.equals("NO")) {
      return Field.TermVector.NO;
    } else if (option.equals("YES")) {
      return Field.TermVector.YES;
    } else if (option.equals("WITH_OFFSETS")) {
      return Field.TermVector.WITH_OFFSETS;
    } else if (option.equals("WITH_POSITIONS")) {
      return Field.TermVector.WITH_POSITIONS;
    } else if (option.equals("WITH_POSITIONS_OFFSETS")) {
      return Field.TermVector.WITH_POSITIONS_OFFSETS;
    } else {
      return Field.TermVector.NO;
    }
  }

}
