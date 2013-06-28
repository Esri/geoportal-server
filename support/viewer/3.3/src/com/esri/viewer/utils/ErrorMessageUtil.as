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
package com.esri.viewer.utils
{

import mx.rpc.Fault;

/**
 * Utility class for mapping error codes with application error messages.
 */
public class ErrorMessageUtil
{
    public static function getKnownErrorCauseMessage(fault:Fault):String
    {
        var message:String;

        switch (fault.faultCode)
        {
            case "Channel.Security.Error":
            {
                message = LocalizationUtil.getDefaultString("serverMissingCrossDomain");
                break;
            }
            case "Server.Error.Request":
            case "400":
            case "404":
            {
                message = LocalizationUtil.getDefaultString("serviceIsInaccessible");
                break;
            }
            case "499":
            {
                message = LocalizationUtil.getDefaultString("unauthorizedAccess");
                break;
            }
            case "403":
            {
                message = LocalizationUtil.getDefaultString("resourceAccessDenied");
                break;
            }
            default:
            {
                message = (fault.faultString == "Sign in aborted") ?
                    LocalizationUtil.getDefaultString("signInAborted") : LocalizationUtil.getDefaultString("unknownErrorCause");
            }
        }

        return message;
    }

    public static function makeHTMLSafe(content:String):String
    {
        content = content.replace(/>/g, "&gt;");
        content = content.replace(/</g, "&lt;");
        return content;
    }

    public static function buildFaultMessage(fault:Fault):String
    {
        var faultMessage:String = "";

        if (fault.faultCode)
        {
            faultMessage += LocalizationUtil.getDefaultString('faultCode', fault.faultCode) + "\n\n";
        }

        if (fault.faultString)
        {
            faultMessage += LocalizationUtil.getDefaultString('faultInfo', fault.faultString) + "\n\n";
        }

        if (fault.faultDetail)
        {
            faultMessage += LocalizationUtil.getDefaultString('faultDetail', fault.faultDetail);
        }

        return faultMessage;
    }
}

}
