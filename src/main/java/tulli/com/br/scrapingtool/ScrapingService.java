package tulli.com.br.scrapingtool;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ScrapingService {

	private static final Logger LOG = Logger.getLogger(ScrapingService.class);

	@Cacheable("bodies")
//	@Async
	public String getBodyFromUrl(String path) throws URISyntaxException {
		LOG.info("geting page: " + path);
		RestTemplate restTemplate = new RestTemplate();
		URI uri;
		try {
			uri = new URI("https://github.com/" + path);
			return restTemplate.getForEntity(uri, String.class).getBody();
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				// if the request gets 404 so the working branch is "main" instead of "master"
				path = path.replace("master", "main");
				uri = new URI("https://github.com/" + path);
				return restTemplate.getForEntity(uri, String.class).getBody();
			}
		}
		return "";
	}
}
