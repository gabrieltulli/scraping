package tulli.com.br.scrapingtool;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RestController
@EnableWebMvc
public class ScrapingController {

	@Autowired
	private ScrapingService scrapingService;

	private static final Logger LOG = Logger.getLogger(ScrapingController.class);

	@RequestMapping(path = "/scraping/{user}/{repository}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Result> scrapingGit(@PathVariable("user") String user, @PathVariable("repository") String repository) {
		long start = System.currentTimeMillis();
		String path = user + "/" + repository;
		LOG.info("start on " + path);

		String defaultBranch = getDefaultBranch(path);
		String body = getBodyFromUrl(path + "/file-list/" + defaultBranch);

		ArrayList<FileDetail> filesDetail = getFilesFromPage(body);
		processFiles(filesDetail);

		HashMap<String, Result> summary = new HashMap<>();
		for (FileDetail file : filesDetail) {
			if (!file.isFolder()) {
				if (summary.containsKey(file.getExtension())) {
					summary.get(file.getExtension()).addTotalLines(file.getLines());
					summary.get(file.getExtension()).addTotalSize(file.getSize());
					summary.get(file.getExtension()).addQuantity();
				} else {
					summary.put(file.getExtension(), new Result(file.getLines(), file.getSize()));
				}
			}
		}
		LOG.info(MessageFormat.format("duration: {0}ms", System.currentTimeMillis() - start));
		return summary;
	}

	private String getBodyFromUrl(String path) {
		String body = "";
		boolean error = false;
		long timeout = 50;
		int tries = 0;
		do {
			try {
				body = scrapingService.getBodyFromUrl(path);
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

	private String getDefaultBranch(String path) {
		String mainPage = getBodyFromUrl(path);
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
				String newHtml = getBodyFromUrl(filesDetail.get(i).getDirectoryName());
				filesDetail.addAll(getFilesFromPage(newHtml));
				filesDetail.get(i).setProcessed(true);
			} else {
				String data = getBodyFromUrl(filesDetail.get(i).getFileName());
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