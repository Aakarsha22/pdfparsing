package pdf2html.src.bean;

public class ImageBean 
{

	double XPos;
	double YPos;
	double XSize;
	double YSize;
	String name;


public ImageBean(String container) 
{
	String[] values = container.split(",");
	int i=0;
	this.name=values[i];
	this.XPos=Double.parseDouble(values[i++]);
	this.YPos=Double.parseDouble(values[i++]);
	this.XSize=Double.parseDouble(values[i++]);
	this.YSize=Double.parseDouble(values[i++]);
	
}
}

