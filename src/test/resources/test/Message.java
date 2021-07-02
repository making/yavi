package am.ik.yavi.processor;

import jakarta.validation.constraints.NotEmpty;

public class Message {
	@NotEmpty
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
