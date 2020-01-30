package am.ik.yavi.core;

public class DefaultIncludedViolationMessages extends IncludedViolationMessages {

    @Override
    public ViolationMessage OBJECT_NOT_NULL() {
        return ViolationMessage.of("object.notNull", "\"{0}\" must not be null");
    }

    @Override
    public ViolationMessage OBJECT_IS_NULL() {
        return ViolationMessage.of("object.isNull", "\"{0}\" must be null");
    }

    @Override
    public ViolationMessage CONTAINER_NOT_EMPTY() {
        return ViolationMessage.of("container.notEmpty", "\"{0}\" must not be empty");
    }

    @Override
    public ViolationMessage CONTAINER_LESS_THAN() {
        return ViolationMessage
                .of("container.lessThan", "The size of \"{0}\" must be less than {1}. The given size is {2}");
    }

    @Override
    public ViolationMessage CONTAINER_LESS_THAN_OR_EQUAL() {
        return ViolationMessage.of("container.lessThanOrEqual",
                "The size of \"{0}\" must be less than or equal to {1}. The given size is {2}");
    }

    @Override
    public ViolationMessage CONTAINER_GREATER_THAN() {
        return ViolationMessage
                .of("container.greaterThan", "The size of \"{0}\" must be greater than {1}. The given size is {2}");
    }

    @Override
    public ViolationMessage CONTAINER_GREATER_THAN_OR_EQUAL() {
        return ViolationMessage.of("container.greaterThanOrEqual",
                "The size of \"{0}\" must be greater than or equal to {1}. The given size is" + " {2}");
    }

    @Override
    public ViolationMessage CONTAINER_FIXED_SIZE() {
        return ViolationMessage.of("container.fixedSize", "The size of \"{0}\" must be {1}. The given size is {2}");
    }

    @Override
    public ViolationMessage NUMERIC_GREATER_THAN() {
        return ViolationMessage.of("numeric.greaterThan", "\"{0}\" must be greater than {1}");
    }

    @Override
    public ViolationMessage NUMERIC_GREATER_THAN_OR_EQUAL() {
        return ViolationMessage.of("numeric.greaterThanOrEqual", "\"{0}\" must be greater than or equal to {1}");
    }

    @Override
    public ViolationMessage NUMERIC_LESS_THAN() {
        return ViolationMessage.of("numeric.lessThan", "\"{0}\" must be less than {1}");
    }

    @Override
    public ViolationMessage NUMERIC_LESS_THAN_OR_EQUAL() {
        return ViolationMessage.of("numeric.lessThanOrEqual", "\"{0}\" must be less than or equal to {1}");
    }

    @Override
    public ViolationMessage BOOLEAN_IS_TRUE() {
        return ViolationMessage.of("boolean.isTrue", "\"{0}\" must be true");
    }

    @Override
    public ViolationMessage BOOLEAN_IS_FALSE() {
        return ViolationMessage.of("boolean.isFalse", "\"{0}\" must be false");
    }

    @Override
    public ViolationMessage CHAR_SEQUENCE_NOT_BLANK() {
        return ViolationMessage.of("charSequence.notBlank", "\"{0}\" must not be blank");
    }

    @Override
    public ViolationMessage CHAR_SEQUENCE_CONTAINS() {
        return ViolationMessage.of("charSequence.contains", "\"{0}\" must contain {1}");
    }

    @Override
    public ViolationMessage CHAR_SEQUENCE_EMAIL() {
        return ViolationMessage.of("charSequence.email", "\"{0}\" must be a valid email address");
    }

    @Override
    public ViolationMessage CHAR_SEQUENCE_URL() {
        return ViolationMessage.of("charSequence.url", "\"{0}\" must be a valid URL");
    }

    @Override
    public ViolationMessage CHAR_SEQUENCE_PATTERN() {
        return ViolationMessage.of("charSequence.pattern", "\"{0}\" must match {1}");
    }

    @Override
    public ViolationMessage BYTE_SIZE_LESS_THAN() {
        return ViolationMessage
                .of("byteSize.lessThan", "The size of \"{0}\" must be less than {1}. The given size is {2}");
    }

    @Override
    public ViolationMessage BYTE_SIZE_LESS_THAN_OR_EQUAL() {
        return ViolationMessage.of("byteSize.lessThanOrEqual",
                "The byte size of \"{0}\" must be less than or equal to {1}. The given size is " + "{2}");
    }

    @Override
    public ViolationMessage BYTE_SIZE_GREATER_THAN() {
        return ViolationMessage
                .of("byteSize.greaterThan", "The byte size of \"{0}\" must be greater than {1}. The given size is {2}");
    }

    @Override
    public ViolationMessage BYTE_SIZE_GREATER_THAN_OR_EQUAL() {
        return ViolationMessage.of("byteSize.greaterThanOrEqual",
                "The byte size of \"{0}\" must be greater than or equal to {1}. The given " + "size is {2}");
    }

    @Override
    public ViolationMessage BYTE_SIZE_FIXED_SIZE() {
        return ViolationMessage.of("byteSize.fixedSize", "The byte size of \"{0}\" must be {1}. The given size is {2}");
    }

    @Override
    public ViolationMessage COLLECTION_CONTAINS() {
        return ViolationMessage.of("collection.contains", "\"{0}\" must contain {1}");
    }

    @Override
    public ViolationMessage MAP_CONTAINS_VALUE() {
        return ViolationMessage.of("map.containsValue", "\"{0}\" must contain value {1}");
    }

    @Override
    public ViolationMessage MAP_CONTAINS_KEY() {
        return ViolationMessage.of("map.containsKey", "\"{0}\" must contain key {1}");
    }

    @Override
    public ViolationMessage ARRAY_CONTAINS() {
        return ViolationMessage.of("array.contains", "\"{0}\" must contain {1}");
    }

    @Override
    public ViolationMessage CODE_POINTS_ALL_INCLUDED() {
        return ViolationMessage.of("codePoints.asWhiteList", "\"{1}\" is/are not allowed for \"{0}\"");
    }

    @Override
    public ViolationMessage CODE_POINTS_NOT_INCLUDED() {
        return ViolationMessage.of("codePoints.asBlackList", "\"{1}\" is/are not allowed for \"{0}\"");
    }
}
