package pdf2html.src.bean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.*;

public class BeanGenerator 
{
	StringBuilder beanGen;
	String[] lines;
	Pattern character;
	Pattern image;
	
	ArrayList<CharBean> charBeanList = new ArrayList<CharBean>();
	
	public BeanGenerator(String s)
	{
		beanGen = new StringBuilder(s);
		character = Pattern.compile(RegexHelper.charRegex);
	}
	
	public BeanGenerator()
	{
		beanGen = new StringBuilder("");
	}
	
	public void makeBeans()
	{
		String groups = "";
		String getScan = null;
		//lines = beanGen.split("\\r?\\n"); //Get all lines in an array.
		//for(int i = 0; i < lines.length; i++)
		//Scanner scan = new Scanner(beanGen.toString());
		//scan.useDelimiter("^" + character.pattern());
		BufferedReader io = new BufferedReader(new StringReader(beanGen.toString()));
		
		try {
			while((getScan = io.readLine()) != null)
			{
				Matcher m = character.matcher(getScan);
				if(m.find())
				{
					groups = m.group(3) + "," + m.group(5) + "," + m.group(8)
							+ "," + m.group(11) + "," + m.group(14) + ","
							+ m.group(17) + "," + m.group(20) + "," + m.group(22);
					charBeanList.add(new CharBean(groups));
				}
				groups = "";
			}
		} catch (IllegalArgumentException
				| IllegalAccessException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static class RegexHelper
	{
		static String words = "((?:[a-z][a-z]+))";
		static String singleChar = "(.)";
		static String floatNum = "([+-]?\\d*\\.\\d+)(?![-+0-9\\.])";
		static String space = "\\s";
		
		static String charRegex = words + singleChar + floatNum
				+ singleChar + floatNum + space + words 
				+ singleChar + floatNum + space + words
				+ singleChar + floatNum + space + words
				+ singleChar + floatNum + space + words
				+ singleChar + floatNum + space + words
				+ singleChar + floatNum + singleChar + singleChar;
	}
}
