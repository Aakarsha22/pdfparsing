package pdftohtml;

/*
* ==============================
* PDF to HTML Conversion Project
* ==============================
*
* Project Info:  http://tamirhassan.com/project/
* Project Lead:  Tamir Hassan, University of Warwick, UK
*
* Submitted as CS310 Third Year Project
*
* ----------------
* PdfGrouping.java
* ----------------
*
* Original Author:  Tamir Hassan (project@tamirhassan.com)
*
* Last Modified: 30 April 2003
* --------------------------
*/

import java.awt.image.*;
import java.io.*;
import org.jpedal.*;
import org.jpedal.gui.*;
import org.jpedal.io.*;
import org.jpedal.objects.*;
import org.jpedal.utils.*;
import org.jpedal.grouping.*;

import org.jdom.*;
import org.jdom.output.XMLOutputter;
import java.util.*;

/**
 * This is the front end to the PDF-to-HTML converter.</P>
 *
 * It performs all the file accesses, input and output.  It
 * interfaces with PdfGrouping.processPageFragments to group
 * and merge the text objects before outputting the HTML file.
 *
 * If the -c command-line parameter is used, processPageFragments is
 * called with the parameter TRUE, instructing it to detect columns
 * and process the text accordingly
 */
public class pdf2html
{
	//////////////////////////////////////////////////////////////////////
	/**
	 * strips <~START> and <~END> tags from the supplied string
	 */
	public static String textOf(String content)
	{
		return content.substring(8, content.length()-6);
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * removes ".pdf" from the end of the string if it is there
	 */
	public static String withoutPdfExtension(String content)
	{
		int end = content.lastIndexOf(".pdf", content.length());
		if (end > 0) return content.substring(0, end); else return content;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * replaces all occurrences of typographical quotation marks and dashes
	 * with their multi-platform equivalents
	 */
	public static String replaceSymbols(String content)
	{
		// as dashes are replaced by spaced-out hyphens it is necessary to first
		// count the number of dashes so that the correct length string is returned
		int dashcount = 0;
		for (int n = 0; n < content.length(); n ++)
		{
			if (content.charAt(n) == 8211 || content.charAt(n) == 8212)
				dashcount ++;
		}

		char[] value = new char[content.length() + (2 * dashcount)];
		int position = 0;

		/* replace the symbols in their appropriate position */
		for (int n = 0; n < content.length(); n ++)
		{
			if (content.charAt(n) == 8216 || content.charAt(n) == 8217)
			{
				value[position] = '\'';
				position ++;
			}
			else if (content.charAt(n) == 8220 || content.charAt(n) == 8221)
			{
				value[position] = '"';
				position ++;
			}
			else if (content.charAt(n) == 8211 || content.charAt(n) == 8212)
			{
				value[position] = ' ';
				value[position + 1] = '-';
				value[position + 2] = ' ';
				position = position + 3;
			}
			else
			{
				value[position] = content.charAt(n);
				position ++;
			}
		}
		return new String(value);
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * main method: takes the command line parameters, performs
	 * the conversion and outputs the file
	 */
	public static void main(String args[])
	{
	  /* initialize variables */
	  String infile = "";
	  String outfile = "";
	  boolean decode_columns = false;
	  PdfGrouping pdf_grouping;

	  /* obtain code for newline */
	  Properties props = System.getProperties();
	  String newLine = props.getProperty("line.separator");

	  /* read command line arguments */
	  if (args.length == 0 || args.length > 3 || (args.length == 1 && args[0].equals(new String("-c")) || (args.length == 3 && args[0].equals(new String("-c")))))
	  {
		System.out.println("Usage: pdf2html [-c] infile [outfile]");
		System.exit(0);
	  }
	  else
	  if (args.length == 1)
	  {
		infile = args[0];
		outfile = withoutPdfExtension(args[0]) + ".html";
	  }
	  if (args.length == 2)
	  {
		if (args[0].equals(new String("-c")))
		{
			decode_columns = true;
			infile = args[1];
			outfile = withoutPdfExtension(args[1]) + ".html";
		}
		else
		{
			infile = args[0];
			outfile = args[1];
		}
	  }
	  if (args.length == 3)
	  {
		decode_columns = true;
		infile = args[1];
		outfile = args[2];
	  }

	  System.out.println("Using input file: " + infile);
	  System.out.println("Using output file: " + outfile);

	  File pdf_file = new File( infile );

	  StringBuffer html = new StringBuffer("<HTML>" + newLine + "<HEAD>" + newLine +
			      "<STYLE type=\"text/css\">" + newLine +
			      "h1 {font-family: helvetica; font-size:24}" + newLine +
			      "p {font-family: times}" + newLine +
			      "</STYLE>" + newLine + "</HEAD>" + newLine + "<BODY>");

	  PdfDecoder decode_pdf = null;
	  PdfData pdf_text;

	  /* PdfDecoder returns a PdfException if there is a problem */
	  try
	  {
	    decode_pdf = new PdfDecoder( false );

	    /**
	     * open the file (and read metadata including pages in file)
	    */
	    System.out.println( "Opening file :" + infile );
	    decode_pdf.openPdfFile( infile );
	  }
	  catch( Exception e )
	  {
	    System.err.println( "Exception " + e + " in pdf code" );
	    System.exit( 1 );
	  }

	  /* get page count */
	  int pageCount = decode_pdf.getPageCount();

	  /* for each page do: */
	  for (int page = 1; page <= pageCount; page ++)
	  {
		try
		{
			System.out.println("Decoding page " + page);
	  		decode_pdf.decodePage( false, page );
		}
		catch( Exception e )
	  	{
	    		System.err.println( "Exception " + e + " in pdf code" );
	    		System.exit( 1 );
	  	}

		if (pageCount > 1)
		{
			/* if there is more than one page append a header showing page number */
			html = html.append(newLine + "<table border=0 width=100%><tr><td bgcolor=eeeeee align=right>" + newLine);
			html = html.append("<font face=Arial, Helvetica, sans-serif><a name=1><b>Page " + page + "</b></a></font></td></tr></table>" + newLine);
		}

		/* obtain PDF data from library */
	  	pdf_text = decode_pdf.getPdfData();
		pdf_grouping = new PdfGrouping();

		/* call PdfGrouping to process the page fragments */
	  	pdf_grouping.processPageFragments( pdf_text, page, decode_columns );

	  	int items = pdf_text.getTextElementCount();

		/* extract each text element */
          	Element[] arraytext = new Element[items];
	  	for (int i = 0; i < items; i ++)
	  	{
	    		arraytext[i] = pdf_text.getTextElementAt( i );
	  	}

			boolean flag = false;
			int previous = 0;
			for (int i = 0; i < items; i ++)
			{
				Element current_line = arraytext[i];

				/* append content to HTML file */
				String content = current_line.getAttributeValue( "content" );
				String text = textOf(content);

				html = html.append(replaceSymbols(text));

			}

			/* remove data once written out */
			decode_pdf.flushObjectValues( false );
		}

		/* add closing tags at end */
		html = html.append("</BODY>" + newLine + "</HTML>");

		/* write to file */
		try
		{
			File outputFile = new File(outfile);
			FileWriter out = new FileWriter(outputFile);
			out.write(html.toString());
			out.close();
		}
		catch (Exception e)
		{
		}
	}
}

