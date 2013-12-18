package org.mylife.home.net.structure;

/**
 * Range
 * 
 * @author pumbawoman
 * 
 */
public class NetRange extends NetType {

	private final int min;
	private final int max;
	private final String toStringCache;

	public NetRange(int min, int max) {
		this.min = min;
		this.max = max;
		toStringCache = String.format("Range[%d..%d]", min, max);
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NetRange))
			return false;
		NetRange other = (NetRange) obj;

		if (other.min != min)
			return false;
		if (other.max != max)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return toStringCache.hashCode();
	}

	@Override
	public String toString() {
		return toStringCache;
	}

}
