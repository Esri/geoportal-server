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
package com.esri.viewer.components.toc.controls
{

import flash.events.Event;

import mx.controls.CheckBox;

/**
 * CheckBox that supports a tri-state check. In addition to selected and
 * unselected, the CheckBox can be in an indeterminate state.
 */
public class CheckBoxIndeterminate extends CheckBox
{
    /**
     * Creates a new tri-state CheckBox with custom skin.
     */
    public function CheckBoxIndeterminate()
    {
        setStyle("icon", CheckBoxIndeterminateIcon);
        setStyle("indeterminate", _indeterminate);
    }

    //--------------------------------------------------------------------------
    //  Property:  indeterminate
    //--------------------------------------------------------------------------

    private var _indeterminate:Boolean = false;

    [Bindable("indeterminateChanged")]
    /**
     * Whether this check box is in the indeterminate state.
     */
    public function get indeterminate():Boolean
    {
        return _indeterminate;
    }

    /**
     * @private
     */
    public function set indeterminate(value:Boolean):void
    {
        if (value != _indeterminate)
        {
            _indeterminate = value;
            setStyle("indeterminate", _indeterminate);

            dispatchEvent(new Event("indeterminateChanged"));
        }
    }
}

}
