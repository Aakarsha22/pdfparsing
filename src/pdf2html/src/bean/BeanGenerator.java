package pdf2html.src.bean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.*;

public class BeanGenerator 
{
	StringBuilder beanGen;
	Pattern character;
	Pattern imageName;
	Pattern imagePos;
	Pattern imageSize;
	
	ArrayList<CharBean> charBeanList; //Will use this as a placeholder.
	private LinkedHashMap<Coordinate, ArrayList<CharBean>> beanMap = new LinkedHashMap<Coordinate, ArrayList<CharBean>>();
	
	public BeanGenerator(String s)
	{
		beanGen = new StringBuilder(s);
		character = Pattern.compile(RegexHelper.charRegex, Pattern.UNICODE_CHARACTER_CLASS);
	}
	
	public LinkedHashMap<Coordinate, ArrayList<CharBean>> getBeanMap() {
		return beanMap;
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
		
		try 
		{
			while((getScan = io.readLine()) != null)
			{
				Matcher m = character.matcher(getScan);
				if(m.find())
				{
					Coordinate coord = new Coordinate(m.group(5));
					groups = m.group(3) + "," + m.group(5) + "," + m.group(8)
							+ "," + m.group(11) + "," + m.group(14) + ","
							+ m.group(17) + "," + m.group(20) + "," + m.group(22);
					if(getBeanMap().containsKey(coord)) //If the map already contains this coordinate object
					{
						getBeanMap().get(coord).add(new CharBean(groups)); //Simply add the CharBean to the ArrayList indexed by this Coordinate object.
					}
					else
					{
						charBeanList = new ArrayList<CharBean>();
						charBeanList.add(new CharBean(groups));
						getBeanMap().put(coord, charBeanList);
					}
				}
				groups = "";
			}
			System.out.print("Pointless debug statement");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static class RegexHelper
	{
		static String words = "((?:[a-z][a-z]+))";
		static String singleChar = "(.)";
		static String floatNum = "([+-]?\\d*\\.\\d+)(?![-+0-9.])";
		static String space = "\\s";
		static String spacePlus = "\\s+";
		static String numPlus = "\\d+";
		static String singleCharOrSpace = "([\\s\\w])";
		static String alphaNum = "((?:[a-z][a-z]*[0-9]+[a-z0-9]*))";
		
		
		
		static String charRegex = words + singleChar + floatNum
				+ singleChar + floatNum + space + words 
				+ singleChar + floatNum + space + words
				+ singleChar + floatNum + space + words
				+ singleChar + floatNum + space + words
				+ singleChar + floatNum + space + words
				+ singleChar + floatNum + singleChar + singleCharOrSpace;
		
		static String imageName = words + spacePlus + words + spacePlus
									+ singleChar + alphaNum + singleChar;
		
		static String imagePosition = words + spacePlus + singleChar 
									+ spacePlus + floatNum + singleChar
									+ spacePlus + floatNum;
		static String imageSize = words + spacePlus + singleChar + spacePlus
								+ numPlus + words + singleChar + singleChar 
								+ numPlus + words;
		
	}
}
