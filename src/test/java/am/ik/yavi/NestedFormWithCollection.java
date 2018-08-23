package am.ik.yavi;

import java.util.List;

public class NestedFormWithCollection {
	private final List<FormWithCollection> forms;

	public NestedFormWithCollection(List<FormWithCollection> forms) {
		this.forms = forms;
	}

	public List<FormWithCollection> getForms() {
		return forms;
	}
}
