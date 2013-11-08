package com.esri.viewer
{

/**
 * The content of an error message window.
 */
public class ErrorMessage
{
    public function ErrorMessage(content:String, title:String)
    {
        _content = content;
        _title = title;
    }

    private var _content:String;

    public function get content():String
    {
        return _content;
    }

    private var _title:String;

    public function get title():String
    {
        return _title;
    }
}
}
