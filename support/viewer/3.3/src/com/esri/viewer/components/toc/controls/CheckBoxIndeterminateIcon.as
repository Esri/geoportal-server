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

import mx.skins.halo.CheckBoxIcon;

/**
 * CheckBox skin that supports a tri-state check. In addition to selected and
 * unselected, the CheckBox can be in an indeterminate state.
 *
 * @private
 */
public class CheckBoxIndeterminateIcon extends CheckBoxIcon
{
    /**
     * @private
     */
    override protected function updateDisplayList(w:Number, h:Number):void
    {
        super.updateDisplayList(w, h);

        var indet:Boolean = getStyle("indeterminate");

        if (indet)
        {
            var cornerRadius:Number = 2;

            var boxFillColors:Array = [ 0xAAAACC, 0x666666 ];
            var boxFillAlphas:Array = [ 1.0, 1.0 ];

            drawRoundRect(
                3, 3, w - 6, h - 6, cornerRadius,
                boxFillColors, boxFillAlphas,
                verticalGradientMatrix(1, 1, w - 2, h - 2));
        }
    }
}

}
