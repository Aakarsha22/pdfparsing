package pdf2html.src.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class BeanToHTML 
{
	String newLine = System.getProperty("line.separator");
	StringBuilder HTML = new StringBuilder("<HTML>" + newLine);
	LinkedHashMap<Coordinate, ArrayList<CharBean>> beanMap;
	int pageNumber;
	
	public BeanToHTML(LinkedHashMap<Coordinate, ArrayList<CharBean>> LHM, int i)
	{
		beanMap = LHM;
		pageNumber = i;
	}
	
	public void createHTML()
	{
		//File HTMLfile = new File("output/HTML" + pageNumber + ".html");
		for(Map.Entry<Coordinate, ArrayList<CharBean>> entry : beanMap.entrySet())
		{
			
		}
	}
}
