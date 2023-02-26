package muri;

public class RegisteredName implements Host {
	public final String name;

	public RegisteredName(String name) {
		this.name = name;
	}

	@Override public String toString() {
		return this.name;
	}
}
