package muri;

import java.util.Arrays;

public final class IPv6Address implements IPAddress {
	public final short[] address;

	IPv6Address(short[] address) {
		this.address = address;
	}

	@Override public int hashCode() {
		return Arrays.hashCode(this.address);
	}

	@Override public boolean equals(Object o) {
		return o instanceof IPv6Address that && Arrays.equals(this.address, that.address);
	}

	@Override public String toString() {
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
