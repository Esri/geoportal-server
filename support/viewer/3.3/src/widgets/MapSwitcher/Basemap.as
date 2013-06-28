package widgets.MapSwitcher
{

public class Basemap
{
    public var id:String;
    public var thumbnail:String;
    public var label:String;
    public var visible:Boolean;

    public function Basemap(id:String, label:String, thumbnail:String = null, visible:Boolean = false)
    {
        this.id = id;
        this.label = label;
        this.thumbnail = thumbnail;
        this.visible = visible;
    }
}
}
