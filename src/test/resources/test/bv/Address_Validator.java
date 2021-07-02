package test.bv;

public final class Address_Validator {
	private static final am.ik.yavi.builder.ValidatorBuilder<test.bv.Address> BUILDER = am.ik.yavi.builder.ValidatorBuilder.<test.bv.Address>of()
			.constraint(test.bv.Address::getStreet, "street", c -> c.notBlank());

	public static final am.ik.yavi.core.Validator<test.bv.Address> INSTANCE = BUILDER.build();

	public static am.ik.yavi.builder.ValidatorBuilder<test.bv.Address> builder() {
		return BUILDER.clone();
	}
}
