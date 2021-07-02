package test.bv;

public final class User_Validator {
	private static final am.ik.yavi.builder.ValidatorBuilder<test.bv.User> BUILDER = am.ik.yavi.builder.ValidatorBuilder.<test.bv.User>of()
			.constraint(test.bv.User::getEmail, "email", c -> c.email().message("Invalid email").notEmpty().message("Please enter email"))
			.constraint(test.bv.User::getName, "name", c -> c.greaterThanOrEqual(3).message("Invalid name").lessThanOrEqual(20).message("Invalid name").notEmpty().message("Please enter name"))
			.constraint(test.bv.User::getId, "id", c -> c.notNull().message("Please enter id"));

	public static final am.ik.yavi.core.Validator<test.bv.User> INSTANCE = BUILDER.build();

	public static am.ik.yavi.builder.ValidatorBuilder<test.bv.User> builder() {
		return BUILDER.clone();
	}
}
