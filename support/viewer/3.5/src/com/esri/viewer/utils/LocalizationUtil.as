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
package com.esri.viewer.utils
{

import mx.resources.ResourceManager;

public class LocalizationUtil
{
    private static const VIEWER_STRINGS:String = "ViewerStrings";

    public static function getDefaultString(resourceName:String, ... params):String
    {
        return ResourceManager.getInstance().getString(VIEWER_STRINGS, resourceName, params);
    }
}
}
