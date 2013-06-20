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

import mx.skins.halo.CheckBoxIcon;

/**
 * CheckBox skin that supports a tri-state check. In addition to selected and
 * unselected, the CheckBox can be in an indeterminate state.
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
