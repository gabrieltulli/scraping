package tulli.com.br.scrapingtool;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ScrapingService {

	private static final Logger LOG = Logger.getLogger(ScrapingService.class);

	@Cacheable("files")
	public List<FileDetail> extractFilesFromRepository(String path) {
		LOG.info(MessageFormat.format("geting files from: {0}", path));
		String defaultBranch = getDefaultBranch(path);

		ArrayList<FileDetail> filesDetail = new ArrayList<>();

		BodyHtml body = getBodyFromUrlWithRetry(path + "/file-list/" + defaultBranch);
		filesDetail.addAll(body.getFiles());

		processFiles(filesDetail);

		return filesDetail;
	}

	private String getDefaultBranch(String path) {
		String mainPage = getBodyFromUrlWithRetry(path).getBody();
		int startPos = mainPage.indexOf("commits/");
		return mainPage.substring(startPos + 8, mainPage.indexOf(".atom", startPos));
	}

	private BodyHtml getBodyFromUrlWithRetry(String path) {
		LOG.info(MessageFormat.format("geting page: {0}", path));
		RestTemplate restTemplate = new RestTemplate();
		URI uri;

		boolean error = false;
		long timeout = 50;
		int tries = 0;
		do {
			try {
				uri = new URI("https://github.com/" + path);
				return new BodyHtml(restTemplate.getForEntity(uri, String.class).getBody());
			} catch (HttpClientErrorException e) {
				if (e.getStatusCode().is4xxClientError()) {
					LOG.error(MessageFormat.format("Error on getting page: status code: {0}", e.getStatusCode()));
				}
				error = true;
				sleep(timeout, tries);
				tries++;
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} while (error);
		return new BodyHtml();
	}

	private void processFiles(ArrayList<FileDetail> filesDetail) {
		for (int i = 0; i < filesDetail.size(); i++) {
			if (filesDetail.get(i).isProcessed()) {
				continue;
			}
			if (filesDetail.get(i).isFolder()) {
				BodyHtml page = getBodyFromUrlWithRetry(filesDetail.get(i).getDirectoryName());
				filesDetail.addAll(page.getFiles());
				filesDetail.get(i).setProcessed(true);
			} else {
				BodyHtml data = getBodyFromUrlWithRetry(filesDetail.get(i).getFileName());
				filesDetail.get(i).setLines(data.getLines());
				filesDetail.get(i).setSize(data.getSize());
				filesDetail.get(i).setProcessed(true);
			}
		}
	}

	private void sleep(long timeout, int tries) {
		try {
			Thread.sleep(timeout * tries);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

}
