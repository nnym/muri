package muri;

public final class Name implements Host {
	public final String name;

	private final String source;

	Name(String source, String name) {
		this.source = source;
		this.name = name;
	}

	@Override public int hashCode() {
		return this.toString().hashCode();
	}

	@Override public boolean equals(Object o) {
		return o instanceof Name that && this.toString().equals(that.toString());
	}

	@Override public String toString() {
		return this.source == null ? this.name : this.source;
	}
}
