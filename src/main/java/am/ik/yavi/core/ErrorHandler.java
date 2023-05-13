package am.ik.yavi.core;

/**
 * @param <E> error type
 * @since 0.13.0
 */
public interface ErrorHandler<E> {
	void handleError(E errors, String name, String messageKey, Object[] args,
			String defaultMessage);
}
