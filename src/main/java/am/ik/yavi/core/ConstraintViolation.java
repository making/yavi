package am.ik.yavi.core;

import java.util.Arrays;

import am.ik.yavi.message.MessageFormatter;

public class ConstraintViolation {
	private final String name;
	private final String messageKey;
	private final String defaultMessageTemplate;
	private final Object[] args;
	private final Object value;
	private final MessageFormatter messageFormatter;

	public ConstraintViolation(String name, String messageKey,
			String defaultMessageTemplate, Object[] args, Object value,
			MessageFormatter messageFormatter) {
		this.name = name;
		this.messageKey = messageKey;
		this.defaultMessageTemplate = defaultMessageTemplate;
		this.args = args;
		this.value = value;
		this.messageFormatter = messageFormatter;
	}

	public String message() {
		return this.messageFormatter.format(this.name, this.messageKey,
				this.defaultMessageTemplate, this.args, this.value);
	}

	@Override
	public String toString() {
		return "ConstraintViolation{" + "name='" + name + '\'' + ", messageKey='"
				+ messageKey + '\'' + ", defaultMessageTemplate='"
				+ defaultMessageTemplate + '\'' + ", args=" + Arrays.toString(args)
				+ ", value=" + value + ", messageFormatter=" + messageFormatter + '}';
	}
}
