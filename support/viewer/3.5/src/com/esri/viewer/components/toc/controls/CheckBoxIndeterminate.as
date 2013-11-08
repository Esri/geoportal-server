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
package com.esri.viewer.components.toc.controls
{

import flash.events.Event;

import mx.controls.CheckBox;

/**
 * CheckBox that supports a tri-state check. In addition to selected and
 * unselected, the CheckBox can be in an indeterminate state.
 *
 * @private
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
        setStyle("layoutDirection", "ltr"); // fix check mark's direction - https://bugs.adobe.com/jira/browse/SDK-25817
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
