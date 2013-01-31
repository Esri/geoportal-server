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
package com.esri.gpt.catalog.management;
import com.esri.gpt.framework.util.Val;

/**
 * This class defines enums associated with metadata documents.
 */
public class MmdEnums {
  
public static String INCOMING_STATUS = "INCOMING_STATUS";
  
/**
 * An enumeration describing a metadata record's approval status.
 */
public enum ApprovalStatus {
  
  /** Any status. */
  any,
  /** Document was posted (default value). */
  posted,
  /** Document is incomplete. */
  incomplete,
  /** Document was reviewed. */
  reviewed,
  /** Document was approved. */
  approved,
  /** Document was disapproved. */
  disapproved,
  /** Document is a draft*/
  draft,
  /** Document action is pending add from thunderdome **/
  pending_approved,
  /** Document action is pending delete from thunderdome **/
  pending_delete,
  
  pending_posted,
  
  pending_incomplete,
  
  pending_reviewed,
  
  pending_disapproved,
  
  pending_draft,
  
  remotely_deleted;

  /**
   * Checks the value of a String to determine the corresponding enum.
   * @param value the string to check
   * @return the corresponding enum (default is ApprovalStatus.posted)
   */
  public static ApprovalStatus checkValue(String value) {
    try {
      return ApprovalStatus.valueOf(Val.chkStr(value));
    } catch (IllegalArgumentException ex) {
      return ApprovalStatus.defaultValue();
    }
  }
  
  /**
   * Returns the default value for the enum.
   * @return ApprovalStatus.posted
   */
  public static ApprovalStatus defaultValue() {
    return ApprovalStatus.posted;
  }
  
  /**
   * Determines if a status String represents a publicly visible state.
   * <br/>ApprovalStatus.approved or ApprovalStatus.reviewed
   * @return true if publicly visible
   */
  public static boolean isPubliclyVisible(String value) {
    try {
      ApprovalStatus status = ApprovalStatus.valueOf(Val.chkStr(value));
      return status.equals(ApprovalStatus.approved) ||
             status.equals(ApprovalStatus.reviewed);
    } catch (IllegalArgumentException ex) {}
    return false;
  }
  
}

/**
 * An enumeration describing the metadata publication method.
 */
  public enum PublicationMethod {
 
    /** Any method. */
  any,
  /** Published through the online editor. */
  editor,
  /** Published through file upload. */
  upload,
  /** Published through batch file upload. */
  batch,
  /** Published through the harvester. */
  harvester,
  /** 
   * Published through registration.
   */
  registration,
  /** 
   * Published through the sdi.suite smartEditor.
   */
  seditor,
  /** Published through another method (default value). */
  other;

  /**
   * Checks the value of a String to determine the corresponding enum.
   * @param method the string to check
   * @return the corresponding enum (default is PublicationMethod.other)
   */
  public static PublicationMethod checkValue(String method) {
    try {
      return PublicationMethod.valueOf(Val.chkStr(method));
    } catch (IllegalArgumentException ex) {
      return PublicationMethod. defaultValue();
    }
  }
  
  /**
   * Returns the default value for the enum.
   * @return PublicationMethod.other
   */
  public static PublicationMethod defaultValue() {
    return PublicationMethod.other;
  }
}
 
}
