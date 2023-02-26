package muri;

public class Uri {
	public final String scheme;
	public final Authority authority;
	public final Path path;

	Uri(String scheme, Authority authority, Path path) {
		this.scheme = scheme;
		this.authority = authority;
		this.path = path;
	}

	public static Uri uri(String uri) {
		return new Parser(uri).parse();
	}

	@Override public String toString() {
		var builder = new StringBuilder();

		if (this.scheme != null) builder.append(this.scheme).append(':');
		if (this.authority != null) builder.append("//").append(this.authority);

		return builder.append(this.path).toString();
	}
}
