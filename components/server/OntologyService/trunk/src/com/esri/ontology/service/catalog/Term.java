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
package com.esri.ontology.service.catalog;

import com.esri.ontology.service.util.Val;

/**
 * Term.
 */
public class Term {

  /** subject */
  private String subject;
  /** category */
  private String category;
  /** relationship */
  private Relationship relationship;
  /** count */
  private float count;
  /** topic category indicator */
  private boolean topicCategory;

  /**
   * Creates instance of the term.
   * @param subject subject
   * @param relationship relationship
   * @param category category
   * @param count count
   */
  public Term(String subject, Relationship relationship, String category, float count) {
    setSubject(subject);
    setRelationship(relationship);
    setCategory(category);
    setCount(count);
    topicCategory = false;
  }

  /**
   * Creates instance of the term.
   * @param subject subject
   * @param relationship relationship
   * @param count count
   */
  public Term(String subject, Relationship relationship, float count) {
    setSubject(subject);
    setRelationship(relationship);
    setCategory("");
    setCount(count);
    topicCategory = true;
  }

  /**
   * Gets relationship.
   * @return relationship
   */
  public Relationship getRelationship() {
    return relationship;
  }

  /**
   * Sets relationship.
   * @param relationship relationship
   */
  public void setRelationship(Relationship relationship) {
    this.relationship = relationship;
  }

  /**
   * Gets subject.
   * @return subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets subject.
   * @param subject subject
   */
  public void setSubject(String subject) {
    this.subject = Val.chkStr(subject);
  }

  /**
   * Gets category.
   * @return category
   */
  public String getCategory() {
    return category;
  }

  /**
   * Sets category.
   * @param category category
   */
  public void setCategory(String category) {
    this.category = Val.chkStr(category);
  }

  /**
   * Gets count.
   * @return count
   */
  public float getCount() {
    return count;
  }

  /**
   * Sets count.
   * @param count count
   */
  public void setCount(float count) {
    this.count = (Math.round(1 / count * 100.0f) / 100.0f);
  }

  /**
   * Gets information if term is a topic category.
   * @return <code>true</code> if term is a topic category
   */
  public boolean isTopicCategory() {
    return topicCategory;
  }

  @Override
  public String toString() {
    if (!isTopicCategory()) {
      return subject + " (" + (relationship!=null? relationship.toString(): "") + ": " + category + "): " + count;
    } else {
      return subject + " (" + (relationship!=null? relationship.toString(): "") + ": Topic Category): " + count;
    }
  }

  /**
   * Relationship.
   */
  public static enum Relationship {
    /** sub class of another class */
    SubClassOf,
    /** reference to another class */
    SeeAlso;

    /**
     * Parses relationship as string.
     * @param relationship relationship name
     * @return relationship or <code>null</code> if invalid name
     */
    public static Relationship parse (String relationship) {
      relationship = Val.chkStr(relationship);
      for (Relationship r : values()) {
        if (r.name().equalsIgnoreCase(relationship)) {
          return r;
        }
      }
      return null;
    }
  }
}