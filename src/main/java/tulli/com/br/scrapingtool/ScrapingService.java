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

	public String getBodyFromUrl(String path) throws Exception {
		LOG.info(MessageFormat.format("geting page: {0}", path));
		RestTemplate restTemplate = new RestTemplate();
		URI uri;
		try {
			uri = new URI("https://github.com/" + path);
			return restTemplate.getForEntity(uri, String.class).getBody();
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				throw new Exception(MessageFormat.format("status code: {0}", ex.getStatusCode()));
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	private BodyHtml getBodyFromUrlRetry(String path) {
		boolean error = false;
		long timeout = 50;
		int tries = 0;
		do {
			try {
				return new BodyHtml(getBodyFromUrl(path));
			} catch (Exception e) {
				LOG.error(e.getMessage());
				error = true;
				try {
					Thread.sleep(timeout * tries);
				} catch (InterruptedException e1) {
					LOG.error("Error on getting page", e1);
					Thread.currentThread().interrupt();
				}
				tries++;
			}
		} while (error);
		return new BodyHtml();
	}

	@Cacheable("files")
	public List<FileDetail> extractFilesFromRepository(String path) {
		LOG.info(MessageFormat.format("geting files from: {0}", path));
		String defaultBranch = getDefaultBranch(path);

		ArrayList<FileDetail> filesDetail = new ArrayList<>();

		BodyHtml body = getBodyFromUrlRetry(path + "/file-list/" + defaultBranch);
		filesDetail.addAll(body.getFiles());

		processFiles(filesDetail);

		for (FileDetail temp : filesDetail) {
			if (!temp.isProcessed()) {
				LOG.info(temp.toString());
			}
		}
		return filesDetail;
	}

	private String getDefaultBranch(String path) {
		String mainPage = getBodyFromUrlRetry(path).getBody();
		int startPos = mainPage.indexOf("commits/");
		return mainPage.substring(startPos + 8, mainPage.indexOf(".atom", startPos));
	}

	private void processFiles(ArrayList<FileDetail> filesDetail) {
		for (int i = 0; i < filesDetail.size(); i++) {
			if (filesDetail.get(i).isProcessed()) {
				continue;
			}
			if (filesDetail.get(i).isFolder()) {
				BodyHtml page = getBodyFromUrlRetry(filesDetail.get(i).getDirectoryName());
				filesDetail.addAll(page.getFiles());
				filesDetail.get(i).setProcessed(true);
			} else {
				BodyHtml data = getBodyFromUrlRetry(filesDetail.get(i).getFileName());
				filesDetail.get(i).setLines(data.getLines());
				filesDetail.get(i).setSize(data.getSize());
				filesDetail.get(i).setProcessed(true);
			}
		}
	}

}
