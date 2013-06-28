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
package widgets.Query
{

import mx.core.ClassFactory;

import spark.components.DataGroup;

// these events bubble up from the QueryResultItemRenderer
[Event(name="queryResultClick", type="flash.events.Event")]
[Event(name="queryResultMouseOver", type="flash.events.Event")]
[Event(name="queryResultMouseOut", type="flash.events.Event")]

public class QueryResultDataGroup extends DataGroup
{
    public function QueryResultDataGroup()
    {
        super();

        this.itemRenderer = new ClassFactory(QueryResultItemRenderer);
    }
}

}
