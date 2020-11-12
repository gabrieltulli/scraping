package tulli.com.br.scrapingtool;

public class FileDetail {

	private String fileName;
	private int lines;
	private double size;
	private String type;
	private boolean processed;

	public FileDetail() {
		super();
	}

	public FileDetail(String fileName) {
		super();
		this.fileName = fileName;
	}

	public FileDetail(String fileName, String type) {
		super();
		this.fileName = fileName;
		this.type = type;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public double getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setSize(String size) {
		if (size.length() == 1) {
			this.size = 0;
			return;
		}
		int modifier = 1;
		if (size.contains("KB")) {
			modifier = 1000;
		} else if (size.contains("MB")) {
			modifier = 1000 * 1000;
		}
		size = size.substring(0, size.indexOf(" ")).trim();
		this.size = Double.parseDouble(size) * modifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "FileDetail [fileName=" + fileName + ", lines=" + lines + ", size=" + size + "]";
	}

	public String getExtension() {
		if (isFolder()) {
			return "folder";
		}
		int dotPosition = getFileName().lastIndexOf(".");
		if (dotPosition > 0) {
			return getFileName().substring(dotPosition);
		} else {
			return getFileName().substring(getFileName().lastIndexOf("/") + 1);
		}

	}

	public boolean isFolder() {
		return getFileName().contains("/tree/");
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public int getLines() {
		return lines;
	}

	public void setLines(int lines) {
		this.lines = lines;
	}
}
