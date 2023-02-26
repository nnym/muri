package muri;

public class Parameter {
	public final String name;
	public final String value;

	Parameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override public String toString() {
		return this.value == null ? this.name : this.name + '=' + this.value;
	}
}
