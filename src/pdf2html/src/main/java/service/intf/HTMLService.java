package pdf2html.src.main.java.service.intf;

import java.awt.image.BufferedImage;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public interface HTMLService {

	public Document createDocument(String id, int width, int height);

	public Element addText(Document document, String text, String id, int width,
			int height, float direction, float fontSize, float x, float y,
			String fontFamily, int fontWeight, boolean italic, int foreRed,
			int foreGreen, int foreBlue, int backRed, int backGreen,
			int backBlue);

	public Element addImage(Document document, BufferedImage image, String id,
			int width, int height, int x, int y);

}
