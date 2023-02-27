package muri;

import java.util.Objects;

public class UserInfo {
	public final String user;
	public final String password;

	UserInfo(String user, String password) {
		this.user = user;
		this.password = password;
	}

	@Override public int hashCode() {
		return Objects.hash(this.user, this.password);
	}

	@Override public boolean equals(Object o) {
		return o instanceof UserInfo that && this.user.equals(that.user) && Objects.equals(this.password, that.password);
	}

	@Override public String toString() {
		var builder = new StringBuilder(this.user);
		if (this.password != null) builder.append(':').append(this.password);

		return builder.toString();
	}
}
