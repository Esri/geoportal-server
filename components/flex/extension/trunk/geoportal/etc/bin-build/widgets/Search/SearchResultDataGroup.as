////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2010 ESRI
//
// All rights reserved under the copyright laws of the United States.
// You may freely redistribute and use this software, with or
// without modification, provided you include the original copyright
// and use restrictions.  See use restrictions in the file:
// <install location>/License.txt
//
////////////////////////////////////////////////////////////////////////////////
package widgets.Search
{

import mx.core.ClassFactory;

import spark.components.DataGroup;

// these events bubble up from the SearchResultItemRenderer
[Event(name="searchResultClick", type="flash.events.Event")]
[Event(name="searchResultMouseOver", type="flash.events.Event")]
[Event(name="searchResultMouseOut", type="flash.events.Event")]

public class SearchResultDataGroup extends DataGroup
{
    public function SearchResultDataGroup()
    {
        super();

        this.itemRenderer = new ClassFactory(SearchResultItemRenderer);
    }
}

}
