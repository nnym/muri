package muri;

import java.util.Objects;

public final class IPvFutureAddress implements IPAddress {
	public final long version;
	public final String address;

	IPvFutureAddress(long version, String address) {
		this.version = version;
		this.address = address;
	}

	@Override public int hashCode() {
		return Objects.hash(this.version, this.address);
	}

	@Override public boolean equals(Object o) {
		return o instanceof IPvFutureAddress that && this.version == that.version && Objects.equals(this.address, that.address);
	}

	@Override public String toString() {
		return 'v' + Long.toHexString(this.version) + '.' + this.address;
	}
}
