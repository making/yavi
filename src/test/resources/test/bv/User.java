package test.bv;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class User {

	@NotNull(message = "Please enter id")
	private Long id;

	@Size(max = 20, min = 3, message = "Invalid name")
	@NotEmpty(message = "Please enter name")
	private String name;

	@Email(message = "Invalid email")
	@NotEmpty(message = "Please enter email")
	private String email;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}