package pdf2html.src.bean;
import java.lang.reflect.Field;

public class CharBean 
{
	double textAdjXPos;
	double textAdjYPos;
	double fontSize;
	double xScale;
	double textHeight;
	double textSpace;
	double textWidth;
	String character;
	
	public CharBean(String container) throws NumberFormatException, IllegalArgumentException, IllegalAccessException
	{
		String[] values = container.split(",");
		Field[] allFields = CharBean.class.getDeclaredFields();
		
		int i = 0;
		for(i = 0; i < values.length - 1; i++)
		{
			allFields[i].set(this, Double.parseDouble(values[i]));
		}
		allFields[++i].set(this, values[i]);
	}
	
}
