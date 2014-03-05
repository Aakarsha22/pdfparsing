package pdf2html.src.main.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;

import pdf2html.src.main.java.pdf2html.util.PDF2HTMLException;
import pdf2html.src.main.java.service.impl.PDF2HTMLServiceImpl;
import pdf2html.src.main.java.service.intf.PDF2HTMLService;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		ApplicationContext appContext = new ClassPathXmlApplicationContext(
//				"applicationContext.xml");
//
//		PDF2HTMLService service = appContext.getBean(PDF2HTMLService.class);
		PDF2HTMLServiceImpl service = new PDF2HTMLServiceImpl(); 
		try {
			InputStream inputStream = new FileInputStream("/home/pritishc/Documents/Sample text.pdf");
			String output = service.convertPage(inputStream, 2);
			FileUtils.writeStringToFile(new File("generated.html"), output);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
