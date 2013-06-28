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
