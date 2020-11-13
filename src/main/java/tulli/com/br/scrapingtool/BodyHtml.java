package tulli.com.br.scrapingtool;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BodyHtml {

	private String body;

	public BodyHtml(String body) {
		this.body = body;
	}

	public BodyHtml() {
		this.body = "";
	}

	public String getBody() {
		return this.body;
	}

	public List<FileDetail> getFiles() {
		String[] teste = getBody().split(" <div role=\"gridcell\" class=\"mr-3 flex-shrink-0\" style=\"width: 16px;\">");
		ArrayList<FileDetail> listOfFiles = new ArrayList<>();
		for (int i = 1; i < teste.length; i++) {
			int hrefPos = teste[i].indexOf("href");
			int finalPos = teste[i].indexOf("\">", hrefPos);
			String fileName = teste[i].substring(hrefPos + 6, finalPos);

			listOfFiles.add(new FileDetail(fileName));
		}
		return listOfFiles;
	}

	public int getLines() {
		Pattern pattern = Pattern.compile("\\s*(\\d+) lines \\(.*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(getBody());
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

	public String getSize() {
		Pattern pattern = Pattern.compile("\\s{4}(\\d+\\.?\\d+)\\s+([MKByte]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(getBody());
		if (matcher.find()) {
			return matcher.group(1) + " " + matcher.group(2);
		}
		return "0";
	}
}
