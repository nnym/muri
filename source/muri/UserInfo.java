package muri;

public class UserInfo {
	public final String user;
	public final String password;

	UserInfo(String user, String password) {
		this.user = user;
		this.password = password;
	}

	@Override public String toString() {
		var builder = new StringBuilder(this.user);
		if (this.password != null) builder.append(':').append(this.password);

		return builder.toString();
	}
}
