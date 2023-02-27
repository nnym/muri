package muri;

public final class Name implements Host {
	public final String name;

	Name(String name) {
		this.name = name;
	}

	@Override public int hashCode() {
		return this.name.hashCode();
	}

	@Override public boolean equals(Object o) {
		return o instanceof Name that && this.name.equals(that.name);
	}

	@Override public String toString() {
		return this.name;
	}
}
