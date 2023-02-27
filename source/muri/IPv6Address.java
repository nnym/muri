package muri;

import java.util.Arrays;

public final class IPv6Address implements IPAddress {
	public final short[] address;

	private final String source;

	IPv6Address(String source, short[] address) {
		this.source = source;
		this.address = address;
	}

	@Override public int hashCode() {
		return this.source == null ? Arrays.hashCode(this.address) : this.source.hashCode();
	}

	@Override public boolean equals(Object o) {
		return o instanceof IPv6Address that && (this.source == null ? Arrays.equals(this.address, that.address) : this.source.equals(that.toString()));
	}

	@Override public String toString() {
		if (this.source != null) {
			return this.source;
		}

		var builder = new StringBuilder();
		var zeroCount = 0;
		var elisionCount = 1;
		var elisionIndex = -2;

		for (var index = 0; index < this.address.length; ++index) {
			zeroCount = this.address[index] == 0 ? zeroCount + 1 : 0;

			if (zeroCount > elisionCount) {
				elisionCount = zeroCount;
				elisionIndex = index + 1 - elisionCount;
			}
		}

		for (var index = 0; index < this.address.length; ++index) {
			if (index == elisionIndex) {
				builder.append("::");
				index = elisionIndex + elisionCount - 1;
			} else {
				builder.append(Integer.toHexString(Short.toUnsignedInt(this.address[index])));

				if (index != elisionIndex - 1 && index != this.address.length - 1) {
					builder.append(':');
				}
			}
		}

		return builder.toString();
	}
}
