package tulli.com.br.scrapingtool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class AppTest {

//	@Autowired
//	private ScrapingService scrapingService;

	@Test
	public void shouldTellIfisDirectory() {
		FileDetail file1 = new FileDetail("gabrieltulli/scraping/tree/main/src");
		assertTrue(file1.isFolder());
	}

	@Test
	public void shouldTellIfisNotDirectory() {
		FileDetail file1 = new FileDetail("gabrieltulli/scraping/blob/main/src/test/java/tulli/com/br/scrapingTool/AppTest.java");
		assertFalse(file1.isFolder());
	}

	@Test
	public void shouldGetExtensionWithDot() {
		FileDetail file1 = new FileDetail("gabrieltulli/scraping/blob/main/src/test/java/tulli/com/br/scrapingTool/AppTest.java");
		assertEquals(".java", file1.getExtension());
	}

	@Test
	public void shouldGetExtensionWithoutDot() {
		FileDetail file1 = new FileDetail("gabrieltulli/scraping/blob/main/Procfile");
		assertEquals("Procfile", file1.getExtension());
	}

	@Test
	public void directoryDontHaveExtension() {
		FileDetail file1 = new FileDetail("gabrieltulli/scraping/tree/main/src");
		assertNotEquals("src", file1.getExtension());
	}

	@Test
	public void shouldGetTotalLinesCorrect() {
		HashMap<String, Result> result = makeASumary();
		assertEquals(13, result.get(".java").getTotalLines());
	}

	@Test
	public void shouldGetTotalSizeCorrect() {
		HashMap<String, Result> result = makeASumary();
		assertEquals(20.0 * 1024 + 10, result.get(".java").getTotalSize(), 0);
	}

	@Test
	public void shouldGetTotalSizeIncorrect() {
		HashMap<String, Result> result = makeASumary();
		assertNotEquals(20.0 * 1000 + 10, result.get(".java").getTotalSize(), 0);
	}

//	@Test
//	public void shouldFecthDataFromRepository() {
//		ArrayList<FileDetail> files = new ArrayList<>();
//		scrapingService.extractFilesFromRepository("gabrieltulli/oneoffcodes");
//		assertTrue(files.size() > 0);
//	}

	public HashMap<String, Result> makeASumary() {
		ArrayList<FileDetail> list = makeList();

		HashMap<String, Result> summary = new HashMap<String, Result>();
		for (FileDetail file : list) {
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
		return summary;
	}

	private ArrayList<FileDetail> makeList() {
		FileDetail file1 = new FileDetail("filename.java");
		file1.setLines(10);
		file1.setSize("20 KB");

		FileDetail file2 = new FileDetail("filename2.java");
		file2.setLines(3);
		file2.setSize("10 Bytes");

		ArrayList<FileDetail> list = new ArrayList<FileDetail>();
		list.add(file1);
		list.add(file2);
		return list;
	}

}
