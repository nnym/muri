package muri;

public class Uri {
	public final String scheme;
	public final Authority authority;
	public final Path path;
	public final Query query;

	Uri(String scheme, Authority authority, Path path, Query query) {
		this.scheme = scheme;
		this.authority = authority;
		this.path = path;
		this.query = query;
	}

	public static Uri uri(String uri) {
		return new Parser(uri).parse();
	}

	@Override public String toString() {
		var builder = new StringBuilder();

		if (this.scheme != null) builder.append(this.scheme).append(':');
		if (this.authority != null) builder.append("//").append(this.authority);

		builder.append(this.path);

		if (this.query != null) builder.append('?').append(this.query);

		return builder.toString();
	}
}
