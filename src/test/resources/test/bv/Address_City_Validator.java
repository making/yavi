package test.bv;

public final class Address_City_Validator {
	private static final am.ik.yavi.builder.ValidatorBuilder<test.bv.Address.City> BUILDER = am.ik.yavi.builder.ValidatorBuilder.<test.bv.Address.City>of()
			.constraint(test.bv.Address.City::getValue, "value", c -> c.notBlank());

	public static final am.ik.yavi.core.Validator<test.bv.Address.City> INSTANCE = BUILDER.build();

	public static am.ik.yavi.builder.ValidatorBuilder<test.bv.Address.City> builder() {
		return BUILDER.clone();
	}
}
