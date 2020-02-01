package am.ik.yavi.core;

public abstract class IncludedViolationMessages {

	private static IncludedViolationMessages includedViolationMessages = null;

	public static IncludedViolationMessages get() {
		if (includedViolationMessages == null) {
			includedViolationMessages = new DefaultIncludedViolationMessages();
		}
		return includedViolationMessages;
	}

	public static void define(IncludedViolationMessages includedViolationMessages) {
		IncludedViolationMessages.includedViolationMessages = includedViolationMessages;
	}

	public abstract ViolationMessage OBJECT_NOT_NULL();

	public abstract ViolationMessage OBJECT_IS_NULL();

	public abstract ViolationMessage CONTAINER_NOT_EMPTY();

	public abstract ViolationMessage CONTAINER_LESS_THAN();

	public abstract ViolationMessage CONTAINER_LESS_THAN_OR_EQUAL();

	public abstract ViolationMessage CONTAINER_GREATER_THAN();

	public abstract ViolationMessage CONTAINER_GREATER_THAN_OR_EQUAL();

	public abstract ViolationMessage CONTAINER_FIXED_SIZE();

	public abstract ViolationMessage NUMERIC_GREATER_THAN();

	public abstract ViolationMessage NUMERIC_GREATER_THAN_OR_EQUAL();

	public abstract ViolationMessage NUMERIC_LESS_THAN();

	public abstract ViolationMessage NUMERIC_LESS_THAN_OR_EQUAL();

	public abstract ViolationMessage BOOLEAN_IS_TRUE();

	public abstract ViolationMessage BOOLEAN_IS_FALSE();

	public abstract ViolationMessage CHAR_SEQUENCE_NOT_BLANK();

	public abstract ViolationMessage CHAR_SEQUENCE_CONTAINS();

	public abstract ViolationMessage CHAR_SEQUENCE_EMAIL();

	public abstract ViolationMessage CHAR_SEQUENCE_URL();

	public abstract ViolationMessage CHAR_SEQUENCE_PATTERN();

	public abstract ViolationMessage BYTE_SIZE_LESS_THAN();

	public abstract ViolationMessage BYTE_SIZE_LESS_THAN_OR_EQUAL();

	public abstract ViolationMessage BYTE_SIZE_GREATER_THAN();

	public abstract ViolationMessage BYTE_SIZE_GREATER_THAN_OR_EQUAL();

	public abstract ViolationMessage BYTE_SIZE_FIXED_SIZE();

	public abstract ViolationMessage COLLECTION_CONTAINS();

	public abstract ViolationMessage MAP_CONTAINS_VALUE();

	public abstract ViolationMessage MAP_CONTAINS_KEY();

	public abstract ViolationMessage ARRAY_CONTAINS();

	public abstract ViolationMessage CODE_POINTS_ALL_INCLUDED();

	public abstract ViolationMessage CODE_POINTS_NOT_INCLUDED();
}
