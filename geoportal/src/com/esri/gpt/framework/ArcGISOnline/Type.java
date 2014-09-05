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

package com.esri.gpt.framework.ArcGISOnline;

/**
 * ArcGIS Online resource type.
 */
public class Type {
  protected final String typeName;
  protected final DataType dataType;
  protected final DataCategory dataCategory;
  protected ITypePredicate predicate;
  protected final FileType[] fileTypes;
  
  public Type(String typeName, DataType dataType, DataCategory dataCategory, ITypePredicate predicate, FileType...fileTypes) {
    this.typeName = typeName;
    this.dataType = dataType;
    this.dataCategory = dataCategory;
    this.predicate = predicate;
    this.fileTypes = fileTypes;
  }
  
  public Type(String typeName, DataType dataType, DataCategory dataCategory, FileType...fileTypes) {
    this.typeName = typeName;
    this.dataType = dataType;
    this.dataCategory = dataCategory;
    this.predicate = dataType==DataType.FILE? new FileTypePredicateImpl(): null;
    this.fileTypes = fileTypes;
  }
  
  @Override
  public String toString() {
    return typeName;
  }

  public String getTypeName() {
    return typeName;
  }

  public DataType getDataType() {
    return dataType;
  }
  
  public DataCategory getDataCategory() {
    return dataCategory;
  }
  
  public boolean matches(String url) {
    return predicate!=null? predicate.matches(url): false;
  }
  
  public final class FileTypePredicateImpl extends FileTypePredicate {
    @Override
    protected FileType[] getFileTypes() {
      return fileTypes;
    }
  }
  
  public static class ServiceType extends Type {
    
    public ServiceType(String typeName, DataType dataType, DataCategory dataCategory) {
      super(typeName,dataType,dataCategory);
      this.predicate = new AgsServiceTypePredicateImpl();
    }
    
    public ServiceType(String typeName, DataType dataType, DataCategory dataCategory, String serviceName) {
      super(typeName,dataType,dataCategory);
      this.predicate = new AgsServiceTypePredicateImpl(serviceName);
    }
  }
  
  public final class AgsServiceTypePredicateImpl extends AgsServiceTypePredicate {
    public AgsServiceTypePredicateImpl() {
      super();
    }
    
    public AgsServiceTypePredicateImpl(String typeName) {
      super(typeName);
    }

    @Override
    protected String getTypeName() {
      return typeName;
    }
    
  }
}
