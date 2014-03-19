package pdf2html.src.bean;

public class Coordinate //Differentiates lines based on Y-coordinate.
{
	//private Double textAdjXPos; //Using Double objects for equality purposes.
	private String textAdjYPos;
	
	String getTextAdjYPos() {
		return textAdjYPos;
	}
	
	public int hashCode()
	{
		String addition = textAdjYPos;
		int hash = 7;
		for(int i = 0; i < addition.length(); i++)
			hash = hash*31 + addition.charAt(i);
		return hash;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof Coordinate)
		if(o instanceof Double)
		{
			Coordinate c = (Coordinate)o;
			if(this.textAdjYPos.equals(c.getTextAdjYPos())) //Make it so that Coordinate objects on the same line (same Y coord) are equal.
				return true; //This will reduce the number of objects in the LinkedHashMap.
		}
		return false;
	}
	
}
