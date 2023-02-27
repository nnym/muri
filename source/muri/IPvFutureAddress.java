package muri;

import java.util.Objects;

public final class IPvFutureAddress implements IPAddress {
	public final long version;
	public final String address;

	private final String source;

	IPvFutureAddress(String source, long version, String address) {
		this.source = source;
		this.version = version;
		this.address = address;
	}

	@Override public int hashCode() {
		return this.source == null ? Objects.hash(this.version, this.address) : this.source.hashCode();
	}

	@Override public boolean equals(Object o) {
		return o instanceof IPvFutureAddress that && (this.source == null ? this.version == that.version && Objects.equals(this.address, that.address) : this.source.equals(that.toString()));
	}

	@Override public String toString() {
		return this.source == null ? 'v' + Long.toHexString(this.version) + '.' + this.address : this.source;
	}
}
