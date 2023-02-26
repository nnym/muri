package muri;

public class IPv4Address implements IPAddress {
	public final int address;

	IPv4Address(int address) {
		this.address = address;
	}

	@Override public String toString() {
		return String.join(".", Integer.toString(this.address >> 24 & 0xFF), Integer.toString(this.address >> 16 & 0xFF), Integer.toString(this.address >> 8 & 0xFF), Integer.toString(this.address & 0xFF));
	}
}
