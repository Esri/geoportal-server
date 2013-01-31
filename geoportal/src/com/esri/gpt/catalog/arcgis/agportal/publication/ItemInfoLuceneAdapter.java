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
package com.esri.gpt.catalog.arcgis.agportal.publication;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.IndexSearcher;

import com.esri.gpt.catalog.arcgis.agportal.itemInfo.ESRI_ItemInformation;
import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.catalog.lucene.Storeables;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import java.util.Arrays;

/**
 * Builds ESRI_ItemInformation by querying Lucene index.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class ItemInfoLuceneAdapter {

	/**
	 * instance variables ======================================================
	 */
	private LuceneIndexAdapter baseAdapter;
	private IndexSearcher searcher;

	/**
	 * constructors ============================================================
	 */

	/** Default constructor */
	public ItemInfoLuceneAdapter() {
	}

	/**
	 * methods =================================================================
	 */

	/**
	 * Closes any open resources.
	 */
	public void close() {
		if ((this.baseAdapter != null) && (this.searcher != null)) {
			this.baseAdapter.closeSearcher(this.searcher);
		}
	}

	/**
	 * Gets the index srch.
	 * 
	 * @param context
	 *          the operation context
	 * @return the index srch
	 * @throws CorruptIndexException
	 *           if the index is corrupt
	 * @throws IOException
	 *           if an I/O exception occurs
	 */
	private IndexSearcher getSearcher(RequestContext context)
			throws CorruptIndexException, IOException {
		if (this.searcher != null) {
			return this.searcher;
		} else {
			if (this.baseAdapter == null) {
				this.baseAdapter = new LuceneIndexAdapter(context);
			}
			this.searcher = this.baseAdapter.newSearcher();
			return this.searcher;
		}
	}

	/**
	 * Returns the field values associated with a document
	 * 
	 * @param context
	 *          the operation context
	 * @param fields
	 *          array of fields to fetch
	 * @param uuid
	 *          the document uuid
	 * @return the field values (null if not found)
	 * @throws CorruptIndexException
	 *           if the index is corrupt
	 * @throws IOException
	 *           if an I/O exception occurs
	 */
	public ESRI_ItemInformation makeItemInfoByUuid(RequestContext context, String[] fields,
			String uuid) throws CorruptIndexException, IOException {
		TermDocs termDocs = null;
		IndexReader reader = null;
		MapFieldSelector selector = null;
		try {
			uuid = Val.chkStr(uuid);
			if (uuid.length() > 0) {
				IndexSearcher srch = this.getSearcher(context);
				reader = srch.getIndexReader();
				if (fields != null) {
					selector = new MapFieldSelector(fields);
				}
				termDocs = reader.termDocs();
				termDocs.seek(new Term(Storeables.FIELD_UUID, uuid));
				if (termDocs.next()) {
					Document document = reader.document(termDocs.doc(), selector);
					List<Fieldable> flds = document.getFields();
                    
					ESRI_ItemInformation itemInfo = new ESRI_ItemInformation();
                    itemInfo.setType("Map Service");
                    itemInfo.setTags(Arrays.asList(new String[]{"ArcGIS","Server map","service"}));
                    
					Envelope extent = new Envelope();
					for (Fieldable fld : flds) {
						String fieldName = fld.name();
						String[] vals = document.getValues(fieldName);
                        if (fieldName.contains("uuid")) {
                          itemInfo.setId(vals[0].replaceAll("^\\{|\\}$|\\-", ""));
                        } else if (fieldName.contains("title")) {
							itemInfo.setTitle(vals[0]);
							itemInfo.setName(vals[0]);
						} else if (fieldName.contains("resource.url")) {
							itemInfo.setUrl(vals[0]);
							String type = Val.chkStr(guessServiceTypeFromUrl(vals[0]));
							if (type.length() > 0) {
								itemInfo.setType(type);
							}
						} else if (fieldName.contains("contentType")) {
						} else if (fieldName.contains("keywords")) {
							itemInfo.setTypeKeywords(Arrays.asList(vals));
							itemInfo.setTags(Arrays.asList(vals));
						} else if (fieldName.contains("dataTheme")) {
							itemInfo.setTags(Arrays.asList(vals));
						} else if (fieldName.contains("abstract")) {
							itemInfo.setDescription(vals[0]);
//						} else if (fieldName.contains("xml")) {
//							itemInfo.setXml(vals[0]);
						} else if (fieldName.contains("minx")) {
							extent.setMinX(vals[0]);
						} else if (fieldName.contains("miny")) {
							extent.setMinY(vals[0]);
						} else if (fieldName.contains("maxx")) {
							extent.setMaxX(vals[0]);
						} else if (fieldName.contains("maxy")) {
							extent.setMaxY(vals[0]);
						} else if (fieldName.contains("thumbnail.url")) {
							itemInfo.setThumbnailUrl(vals[0]);
						}
					}
					itemInfo.setExtent(extent);
					return itemInfo;
				}
			}
		} finally {
			try {
				if (termDocs != null)
					termDocs.close();
				if (reader != null)
					reader.close();
			} catch (Exception ef) {
			}
		}
		return null;
	}

	/**
	 * Guesses a service type from a URL.
	 * 
	 * @param url
	 *          the url
	 * @return the service type
	 */
	private String guessServiceTypeFromUrl(String url) {
		String serviceType = "";
		String[] types = { "mapserver", "imageserver", "globeserver",
				"featureserver", "gpserver", "geocodeserver", "geometryserver",
				"networkserver", "geodataserver" };
		url = Val.chkStr(url).toLowerCase();
		if (url.contains("arcgis/rest") || url.contains("arcgis/services")
				|| url.contains("rest/services")) {
			serviceType = "ags";
			if (url.contains(types[0])) {
				serviceType = "Map Service";
			} else if (url.contains(types[1])) {
				serviceType = "Image Service";
			} else if (url.contains(types[2])) {
				serviceType = "Globe Service";
			} else if (url.contains(types[3])) {
				serviceType = "Feature Service";
			} else {
				serviceType = "Map Service";
			}
		} else if (url.contains("wmsserver")) {
			serviceType = "Web Map";
		} else if (url.endsWith(".nmf")) {
			serviceType = "Explorer Document";
		} else if (url.endsWith(".lyr")) {
			serviceType = "Layer File";
		} else if (url.endsWith(".mxd")) {
			serviceType = "ArcMap Document";
		} else if (url.endsWith(".lpk")) {
			serviceType = "Layer Package";
		} else if (url.endsWith(".ncgf")) {
			serviceType = "Explorer Application Configuration";
		} else if (url.endsWith(".mpk")) {
			serviceType = "Map Package";
		} else if (url.endsWith(".wmpk")) {
			serviceType = "Mobile Package";
		} else if (url.endsWith(".zip")) {
			serviceType = "Map template";
		} else if (url.endsWith(".esriaddin")) {
			serviceType = "Desktop Add In";
		} else if (url.endsWith(".eaz")) {
			serviceType = "Explorer Add In";
		}

		return serviceType;
	}

}
