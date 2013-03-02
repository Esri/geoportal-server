/*See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
Esri Inc. licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/ 
package com.esri.gpt.utils
{ 
/**
TM: 2q Copied from http://www.adobe.us/livedocs/flex/2/langref/String.html
**/  
public class StringHelper {
  
    public function StringHelper() {
    }

    public function replace(str:String, oldSubStr:String, newSubStr:String):String {
        return str.split(oldSubStr).join(newSubStr); 
    }

    public function trim(str:String):String {
      
        var char:String = '\r';
        str = trimBack(trimFront(str, char), char)
        char = '\n';
        str = trimBack(trimFront(str, char), char)
        char = '\r\n';
        str = trimBack(trimFront(str, char), char)
        char = ' ';
        str = trimBack(trimFront(str, char), char)
        
        return str;
    }

    public function trimFront(str:String, char:String):String {
        char = stringToCharacter(char);
        if (str.charAt(0) == char) {
            str = trimFront(str.substring(1), char);
        }
        return str;
    }

    public function trimBack(str:String, char:String):String {
        char = stringToCharacter(char);
        if (str.charAt(str.length - 1) == char) {
            str = trimBack(str.substring(0, str.length - 1), char);
        }
        return str;
    }

    public function stringToCharacter(str:String):String {
        if (str.length == 1) {
            return str;
        }
        return str.slice(0, 1);
    }
}
}