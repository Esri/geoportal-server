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
package widgets.Coordinate
{

//--------------------------------------
//  Other metadata
//--------------------------------------
/**
 * Utility class to pretty print decimal degree numbers.
 * @private
 */
public final class DegToDMS
{
    // Constants to define the format.
    public static const LAT:String = "lat";

    public static const LON:String = "lon";

    /**
     * Utility function to format a decimal degree number into a pretty string with degrees, minutes and seconds.
     * @param decDeg the decimal degree number.
     * @param decDir "lat" for a latitude number, "lon" for a longitude value.
     * @return A pretty print string with degrees, minutes and seconds.
     */
    public static function format(decDeg:Number, decDir:String):String
    {
        var d:Number = Math.abs(decDeg);
        var deg:Number = Math.floor(d);
        d = d - deg;
        var min:Number = Math.floor(d * 60);
        var av:Number = d - min / 60;
        var sec:Number = Math.floor(av * 60 * 60);
        if (sec == 60)
        {
            min++;
            sec = 0;
        }
        if (min == 60)
        {
            deg++;
            min = 0;
        }
        var smin:String = min < 10 ? "0" + min + "' " : min + "' ";
        var ssec:String = sec < 10 ? "0" + sec + "\" " : sec + "\" ";
        var sdir:String = (decDir == LAT) ? (decDeg < 0 ? "S" : "N") : (decDeg < 0 ? "W" : "E");
        return deg + "\xB0 " + smin + ssec + sdir;
    }
}

}
