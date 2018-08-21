package am.ik.yavi.message;

public interface MessageFormatter {
	String format(String name, String messageKey, String defaultMessageTemplate,
			Object[] args, Object value);
}
