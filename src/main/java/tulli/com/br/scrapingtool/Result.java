package tulli.com.br.scrapingtool;

public class Result {

	private int totalLines;
	private double totalSize;

	public Result() {
		this.totalLines = 0;
		this.totalSize = 0.0;
	}

	public Result(int lines, double size) {
		this.totalLines = lines;
		this.totalSize = size;
	}

	public int getTotalLines() {
		return this.totalLines;
	}

	public double getTotalSize() {
		return this.totalSize;
	}

	public void addTotalLines(int linesToAdd) {
		this.totalLines += linesToAdd;
	}

	public void addTotalSize(double sizeToAdd) {
		this.totalSize += sizeToAdd;
	}

	@Override
	public String toString() {
		return "Result [totalLines=" + totalLines + ", totalSize=" + totalSize + "]";
	}
}
