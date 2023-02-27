package muri;

import java.util.Objects;

public class Authority {
	public final UserInfo userinfo;
	public final Host host;
	public final int port;

	Authority(UserInfo userinfo, Host host, int port) {
		this.userinfo = userinfo;
		this.host = host;
		this.port = port;
	}

	@Override public int hashCode() {
		return Objects.hash(this.userinfo, this.host, this.port);
	}

	@Override public boolean equals(Object o) {
		return o instanceof Authority that && Objects.equals(this.userinfo, that.userinfo) && this.host.equals(that.host) && this.port == that.port;
	}

	@Override public String toString() {
		var builder = new StringBuilder();

		if (this.userinfo != null) builder.append(this.userinfo).append('@');

		if (this.host instanceof IPv6Address || this.host instanceof IPvFutureAddress) {
			builder.append('[').append(this.host).append(']');
		} else {
			builder.append(this.host);
		}

		if (this.port >= 0) builder.append(':').append(this.port);

		return builder.toString();
	}
}
