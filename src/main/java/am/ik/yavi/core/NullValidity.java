package am.ik.yavi.core;

public enum NullValidity {
	NULL_IS_INVALID(false), NULL_IS_VALID(true);

	private final boolean skipNull;

	NullValidity(boolean skipNull) {
		this.skipNull = skipNull;
	}

	public boolean skipNull() {
		return this.skipNull;
	}
}
