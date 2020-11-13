package tulli.com.br.scrapingtool;

public class Result {

	private int totalLines;
	private double totalSize;
	private int quantity;

	public Result() {
		this.totalLines = 0;
		this.totalSize = 0.0;
		this.quantity = 0;
	}

	public Result(int lines, double size) {
		this.totalLines = lines;
		this.totalSize = size;
		this.quantity = 1;
	}

	public int getTotalLines() {
		return this.totalLines;
	}

	public double getTotalSize() {
		return this.totalSize;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void addTotalLines(int linesToAdd) {
		this.totalLines += linesToAdd;
	}

	public void addTotalSize(double sizeToAdd) {
		this.totalSize += sizeToAdd;
	}

	public void addQuantity() {
		this.quantity++;
	}

	@Override
	public String toString() {
		return "Result [totalLines=" + totalLines + ", totalSize=" + totalSize + ", quantity=" + quantity + "]";
	}

}
