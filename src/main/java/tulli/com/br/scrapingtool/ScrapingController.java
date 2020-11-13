package tulli.com.br.scrapingtool;

import java.text.MessageFormat;
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
	public Map<String, Result> scrapingGit(@PathVariable("user") String user, @PathVariable("repository") String repository) {
		long start = System.currentTimeMillis();
		String path = user + "/" + repository;
		LOG.info("start on " + path);

		ArrayList<FileDetail> filesDetail = new ArrayList<>();
		filesDetail.addAll(scrapingService.extractFilesFromRepository(path));

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

}