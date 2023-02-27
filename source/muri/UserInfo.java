package muri;

import java.util.Objects;

public class UserInfo {
	public final String user;
	public final String password;

	private final String source;

	UserInfo(String source, String user, String password) {
		this.source = source;
		this.user = user;
		this.password = password;
	}

	@Override public int hashCode() {
		return this.source == null ? Objects.hash(this.user, this.password) : this.source.hashCode();
	}

	@Override public boolean equals(Object o) {
		return o instanceof UserInfo that
			&& (this.source == null ? this.user.equals(that.user) && Objects.equals(this.password, that.password) : this.source.equals(that.toString()));
	}

	@Override public String toString() {
		if (this.source != null) {
			return this.source;
		}

		var builder = new StringBuilder(this.user);
		if (this.password != null) builder.append(':').append(this.password);

		return builder.toString();
	}
}
