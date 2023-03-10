package muri;

public final class IPv4Address implements IPAddress {
	public final int address;

	IPv4Address(int address) {
		this.address = address;
	}

	@Override public int hashCode() {
		return this.address;
	}

	@Override public boolean equals(Object o) {
		return o instanceof IPv4Address that && this.address == that.address;
	}

	@Override public String toString() {
		return String.join(".", Integer.toString(this.address >> 24 & 0xFF), Integer.toString(this.address >> 16 & 0xFF), Integer.toString(this.address >> 8 & 0xFF), Integer.toString(this.address & 0xFF));
	}
}
