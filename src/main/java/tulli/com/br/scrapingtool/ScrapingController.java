package tulli.com.br.scrapingtool;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	public Map<String, Result> scrapingGit(@PathVariable("user") String user, @PathVariable("repository") String repository) throws URISyntaxException, InterruptedException {
		long start = System.currentTimeMillis();
		String path = user + "/" + repository;

		LOG.info("start on " + path);

		Map<String, String> pong = new HashMap<>();
		pong.put("git", path);

		String body = scrapingService.getBodyFromUrl(path + "/file-list/master");

		ArrayList<FileDetail> filesDetail = getFilesFromPage(body);
		processFiles(filesDetail);

		HashMap<String, Result> summary = new HashMap<>();
		for (FileDetail file : filesDetail) {
			if (!file.isFolder()) {
				if (summary.containsKey(file.getExtension())) {
					summary.get(file.getExtension()).addTotalLines(file.getLines());
					summary.get(file.getExtension()).addTotalSize(file.getSize());
				} else {
					summary.put(file.getExtension(), new Result(file.getLines(), file.getSize()));
				}
			}
		}
		LOG.info(System.currentTimeMillis() - start);
		return summary;
	}

	private ArrayList<FileDetail> getFilesFromPage(String body) {
		String[] teste = body.split(" <div role=\"gridcell\" class=\"mr-3 flex-shrink-0\" style=\"width: 16px;\">");
		LOG.info("searching for files in page; " + teste.length);
		ArrayList<FileDetail> filesDetail = new ArrayList<>();
		for (int i = 1; i < teste.length; i++) {
			int hrefPos = teste[i].indexOf("href");
			int finalPos = teste[i].indexOf("\">", hrefPos);
			String fileName = teste[i].substring(hrefPos + 6, finalPos);

			FileDetail fileDetail = new FileDetail(fileName);

			filesDetail.add(fileDetail);
		}
		return filesDetail;
	}

	private void processFiles(ArrayList<FileDetail> filesDetail) throws URISyntaxException, InterruptedException {
		for (int i = 0; i < filesDetail.size(); i++) {
			if (filesDetail.get(i).isProcessed()) {
				continue;
			}
			if (filesDetail.get(i).isFolder()) {
				String newHtml = scrapingService.getBodyFromUrl(filesDetail.get(i).getFileName().replace("/tree/", "/file-list/"));
				filesDetail.addAll(getFilesFromPage(newHtml));
				filesDetail.get(i).setProcessed(true);
			} else {
				String data = scrapingService.getBodyFromUrl(filesDetail.get(i).getFileName());
				filesDetail.get(i).setLines(getLinesFromBody(data));
				filesDetail.get(i).setSize(getSizeFromBody(data));
				filesDetail.get(i).setProcessed(true);
			}
		}
	}

	private int getLinesFromBody(String body) {
		String[] lines = body.split("Box-header py-2 d-flex flex-column flex-shrink-0 flex-md-row flex-md-items-center")[1].split("\n");
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
		String[] lines = body.split("Box-header py-2 d-flex flex-column flex-shrink-0 flex-md-row flex-md-items-center")[1].split("\n");
		for (int i = 0; i < lines.length; i++) {
			String aux = lines[i].trim();
			if (aux.length() > 1 && aux.matches("^\\d+\\.?\\d+\\s+[MKBytes]+")) {
				return aux;
			}
		}
		return 0 + "";
	}

}
