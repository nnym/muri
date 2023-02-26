package muri;

public class IPvFutureAddress implements IPAddress {
	public final long version;
	public final String address;

	public IPvFutureAddress(long version, String address) {
		this.version = version;
		this.address = address;
	}

	@Override public String toString() {
		return 'v' + Long.toHexString(this.version) + '.' + this.address;
	}
}
