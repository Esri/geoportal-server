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
package widgets.Navigation
{

import spark.components.Button;

//icons
[Style(name="iconUp", type="*")]
[Style(name="iconOver", type="*")]
[Style(name="iconDown", type="*")]
[Style(name="iconDisabled", type="*")]

//paddings
[Style(name="paddingLeft", type="Number")]
[Style(name="paddingRight", type="Number")]
[Style(name="paddingTop", type="Number")]
[Style(name="paddingBottom", type="Number")]

public class IconButton extends Button
{
    public function IconButton()
    {
        super();
    }
}

}
