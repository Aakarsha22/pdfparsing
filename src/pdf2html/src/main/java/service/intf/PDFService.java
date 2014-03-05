package pdf2html.src.main.java.service.intf;

import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;

public interface PDFService {

	public PDDocument load(InputStream input, String password);

}
