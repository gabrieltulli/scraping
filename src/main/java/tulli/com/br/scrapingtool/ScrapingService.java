package tulli.com.br.scrapingtool;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ScrapingService {

	private static final Logger LOG = Logger.getLogger(ScrapingService.class);

//	@Cacheable("bodies")
//	@Async
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

	private String getBodyFromUrlRetry(String path) {
		String body = "";
		boolean error = false;
		long timeout = 50;
		int tries = 0;
		do {
			try {
				body = getBodyFromUrl(path);
				error = false;
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
		return body;
	}

	@Cacheable("files")
	public List<FileDetail> extractFilesFromRepository(String path) {
		LOG.info(MessageFormat.format("geting files from: {0}", path));
		String defaultBranch = getDefaultBranch(path);
		String body = getBodyFromUrlRetry(path + "/file-list/" + defaultBranch);

		ArrayList<FileDetail> filesDetail = getFilesFromPage(body);
		processFiles(filesDetail);
		return filesDetail;
	}

	private String getDefaultBranch(String path) {
		String mainPage = getBodyFromUrlRetry(path);
		int startPos = mainPage.indexOf("commits/");
		return mainPage.substring(startPos + 8, mainPage.indexOf(".atom", startPos));
	}

	private ArrayList<FileDetail> getFilesFromPage(String body) {
		String[] teste = body.split(" <div role=\"gridcell\" class=\"mr-3 flex-shrink-0\" style=\"width: 16px;\">");
		LOG.info("searching for files in page; " + teste.length);
		ArrayList<FileDetail> listOfFiles = new ArrayList<>();
		for (int i = 1; i < teste.length; i++) {
			int hrefPos = teste[i].indexOf("href");
			int finalPos = teste[i].indexOf("\">", hrefPos);
			String fileName = teste[i].substring(hrefPos + 6, finalPos);

			listOfFiles.add(new FileDetail(fileName));
		}
		return listOfFiles;
	}

	private void processFiles(ArrayList<FileDetail> filesDetail) {
		for (int i = 0; i < filesDetail.size(); i++) {
			if (filesDetail.get(i).isProcessed()) {
				continue;
			}
			if (filesDetail.get(i).isFolder()) {
				String newHtml = getBodyFromUrlRetry(filesDetail.get(i).getDirectoryName());
				filesDetail.addAll(getFilesFromPage(newHtml));
				filesDetail.get(i).setProcessed(true);
			} else {
				String data = getBodyFromUrlRetry(filesDetail.get(i).getFileName());
				filesDetail.get(i).setLines(getLinesFromBody(data));
				filesDetail.get(i).setSize(getSizeFromBody(data));
				filesDetail.get(i).setProcessed(true);
			}
		}
	}

	private int getLinesFromBody(String body) {
		Pattern pattern = Pattern.compile("\\s*(\\d+) lines \\(.*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(body);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

	private String getSizeFromBody(String body) {
		Pattern pattern = Pattern.compile("\\s{4}(\\d+\\.?\\d+)\\s+([MKByte]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(body);
		if (matcher.find()) {
			return matcher.group(1) + " " + matcher.group(2);
		}
		return "0";
	}
}
