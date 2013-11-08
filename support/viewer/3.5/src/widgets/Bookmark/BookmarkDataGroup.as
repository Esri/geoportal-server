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
package widgets.Bookmark
{

import flash.events.Event;
import flash.events.FocusEvent;
import flash.events.KeyboardEvent;
import flash.ui.Keyboard;

import mx.core.ClassFactory;

import spark.components.List;
import spark.components.supportClasses.ItemRenderer;

// these events bubble up from the BookmarkItemRenderer
[Event(name="bookmarkClick", type="flash.events.Event")]
[Event(name="bookmarkDelete", type="flash.events.Event")]

public class BookmarkDataGroup extends List
{
    public function BookmarkDataGroup()
    {
        super();
        this.itemRenderer = new ClassFactory(BookmarkItemRenderer);
        addEventListener(FocusEvent.FOCUS_IN, focusHandler);
        addEventListener(FocusEvent.FOCUS_OUT, focusHandler);
        arrowKeysWrapFocus = true;
    }

    //--------------------------------------------------------------------------
    //
    //  Focus Management
    //
    //--------------------------------------------------------------------------

    private function focusHandler(event:FocusEvent):void
    {
        if (selectedIndex == -1 && event.type == FocusEvent.FOCUS_IN)
        {
            selectedIndex = 0;
        }
    }

    //--------------------------------------------------------------------------
    //
    //  Keyboard Management
    //
    //--------------------------------------------------------------------------

    private var keyboardPressed:Boolean = false;

    override protected function keyDownHandler(event:KeyboardEvent):void
    {
        super.keyDownHandler(event);

        if (event.keyCode != Keyboard.SPACE && event.keyCode != Keyboard.DELETE)
        {
            return;
        }

        keyboardPressed = true;
        event.updateAfterEvent();
    }

    override protected function keyUpHandler(event:KeyboardEvent):void
    {
        super.keyUpHandler(event);

        if (event.keyCode != Keyboard.SPACE && event.keyCode != Keyboard.DELETE)
        {
            return;
        }

        if (enabled && keyboardPressed)
        {
            keyboardPressed = false;
            var bookmark:Bookmark = selectedItem as Bookmark;
            var renderer:ItemRenderer = getItemRenderer(bookmark);

            if (renderer)
            {
                // Mimic mouse click on the button.
                if (event.keyCode == Keyboard.DELETE && bookmark.userCreated)
                {
                    renderer.dispatchEvent(new Event("bookmarkDelete", true)); // bubbles
                }
                else if (event.keyCode == Keyboard.SPACE)
                {
                    renderer.dispatchEvent(new Event("bookmarkClick", true)); // bubbles
                }
            }
        }
        event.updateAfterEvent();
    }

    private function getItemRenderer(item:Object):ItemRenderer
    {
        var n:int = dataGroup.numElements;
        for (var i:int = 0; i < n; i++)
        {
            var renderer:ItemRenderer = dataGroup.getElementAt(i) as ItemRenderer;
            if (renderer && renderer.data == item)
            {
                return renderer;
            }
        }
        return null;
    }
}

}
