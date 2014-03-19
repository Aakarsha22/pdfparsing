package pdf2html.src.bean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.*;

import pdf2html.src.bean.BeanGenerator.RegexHelper;

public class ImageGenerator 
{
	StringBuilder imgGen;
	Pattern character;
	Pattern image;
	
	ArrayList<ImageBean> imageBeanList; //Will use this as a placeholder.
	private LinkedHashMap<Coordinate, ArrayList<ImageBean>> beanMap = new LinkedHashMap<Coordinate, ArrayList<ImageBean>>();
	
	public ImageGenerator(String s)
	{
		imgGen = new StringBuilder(s);
		character = Pattern.compile(RegexHelper.charRegex);
	}

}
