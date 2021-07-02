package test.bv;

public final class UserForm_Validator {
	private static final am.ik.yavi.builder.ValidatorBuilder<test.bv.UserForm> BUILDER = am.ik.yavi.builder.ValidatorBuilder.<test.bv.UserForm>of()
			.constraint(test.bv.UserForm::getEmail, "email", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(50).email())
			.constraint(test.bv.UserForm::getName, "name", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(20))
			.constraint(test.bv.UserForm::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200));

	public static final am.ik.yavi.core.Validator<test.bv.UserForm> INSTANCE = BUILDER.build();

	public static am.ik.yavi.builder.ValidatorBuilder<test.bv.UserForm> builder() {
		return BUILDER.clone();
	}
}
