/*
 * Copyright 2015 Esri, Inc..
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
package com.esri.gpt.control.cart;

import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.catalog.lucene.Storeables;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.IndexSearcher;

/**
 * Volume "/try" handler.
 *
 * @author Esri, Inc.
 */
public class VolumeTryHandler implements ITryHandler {

  private static final String TRY_VOLUMEHANDLER_FIELD_PARAM = "catalog.cart.volumetryhandler.commaSeparatedFields";
  private static final String TRY_VOLUMEHANDLER_MAX_PARAM = "catalog.cart.volumetryhandler.max";

  private static final Logger LOGGER = Logger.getLogger(VolumeTryHandler.class.getCanonicalName());

  @Override
  public TryResponse tryKeys(HttpServletRequest request, HttpServletResponse response, RequestContext context, Cart cart, List<String> keys) {
    TryResponse tryResponse = new TryResponse();

    String [] fieldNames = readFieldNames(context);
    double max = readMax(context);

    if (keys != null && fieldNames!=null && fieldNames.length>0 && max > 0) {

      LuceneIndexAdapter adapter = null;
      IndexSearcher searcher = null;

      try {
        adapter = new LuceneIndexAdapter(context);
        searcher = adapter.newSearcher();
        
        IndexReader reader = searcher.getIndexReader();
        TermDocs termDocs = reader.termDocs();
        MapFieldSelector selector = new MapFieldSelector(fieldNames);
        
        VolumeReader volumeReader = new VolumeReader(reader, termDocs, selector, fieldNames);
        double already = volumeReader.sumWeights(cart.keySet());

        for (String uuid : keys) {
          double after = already + volumeReader.readWeight(uuid);
          tryResponse.add(uuid, after<=max);
        }

      } catch (Exception ex) {
        LOGGER.log(Level.WARNING, "Error opening searcher", ex);
      } finally {
        try {
          if ((adapter != null) && (searcher != null)) {
            adapter.closeSearcher(searcher);
          }
        } catch (Exception ef) {
        }
      }
    }

    return tryResponse;
  }

  private String [] readFieldNames(RequestContext context) {
    StringAttributeMap parameters = context.getApplicationConfiguration().getCatalogConfiguration().getParameters();
    String fieldNames = Val.chkStr(parameters.getValue(TRY_VOLUMEHANDLER_FIELD_PARAM));
    String [] fieldsArray = fieldNames.split(",");
    for (int i=0; i<fieldsArray.length; i++) {
      fieldsArray[i] = Val.chkStr(fieldsArray[i]);
    }
    return fieldsArray;
  }

  private double readMax(RequestContext context) {
    StringAttributeMap parameters = context.getApplicationConfiguration().getCatalogConfiguration().getParameters();
    return Val.chkDbl(parameters.getValue(TRY_VOLUMEHANDLER_MAX_PARAM), 0);
  }

  private class VolumeReader {

    private final IndexReader reader;
    private final TermDocs termDocs;
    private final MapFieldSelector selector;
    private final String [] fieldNames;

    public VolumeReader(IndexReader reader, TermDocs termDocs, MapFieldSelector selector, String [] fieldNames) {
      this.reader = reader;
      this.termDocs = termDocs;
      this.selector = selector;
      this.fieldNames = fieldNames;
    }

    public double readWeight(String uuid) throws IOException {
      termDocs.seek(new Term(Storeables.FIELD_UUID, uuid));
      double weight = 0;
      if (termDocs.next()) {
        Document document = reader.document(termDocs.doc(), selector);
        weight += readWeights(document, fieldNames);
      }
      return weight;
    }

    private double readWeights(Document document, String [] fieldNames) {
      double allWeights = 0.0;
      for (String fieldName: fieldNames) {
        if (fieldName.isEmpty()) continue;
        allWeights += readWeight(document, fieldName);
      }
      return allWeights;
    }
    
    private double readWeight(Document document, String fieldName) {
      Field[] fields = document.getFields(fieldName);
      if (fields!=null && fields.length>0) {
        Field fld = fields[0];
        String value = fld.stringValue();
        try {
          return Math.max(Double.parseDouble(value), 0);
        } catch (Exception ex) {

        }
      }
      return 0.0;
    }
    
    public double sumWeights(Collection<String> uuids) throws IOException {
      double sum = 0;
      for (String uuid: uuids) {
        sum += readWeight(uuid);
      }
      return sum;
    }
  }

}
