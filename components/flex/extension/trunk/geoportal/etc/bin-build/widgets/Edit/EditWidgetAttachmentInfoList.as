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
package widgets.Edit
{

import spark.components.List;

public class EditWidgetAttachmentInfoList extends List
{
    [Bindable]
    public var deleteEnabled:Boolean = true;

    public function EditWidgetAttachmentInfoList()
    {
        super();
    }
}

}
