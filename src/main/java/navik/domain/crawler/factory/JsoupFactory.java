package navik.domain.crawler.factory;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class JsoupFactory {

	public Document createDocument(String url) {
		try {
			return Jsoup.connect(url)
				.userAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
				.get();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
