package pdf2html.src.bean;
//import java.lang.reflect.Field;

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
	
	public CharBean(String container)
	{
		String[] values = container.split("_,_");
		//Field[] allFields = CharBean.class.getDeclaredFields();
		
		int i = 0;
		/*for(i = 0; i < values.length - 1; i++)
		{
			allFields[i].set(this, Double.parseDouble(values[i]));
		}
		allFields[++i].set(this, values[i]);*/
		this.textAdjXPos = Double.parseDouble(values[i++]);
		this.textAdjYPos = Double.parseDouble(values[i++]);
		this.fontSize = Double.parseDouble(values[i++]);
		this.xScale = Double.parseDouble(values[i++]);
		this.textHeight = Double.parseDouble(values[i++]);
		this.textSpace = Double.parseDouble(values[i++]);
		this.textWidth = Double.parseDouble(values[i++]);
		this.character = values[i];
	}
	
}
