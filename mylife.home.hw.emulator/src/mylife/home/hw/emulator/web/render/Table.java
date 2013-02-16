package mylife.home.hw.emulator.web.render;

public class Table extends WebContainer<Table.Row>{

	public Table(Row ... content) {
		super("table");
		for(Row item : content)
			this.getContent().add(item);
	}
	
	/**
	 * tr
	 * @author pumbawoman
	 *
	 */
	public static class Row extends WebContainer<Cell> {
		public Row(Cell ... content) {
			super("tr");
			for(Cell item : content)
				this.getContent().add(item);
		}
	}
	
	/**
	 * base pour td et th
	 * @author pumbawoman
	 *
	 */
	public static abstract class Cell extends WebContainer<WebRendable> {
		protected Cell(String tag) {
			super(tag);
		}
	}
	
	/**
	 * td
	 * @author pumbawoman
	 *
	 */
	public static class Data extends Cell {
		public Data(WebRendable ... content) {
			super("td");
			for(WebRendable item : content)
				this.getContent().add(item);
		}
	}
	
	/**
	 * th
	 * @author pumbawoman
	 *
	 */
	public static class Header extends Cell {
		public Header(WebRendable ... content) {
			super("th");
			for(WebRendable item : content)
				this.getContent().add(item);
		}
	}
}
