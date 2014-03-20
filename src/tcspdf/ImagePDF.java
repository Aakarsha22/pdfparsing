package tcspdf;
import java.util.*;
import java.io.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.PDFText2HTML;


public class ImagePDF 
{
	public static void main(String[] args) {
		   ImagePDF obj = new ImagePDF();
		    try {
		        //obj.read_pdf(); //Image output method.
		    	obj.getFontProperties(); //Trying for fonts.
		    	//if(Boolean.parseBoolean(args[0]) == true)
		    		//writeToHTML();
		    } catch (Exception ex) {
		        System.out.println("" + ex);
		    }

		}

		 private static void writeToHTML() 
		 {
			 File file = new File("/home/pritishc/Documents/ToHTML/ToHTML.html");
			 PrintWriter output = null;
			 PDDocument document = null;
			 try
			 {
				 document = PDDocument.load("/home/pritishc/Documents/Sample text.pdf");
				 output = new PrintWriter(file);
				 PDFTextStripper stripper = new PDFText2HTML("");
				 stripper.writeText(document, output);
				 output.flush();
				 output.close();
				 document.close();
			 }
			 catch(Exception e)
			 {
				 System.err.println(e.getMessage());
			 }
		 }

		void read_pdf() throws IOException {
		    PDDocument document = null; 
		    try {
		        document = PDDocument.load("lib/Sample text.pdf");
		    } catch (IOException ex) {
		        System.out.println("" + ex);
		    }
		    List pages = document.getDocumentCatalog().getAllPages();
		    Iterator iter = pages.iterator(); 
		    int i =1;
		    String name = null;

		    while (iter.hasNext()) {
		        PDPage page = (PDPage) iter.next();
		        PDResources resources = page.getResources();
		        Map pageImages = resources.getImages();
		        if (pageImages != null) { 
		            Iterator imageIter = pageImages.keySet().iterator();
		            while (imageIter.hasNext()) {
		                String key = (String) imageIter.next();
		                PDXObjectImage image = (PDXObjectImage) pageImages.get(key);
		                image.write2file("output/ImageIO/image" + i);
		                i ++;
		            }
		        }
		    }
		    document.close();
		 }
		 
		 void getFontProperties() throws Exception
		 {
			 Map<String, PDFont> pageFonts = new HashMap<String, PDFont>();
			 PDDocument pd = PDDocument.load("lib/Sample text.pdf");
			 PDFTextStripper ts = new PDFTextStripper();
			 System.out.println(ts.getText(pd));
			 @SuppressWarnings("unchecked")
			 List<PDPage> pages = pd.getDocumentCatalog().getAllPages();
			 for(PDPage page : pages)
			 {
				 pageFonts = page.getResources().getFonts();
				 for(Map.Entry<String, PDFont> entry : pageFonts.entrySet())
					 System.out.println(entry.getValue().getBaseFont());
			 }
			 //System.out.println("\n" + pageFonts.toString());
			 pd.close();
		 }
		 
		 
}
