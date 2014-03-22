package pdf2html.src.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class BeanToHTML 
{
	String newLine = "<BR>";
	StringBuilder HTML = new StringBuilder("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
             + "\"http://www.w3.org/TR/html4/loose.dtd\">" + "<HTML>");
	LinkedHashMap<Coordinate, ArrayList<CharBean>> beanMap;
	int pageNumber;
	
	public BeanToHTML(LinkedHashMap<Coordinate, ArrayList<CharBean>> LHM, int i)
	{
		beanMap = LHM;
		pageNumber = i;
	}
	
	void createHTMLString()
	{
		//File HTMLfile = new File("output/HTML" + pageNumber + ".html");
		int beanCounter = 1;
		for(Map.Entry<Coordinate, ArrayList<CharBean>> entry : beanMap.entrySet())
		{
			HTML.append("<FONT style=\"font-size: ");
			for(CharBean C : entry.getValue())
			{
				if(beanCounter == 1)
					HTML.append(C.fontSize + "pt;\">");
				HTML.append(C.character);
				beanCounter++;
			}
			beanCounter = 1;
			HTML.append("</FONT>" + newLine);
		}
		HTML.append("</HTML>");
	}
	
	public void writeHTML() throws IOException
	{
		createHTMLString();
		File htmlFile = new File("output/HTML" + pageNumber + ".html");
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(htmlFile), StandardCharsets.UTF_16));
		pw.write(HTML.toString());
		System.out.println("Pointless debug again");
		pw.close();
	}
}
