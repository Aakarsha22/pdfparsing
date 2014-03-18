package pdf2html.src.bean;

public class Coordinate //Differentiates lines based on Y-coordinate.
{
	//private Double textAdjXPos; //Using Double objects for equality purposes.
	private Double textAdjYPos;
	
	public Coordinate(double Y)
	{
		this.textAdjYPos = Y;
	}
	
	/*double getTextAdjXPos() {
		return textAdjXPos;
	}
	void setTextAdjXPos(double textAdjXPos) {
		this.textAdjXPos = textAdjXPos;
	}*/
	double getTextAdjYPos() {
		return textAdjYPos;
	}
	void setTextAdjYPos(double textAdjYPos) {
		this.textAdjYPos = textAdjYPos;
	}
	
	public int hashCode()
	{
		String addition = Double.toString(textAdjYPos);
		int hash = 7;
		for(int i = 0; i < addition.length(); i++)
			hash = hash*31 + addition.charAt(i);
		return hash;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof Double)
		{
			Coordinate c = (Coordinate)o;
			if(this.textAdjYPos.equals(c.getTextAdjYPos())) //Make it so that Coordinate objects on the same line (same Y coord) are equal.
				return true; //This will reduce the number of objects in the LinkedHashMap.
		}
		return false;
	}
	
}
