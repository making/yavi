package test.bv;

public final class Car_Validator {
	private static final am.ik.yavi.builder.ValidatorBuilder<test.bv.Car> BUILDER = am.ik.yavi.builder.ValidatorBuilder.<test.bv.Car> of()
			.constraint(test.bv.Car::getManufacturer, "manufacturer", c -> c.notNull())
			.constraint(test.bv.Car::getLicensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
			.constraint(test.bv.Car::getSeatCount, "seatCount", c -> c.greaterThanOrEqual(2));

	public static final am.ik.yavi.core.Validator<test.bv.Car> INSTANCE = BUILDER.build();

	public static am.ik.yavi.builder.ValidatorBuilder<test.bv.Car> builder() {
		return BUILDER.clone();
	}
}