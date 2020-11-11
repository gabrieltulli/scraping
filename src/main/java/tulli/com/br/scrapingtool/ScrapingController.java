package tulli.com.br.scrapingtool;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RestController
@EnableWebMvc
public class ScrapingController {
	@RequestMapping(path = "/scraping/{user}/{repository}", method = RequestMethod.GET)
	public Map<String, String> scrapingGit(@PathVariable("user") String user,
			@PathVariable("repository") String repository) throws IOException, URISyntaxException {
		String path = user + "/" + repository;

		System.out.println("start on " + path);

		Map<String, String> pong = new HashMap<>();
		pong.put("git", path);

		String body = getBodyFromUrl(path + "/file-list/master");

		for (FileDetail file : getFilesFromPage(body)) {
			System.out.println(file.toString());
		}

		return pong;
	}

	private String getBodyFromUrl(String path) throws URISyntaxException {
		System.out.println("geting page: " + path);
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

	private List<FileDetail> getFilesFromPage(String body) throws URISyntaxException {
		String[] teste = body.split(" <div role=\"gridcell\" class=\"mr-3 flex-shrink-0\" style=\"width: 16px;\">");

		ArrayList<FileDetail> filesDetail = new ArrayList<>();
		for (int i = 1; i < teste.length; i++) {
			int hrefPos = teste[i].indexOf("href");
			int finalPos = teste[i].indexOf("\">", hrefPos);
			String fileName = teste[i].substring(hrefPos + 6, finalPos);

			FileDetail fileDetail = new FileDetail(fileName);

			filesDetail.add(fileDetail);
		}

		for (int i = 0; i < filesDetail.size(); i++) {
			if (filesDetail.get(i).isProcessed()) {
				continue;
			}
			if (filesDetail.get(i).isFolder()) {
				String newHtml = getBodyFromUrl(filesDetail.get(i).getFileName().replace("/tree/", "/file-list/"));
				filesDetail.addAll(getFilesFromPage(newHtml));
				filesDetail.get(i).setProcessed(true);
			} else {
				String data = getBodyFromUrl(filesDetail.get(i).getFileName());
				filesDetail.get(i).setLines(getLinesFromBody(data));
				filesDetail.get(i).setSize(getSizeFromBody(data));
				filesDetail.get(i).setProcessed(true);
			}
		}
		return filesDetail;
	}

	private int getLinesFromBody(String body) {
		String[] lines = body
				.split("Box-header py-2 d-flex flex-column flex-shrink-0 flex-md-row flex-md-items-center")[1]
						.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String aux = lines[i].trim();
			if (aux.matches("^\\d+ lines.*")) {

				aux = aux.substring(0, aux.indexOf("lines")).trim();

				return Integer.parseInt(aux);
			}
		}
		return 0;
	}

	private String getSizeFromBody(String body) {
		String[] lines = body
				.split("Box-header py-2 d-flex flex-column flex-shrink-0 flex-md-row flex-md-items-center")[1]
						.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String aux = lines[i].trim();
			if (aux.length() > 1 && aux.matches("^\\d+\\.?\\d+\\s+[MKBytes]+")) {
				return aux;
			}
		}
		return 0 + "";
	}
}
