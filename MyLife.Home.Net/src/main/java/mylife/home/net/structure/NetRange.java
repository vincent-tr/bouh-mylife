package mylife.home.net.structure;

/**
 * Range
 * 
 * @author pumbawoman
 * 
 */
public class NetRange extends NetType {

	private final int min;
	private final int max;

	public NetRange(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

}
