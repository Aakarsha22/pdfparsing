package pdftosvgtohtml;

import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class PDF2SVGBatik 
{
	public static void main(String... args)
	{
		try
		{
			generateSVG(new File("/media/Data/using color error diffusion.pdf"), "/media/Data/PDFtoSVG/");
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	static void generateSVG(File pdfFile, String svgDir) throws IOException, PrinterException
	{
		PDDocument document = PDDocument.load( pdfFile );
		DOMImplementation domImpl =
		    GenericDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document svgDocument = domImpl.createDocument(svgNS, "svg", null);
		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(svgDocument);
		ctx.setEmbeddedFontsOn(true);

		// Ask the test to render into the SVG Graphics2D implementation.

		    for(int i = 0 ; i < document.getNumberOfPages() ; i++){
		        String svgFName = svgDir+"page"+i+".svg";
		        (new File(svgFName)).createNewFile();
		        // Create an instance of the SVG Generator.
		        SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx,false);
		        Printable page  = document.getPrintable(i);
		        page.print(svgGenerator, document.getPageFormat(i), i);
		        svgGenerator.stream(svgFName);
		    }
	}
}
