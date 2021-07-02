package test.bv;

public final class Address_Country_Validator {
	private static final am.ik.yavi.builder.ValidatorBuilder<test.bv.Address.Country> BUILDER = am.ik.yavi.builder.ValidatorBuilder.<test.bv.Address.Country>of()
			.constraint(test.bv.Address.Country::getValue, "value", c -> c.notBlank());

	public static final am.ik.yavi.core.Validator<test.bv.Address.Country> INSTANCE = BUILDER.build();

	public static am.ik.yavi.builder.ValidatorBuilder<test.bv.Address.Country> builder() {
		return BUILDER.clone();
	}
}
