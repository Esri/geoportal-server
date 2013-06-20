///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2011 Esri. All Rights Reserved.
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
package widgets.Geoprocessing.supportClasses
{

import flash.net.FileReference;
import flash.net.URLRequest;

public class FileDownloader
{
    private static const FILE_NAME_DEFAULT:String = "gpFile";

    private var _url:String;

    public function get url():String
    {
        return _url;
    }

    public function set url(value:String):void
    {
        _url = value;
    }

    private var _fileName:String;

    public function get fileName():String
    {
        return _fileName || FILE_NAME_DEFAULT;
    }

    public function set fileName(value:String):void
    {
        _fileName = value;
    }

    public function downloadFile():void
    {
        if (url)
        {
            var downloadURL:URLRequest = new URLRequest(url);
            var file:FileReference = new FileReference();
            var fileNameWithExtension:String = fileName + getFileExtension(url);
            file.download(downloadURL, fileNameWithExtension);
        }
    }

    private function getFileExtension(url:String):String
    {
        var fileExtension:String = "";
        var fileExtensionIndex:int = url.search(/.[\w]+$/g);
        if (fileExtensionIndex > 0)
        {
            fileExtension += url.substr(fileExtensionIndex, url.length);
        }

        return fileExtension;
    }
}

}
