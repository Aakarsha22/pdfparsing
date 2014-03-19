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
	StringBuilder HTML = new StringBuilder("<HTML>");
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
			HTML.append("<DIV style=\"position: absolute left: 0 top: " + entry.getKey().getTextAdjYPos() + " font-size: ");
			for(CharBean C : entry.getValue())
			{
				if(beanCounter == 1)
					HTML.append(C.fontSize + "\">");
				HTML.append(C.character);
				beanCounter++;
			}
			beanCounter = 1;
		}
		HTML.append("</HTML>");
	}
	
	public void writeHTML() throws IOException
	{
		createHTMLString();
		File htmlFile = new File("output/HTML" + pageNumber + ".html");
		PrintWriter pw = new PrintWriter(new FileWriter(htmlFile));
		pw.println(HTML.toString());
		System.out.println("Pointless debug again");
		pw.close();
	}
}
