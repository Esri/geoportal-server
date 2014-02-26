///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2011 Esri. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////
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
