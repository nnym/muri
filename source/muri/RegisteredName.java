package muri;

public class RegisteredName implements Host {
	public final String name;

	RegisteredName(String name) {
		this.name = name;
	}

	@Override public String toString() {
		return this.name;
	}
}
