package muri;

import java.util.Objects;

public class Parameter {
	public final String name;
	public final String value;

	Parameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override public int hashCode() {
		return Objects.hash(this.name, this.value);
	}

	@Override public boolean equals(Object o) {
		return o instanceof Parameter that && this.name.equals(that.name) && Objects.equals(this.value, that.value);
	}

	@Override public String toString() {
		return this.value == null ? this.name : this.name + '=' + this.value;
	}
}
