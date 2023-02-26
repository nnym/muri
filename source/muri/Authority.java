package muri;

public class Authority {
	public final UserInfo userinfo;
	public final Host host;
	public final int port;

	Authority(UserInfo userinfo, Host host, int port) {
		this.userinfo = userinfo;
		this.host = host;
		this.port = port;
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
