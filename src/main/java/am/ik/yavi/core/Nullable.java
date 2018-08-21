package am.ik.yavi.core;

public enum Nullable {
	NULL_IS_INVALID(false), NULL_IS_VALID(true);

	private final boolean skipNull;

	Nullable(boolean skipNull) {
		this.skipNull = skipNull;
	}

	public boolean skipNull() {
		return this.skipNull;
	}
}
