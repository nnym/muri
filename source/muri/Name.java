package muri;

public final class Name implements Host {
	public final String name;

	Name(String name) {
		this.name = name;
	}

	@Override public String toString() {
		return this.name;
	}
}
