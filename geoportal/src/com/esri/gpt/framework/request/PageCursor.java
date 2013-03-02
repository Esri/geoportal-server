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
package com.esri.gpt.framework.request;

import java.io.Serializable;

/**
 * Defines parameters associated with a UI page cursor.
 */
@SuppressWarnings("serial")
public class PageCursor implements Serializable, IPageCursor {

// class variables =============================================================
// instance variables ==========================================================
protected int _currentPage = 1;
protected int _pagesPerCursor = 10;
protected int _totalRecordCount = 0;
protected IRecordsPerPageProvider _recordsPerPageProvider = 
  new DefaultRecordsPerPageProvider();

// constructors ================================================================
/** Default constructor. */
public PageCursor() {
}
// properties ==================================================================
/**
 * Gets the current page for the cursor.
 * 
 * @return the current page
 */
public int getCurrentPage() {
  return _currentPage;
}

/**
 * Sets the current page for the cursor.
 * 
 * @param currentPage the current page
 */
public void setCurrentPage(int currentPage) {
  _currentPage = currentPage;
}

/**
 * Gets the ending page for the cursor.
 * 
 * @return the ending page
 */
public int getEndPage() {
  int nEnd = getStartPage();
  int nPagesPer = getPagesPerCursor();
  int nPagesTotal = getTotalPageCount();
  for (int i = 1; i < nPagesPer; i++) {
    nEnd++;
  }
  if (nEnd > nPagesTotal) {
    nEnd = nPagesTotal;
  }
  return nEnd;
}

/**
 * Gets the ending record for the current page.
 * 
 * @return the ending record
 */
public int getEndRecord() {
  int nTotal = getTotalRecordCount();
  int nEnd = getStartRecord() + getRecordsPerPage() - 1;
  if (nEnd > nTotal) {
    nEnd = nTotal;
  }
  return nEnd;
}

/**
 * Determine if the cursor has a next page.
 * 
 * @return true if the cursor has a next page
 */
public boolean getHasNextPage() {
  return (getCurrentPage() < getTotalPageCount());
}

/**
 * Determine if the cursor has a previous page.
 * 
 * @return true if the cursor has a previous page
 */
public boolean getHasPreviousPage() {
  return (getCurrentPage() > 1);
}

/**
 * Gets the next page.
 * 
 * @return the next page
 */
public int getNextPage() {
  int nPage = (getCurrentPage() + 1);
  int nPages = getTotalPageCount();
  if (nPage > nPages) {
    nPage = nPages;
  }
  return nPage;
}

/**
 * Gets the maximum number of pages to be displayed per cursor.
 * 
 * @return the maximum number of pages per cursor
 */
public int getPagesPerCursor() {
  return _pagesPerCursor;
}

/**
 * Sets the maximum number of pages to be displayed per cursor.
 * 
 * @param pagesPerCursor the maximum number of pages per cursor
 */
public void setPagesPerCursor(int pagesPerCursor) {
  _pagesPerCursor = pagesPerCursor;
}

/**
 * Gets the previous page.
 * 
 * @return the previous page
 */
public int getPreviousPage() {
  int nPage = (getCurrentPage() - 1);
  if (nPage < 1) {
    nPage = 1;
  }
  return nPage;
}

/**
 * Gets the maximum number of records to be displayed per page.
 * 
 * @return the maximum number of records per page
 */
public int getRecordsPerPage() {
  return _recordsPerPageProvider.getRecordsPerPage();
}

/**
 * Sets the maximum number of records to be displayed per page.
 * 
 * @param recordsPerPage the maximum number of records per page
 */
public void setRecordsPerPage(int recordsPerPage) {
  _recordsPerPageProvider.setRecordsPerPage(recordsPerPage);
}

/**
 * Sets records per page provider.
 * 
 * @param recordsPerPageProvider records per page provider
 */
public void setRecordsPerPageProvider(
  IRecordsPerPageProvider recordsPerPageProvider) {
  _recordsPerPageProvider = recordsPerPageProvider!=null?
    recordsPerPageProvider: new DefaultRecordsPerPageProvider();
}

/**
 * Gets the starting page for the cursor.
 * 
 * @return the starting page
 */
public int getStartPage() {
  checkCurrentPage();
  int nStart = getCurrentPage();
  int nHalf = (getPagesPerCursor() / 2);
  if ((getPagesPerCursor() % 2) == 0) {
    nHalf--;
  }
  for (int i = 0; i < nHalf; i++) {
    nStart--;
  }
  if (nStart < 1) {
    nStart = 1;
  }
  return nStart;
}

/**
 * Gets the starting record for the current page.
 * 
 * @return the starting record
 */
public int getStartRecord() {
  checkCurrentPage();
  int nStart = ((getCurrentPage() - 1) * getRecordsPerPage()) + 1;
  if (nStart < 1) {
    nStart = 1;
  }
  return nStart;
}

/**
 * Gets the total page count.
 * <p>
 * The total page count is based upon the total record count and the
 * number of records per page.
 * 
 * @return the total page count
 */
public int getTotalPageCount() {
  int nPages = 0;
  int nRecords = getTotalRecordCount();
  int nPerPage = getRecordsPerPage();
  if (nRecords > 0) {
    nPages = 1;
    if (nPerPage > 0) {
      nPages = nRecords / nPerPage;
      if ((nRecords % nPerPage) > 0) {
        nPages++;
      }
    }
  }
  return nPages;
}

/**
 * Gets the total record count.
 * <p>
 * This is the total record count associated with a query. It is used in
 * conjunction with recordsPerPage to determine the number of pages
 * available to the cursor.
 * 
 * @return the total record count
 */
public int getTotalRecordCount() {
  return _totalRecordCount;
}

/**
 * Sets the total record count.
 * <p>
 * This is the total record count associated with a query. It is used in
 * conjunction with recordsPerPage to determine the number of pages
 * available to the cursor.
 * 
 * @param totalRecordCount the total record count
 */
public void setTotalRecordCount(int totalRecordCount) {
  if(totalRecordCount < 0) {
    totalRecordCount = 0;
  }
  _totalRecordCount = totalRecordCount;
}

// methods =====================================================================

/*
 * Checks to ensure that the current page is not greater than 
 * the total page count.
 */
/**
 * Check current page.
 */
public void checkCurrentPage() {
  if (getCurrentPage() > getTotalPageCount()) {
    setCurrentPage(1);
  }
}


/**
 * Records per page provider.
 */
public interface IRecordsPerPageProvider extends Serializable {
/** Minimum records per page */  
final int MIN_RECORDS_PER_PAGE = 5;
/** Default record per page */
final int DEFAULT_RECORDS_PER_PAGE = 5;
/**
 * Gets number of records per page.
 * @return records per page
 */
int getRecordsPerPage();

/**
 * Sets number of records per page.
 * @param recordsPerPage  number of records per page
 */
void setRecordsPerPage(int recordsPerPage);
}

/**
 * Default records per page provider.
 */
protected class DefaultRecordsPerPageProvider implements Serializable, 
  IRecordsPerPageProvider {
/** Records per page */
protected int _recordsPerPage = IRecordsPerPageProvider.DEFAULT_RECORDS_PER_PAGE;
/** 
 * Gets number of records per page.
 * @return number of records per page
 */
public int getRecordsPerPage() {
  return _recordsPerPage;
}
/**
 * Sets number of records per page.
 * @param recordsPerPage number of records per page
 */
public void setRecordsPerPage(int recordsPerPage) {
  _recordsPerPage = 
    Math.max(IRecordsPerPageProvider.MIN_RECORDS_PER_PAGE, recordsPerPage);
}
}
}
