/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import java.util.List;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * Field meta loader.
 */
public class FieldMetaLoader {
  /**
   * Loads Lucene index metadata.
   * @param context request context
   * @param fields list of fields
   * @throws CatalogIndexException if accessing index fails
   */
  public static void loadLuceneMeta(RequestContext context, List<IFeedRecords.FieldMeta> fields) throws CatalogIndexException {
    LuceneIndexAdapter indexAdapter = new LuceneIndexAdapter(context);
    IndexSearcher searcher = null;
    try {
      searcher = indexAdapter.newSearcher();
      IndexReader indexReader = searcher.getIndexReader();
      for (String fieldName : indexReader.getFieldNames(IndexReader.FieldOption.ALL)) {
        fields.add(new IFeedRecords.FieldMeta(IFeedRecord.STD_COLLECTION_INDEX + "." + fieldName, "esriFieldTypeString", fieldName));
      }
    } catch (Exception e) {
      String sMsg = "Error accessing index:\n " + Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg, e);
    } finally {
      indexAdapter.closeSearcher(searcher);
    }
  }
    
}
