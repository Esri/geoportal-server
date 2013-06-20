
	////////////////////////////////////////////////////////////////////////////////
	//
	// Version 1.0 - Dec 17, 2010
	//
	// Delevoped by Robert Scheitlin
	//
	////////////////////////////////////////////////////////////////////////////////
	
package widgets.Route
{
	import org.alivepdf.fonts.*;
	import org.alivepdf.layout.Size;
	import org.alivepdf.pdf.PDF;
	import org.alivepdf.colors.*;
	
	public class RoutePDF extends PDF
	{
		private var HeaderText:String = "";
		private var HeaderText2:String = "";
		
		public function RoutePDF(orientation:String="Portrait", unit:String="Mm", pageSize:Size=null, rotation:int=0, headerText:String="", headerText2:String="")
		{
			HeaderText = headerText + " PREVIEW VERSION";
			HeaderText2 = headerText2;
			setAliasNbPages ('{nb}');

			super(orientation, unit, pageSize, rotation);
		}
		
		override protected function header():void
		{
			var myCoreFont:IFont = new CoreFont(FontFamily.HELVETICA_BOLD);
			this.setFont(myCoreFont,12);
			var pInsideMar:Number = this.getMargins().right - this.getMargins().left;
			this.addCell(pInsideMar,10,HeaderText,0,0,'C');
			if(this.nbPages == 1){
				this.setXY(this.getMargins().left, 15);
				myCoreFont = new CoreFont(FontFamily.HELVETICA);
				this.setFont(myCoreFont,10);
				this.addCell(pInsideMar,10,HeaderText2,0,0,'C');
				this.newLine(10);
			}else{
				//Reset Font
				myCoreFont = new CoreFont(FontFamily.HELVETICA);
				this.setFont(myCoreFont,12);
				this.newLine(15);
			}
		} 
		
		override protected function footer():void
		{
			this.setXY (0, -20);
			this.newLine(5);
			var newFont:CoreFont = new CoreFont ( FontFamily.HELVETICA_OBLIQUE );
			this.setFont(newFont, 8);
			var totalW: Number = this.currentPage.w - this.leftMargin - this.rightMargin;
			this.addCell(totalW/2,6,'Page ' + this.nbPages + ' of {nb}',0,0,'L');
			this.addCell(totalW/2,6,'Route Widget PREVIEW VERSION',0,0,'R');
		}
	}
}