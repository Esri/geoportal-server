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

import com.esri.gpt.framework.request.PageCursor.IRecordsPerPageProvider;


/**
 * The Interface IPageCursor.
 */
public interface IPageCursor {


/**
 * Gets the current page for the cursor.
 * 
 * @return the current page
 */
public abstract int getCurrentPage();

/**
 * Sets the current page for the cursor.
 * 
 * @param currentPage the current page
 */
public abstract void setCurrentPage(int currentPage);

/**
 * Gets the ending page for the cursor.
 * 
 * @return the ending page
 */
public abstract int getEndPage();

/**
 * Gets the ending record for the current page.
 * 
 * @return the ending record
 */
public abstract int getEndRecord();

/**
 * Determine if the cursor has a next page.
 * 
 * @return true if the cursor has a next page
 */
public abstract boolean getHasNextPage();

/**
 * Determine if the cursor has a previous page.
 * 
 * @return true if the cursor has a previous page
 */
public abstract boolean getHasPreviousPage();

/**
 * Gets the next page.
 * 
 * @return the next page
 */
public abstract int getNextPage();

/**
 * Gets the maximum number of pages to be displayed per cursor.
 * 
 * @return the maximum number of pages per cursor
 */
public abstract int getPagesPerCursor();

/**
 * Sets the maximum number of pages to be displayed per cursor.
 * 
 * @param pagesPerCursor the maximum number of pages per cursor
 */
public abstract void setPagesPerCursor(int pagesPerCursor);

/**
 * Gets the previous page.
 * 
 * @return the previous page
 */
public abstract int getPreviousPage();

/**
 * Gets the maximum number of records to be displayed per page.
 * 
 * @return the maximum number of records per page
 */
public abstract int getRecordsPerPage();

/**
 * Sets the maximum number of records to be displayed per page.
 * 
 * @param recordsPerPage the maximum number of records per page
 */
public abstract void setRecordsPerPage(int recordsPerPage);

/**
 * Sets records per page provider.
 * 
 * @param recordsPerPageProvider records per page provider
 */
public abstract void setRecordsPerPageProvider(
    IRecordsPerPageProvider recordsPerPageProvider);

/**
 * Gets the starting page for the cursor.
 * 
 * @return the starting page
 */
public abstract int getStartPage();

/**
 * Gets the starting record for the current page.
 * 
 * @return the starting record
 */
public abstract int getStartRecord();

/**
 * Gets the total page count.
 * <p>
 * The total page count is based upon the total record count and the
 * number of records per page.
 * 
 * @return the total page count
 */
public abstract int getTotalPageCount();

/**
 * Gets the total record count.
 * <p>
 * This is the total record count associated with a query. It is used in
 * conjunction with recordsPerPage to determine the number of pages
 * available to the cursor.
 * 
 * @return the total record count
 */
public abstract int getTotalRecordCount();

/**
 * Sets the total record count.
 * <p>
 * This is the total record count associated with a query. It is used in
 * conjunction with recordsPerPage to determine the number of pages
 * available to the cursor.
 * 
 * @param totalRecordCount the total record count
 */
public abstract void setTotalRecordCount(int totalRecordCount);

/*
 * Checks to ensure that the current page is not greater than 
 * the total page count.
 */
/**
 * Check current page.
 */
public abstract void checkCurrentPage();

}