package am.ik.yavi;

import am.ik.yavi.core.Validator;

public class PhoneNumber {
	private final String value;

	public PhoneNumber(String value) {
		this.value = value;
	}

	public static Validator<PhoneNumber> validator() {
		return new Validator<PhoneNumber>().constraint((PhoneNumber p) -> p.value,
				"value", c -> c.notBlank().greaterThanOrEquals(8).lessThanOrEquals(16));
	}
}
