package am.ik.yavi.meta;

public class Person {

	private final String firstName;

	private final String lastName;

	private final int age;

	@ConstraintArguments
	public Person(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}
}
