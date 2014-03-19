package pdf2html.src.main.java;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import pdf2html.src.bean.BeanGenerator;
import pdf2html.src.bean.BeanToHTML;

//import org.apache.commons.io.FileUtils;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;

//import pdf2html.src.main.java.pdf2html.util.PDF2HTMLException;
import pdf2html.src.main.java.service.impl.PDF2HTMLServiceImpl;
//import pdf2html.src.main.java.service.intf.PDF2HTMLService;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		ApplicationContext appContext = new ClassPathXmlApplicationContext(
//				"applicationContext.xml");
//
//		PDF2HTMLService service = appContext.getBean(PDF2HTMLService.class);
		String output = "";
		PDF2HTMLServiceImpl service = new PDF2HTMLServiceImpl(); 
		try {
			InputStream inputStream = new FileInputStream("lib/Sample text.pdf");
			output = captureStream(inputStream, service);
			System.out.print(output);
			//FileUtils.writeStringToFile(new File("generated.html"), output);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BeanGenerator bg = new BeanGenerator(output);
		bg.makeBeans();
		BeanToHTML bth = new BeanToHTML(bg.getBeanMap(), 2);
		try {
			bth.writeHTML();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//where is eclipse installed??
	/* Name: captureStream()
	 * Description: This method uses the FileInputStream which links to the PDF and the 
	 * PDF2HTMLServiceImpl object whose convertPage() method needs to be called
	 * and redirects the output to our PrintStream.
	 * In this manner, we are able to return the console output as a string.
	 * Output format: "String[TextAdjusted X-Position, TextAdjusted Y-Position, FontSize,
	 * XScale, MaximumHeightOfAllCharacters, WidthOfSpaceCharacter, TextDirectionAdjustedStringWidth,
	 * ]CharacterItself"  
	 */
	static String captureStream(InputStream fs, PDF2HTMLServiceImpl pdf) //Hook System.out.print to a different stream.
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		//Store the old stream to be reused later.
		PrintStream old = System.out;
		
		System.setOut(ps);
		pdf.convertPage(fs, 2);
		System.out.flush();
		System.setOut(old);
		return os.toString();
	}
}
