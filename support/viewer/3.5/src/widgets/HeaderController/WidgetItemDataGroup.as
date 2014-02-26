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
package widgets.HeaderController
{

import flash.events.KeyboardEvent;
import flash.ui.Keyboard;

import mx.core.ClassFactory;
import mx.managers.IFocusManagerComponent;

import spark.components.DataGroup;

// these events bubble up from the WidgetItemRenderer and GroupWidgetItemRenderer
[Event(name="widgetItemClick", type="flash.events.Event")]
[Event(name="widgetItemMouseOver", type="flash.events.Event")]
[Event(name="widgetItemMouseOut", type="flash.events.Event")]

public class WidgetItemDataGroup extends DataGroup
{
    public function WidgetItemDataGroup()
    {
        super();

        this.itemRendererFunction = rendererFunction;

        addEventListener(KeyboardEvent.KEY_UP, keyboardKeyUpHandler);
    }

    private function rendererFunction(item:Object):ClassFactory
    {
        if (item.isGroup)
        {
            return new ClassFactory(GroupWidgetItemDataGroupRenderer);
        }
        else
        {
            return new ClassFactory(WidgetItemDataGroupRenderer);
        }
    }

    private function keyboardKeyUpHandler(event:KeyboardEvent):void
    {
        var comp:IFocusManagerComponent;

        if (event.keyCode == Keyboard.LEFT)
        {
            comp = focusManager.getNextFocusManagerComponent(true);
        }
        else if (event.keyCode == Keyboard.RIGHT)
        {
            comp = focusManager.getNextFocusManagerComponent();
        }

        if (comp && (comp is WidgetItemDataGroupRenderer || comp is GroupWidgetItemDataGroupRenderer))
        {
            comp.setFocus();
        }
    }
}

}
