package test.bv;

public final class Bv2AllField_Validator {
	private static final am.ik.yavi.builder.ValidatorBuilder<test.bv.Bv2AllField> BUILDER = am.ik.yavi.builder.ValidatorBuilder.<test.bv.Bv2AllField> of()
			.constraint(test.bv.Bv2AllField::getDecimalMaxValue, "decimalMaxValue", c -> c.lessThanOrEqual(new java.math.BigDecimal("2000.0")))
			.constraint(test.bv.Bv2AllField::getNotBlankValue, "notBlankValue", c -> c.notBlank())
			.constraint(test.bv.Bv2AllField::getNegativeOrZeroValue, "negativeOrZeroValue", c -> c.lessThanOrEqual(0))
			.constraint(test.bv.Bv2AllField::getPositiveValue, "positiveValue", c -> c.greaterThan(0))
			.constraint(test.bv.Bv2AllField::isAssertFalseValue, "assertFalseValue", c -> c.isFalse())
			.constraint(test.bv.Bv2AllField::getNullValue, "nullValue", c -> c.isNull())
			.constraint(test.bv.Bv2AllField::getNotEmptyValue, "notEmptyValue", c -> c.notEmpty())
			.constraint(test.bv.Bv2AllField::getPositiveOrZeroValue, "positiveOrZeroValue", c -> c.greaterThanOrEqual(0))
			.constraint(test.bv.Bv2AllField::getNotNullValue, "notNullValue", c -> c.notNull())
			.constraint(test.bv.Bv2AllField::getSizeValue, "sizeValue", c -> c.greaterThanOrEqual(3).lessThanOrEqual(10))
			.constraint(test.bv.Bv2AllField::getMaxValue, "maxValue", c -> c.lessThanOrEqual(100))
			.constraint(test.bv.Bv2AllField::getNegativeValue, "negativeValue", c -> c.lessThan(0))
			.constraint(test.bv.Bv2AllField::getEmailValue, "emailValue", c -> c.email())
			.constraint(test.bv.Bv2AllField::getDecimalMinValue, "decimalMinValue", c -> c.greaterThanOrEqual(new java.math.BigDecimal("2.0")))
			.constraint(test.bv.Bv2AllField::getMinValue, "minValue", c -> c.greaterThanOrEqual(0))
			.constraint(test.bv.Bv2AllField::isAssertTrueValue, "assertTrueValue", c -> c.isTrue());

	public static final am.ik.yavi.core.Validator<test.bv.Bv2AllField> INSTANCE = BUILDER.build();

	public static am.ik.yavi.builder.ValidatorBuilder<test.bv.Bv2AllField> builder() {
		return BUILDER.clone();
	}
}
