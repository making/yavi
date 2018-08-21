package am.ik.yavi.message;

import java.text.MessageFormat;

public class SimpleMessageFormatter implements MessageFormatter {
	@Override
	public String format(String name, String messageKey, String defaultMessageTemplate,
			Object[] args, Object value) {
		return MessageFormat.format(defaultMessageTemplate, args);
	}
}
