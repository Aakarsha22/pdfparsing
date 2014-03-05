package pdf2html.src.main.java.service.intf;

import java.io.InputStream;

public interface PDF2HTMLService {

	public String convertPage(InputStream document, int page);
	
}
