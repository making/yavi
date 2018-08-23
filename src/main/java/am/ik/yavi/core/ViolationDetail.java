package am.ik.yavi.core;

import java.util.Arrays;

/**
 * This class is intended to be used for the JSON serialization (such as Jackson)
 */
public class ViolationDetail {
	private final String key;
	private final Object[] args;
	private final String defaultMessage;

	public ViolationDetail(String key, Object[] args, String defaultMessage) {
		this.key = key;
		this.args = args;
		this.defaultMessage = defaultMessage;
	}

	public String getKey() {
		return key;
	}

	public Object[] getArgs() {
		return args;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}

	@Override
	public String toString() {
		return "ViolationDetail{" + "key='" + key + '\'' + ", args="
				+ Arrays.toString(args) + ", defaultMessage='" + defaultMessage + '\''
				+ '}';
	}
}
