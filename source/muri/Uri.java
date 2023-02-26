package muri;

public class Uri {
	public final String scheme;
	public final Authority authority;
	public final Path path;
	public final Query query;
	public final String fragment;

	Uri(String scheme, Authority authority, Path path, Query query, String fragment) {
		this.scheme = scheme;
		this.authority = authority;
		this.path = path;
		this.query = query;
		this.fragment = fragment;
	}

	public static Uri uri(String uri) {
		return new Parser(uri).parse();
	}

	public Uri absolute() {
		if (this.scheme == null) throw new UnsupportedOperationException("this URI is a relative reference");
		return new Uri(this.scheme, this.authority, this.path, this.query, null);
	}

	public boolean isAbsolute() {
		return this.scheme != null && this.fragment == null;
	}

	@Override public String toString() {
		var builder = new StringBuilder();

		if (this.scheme != null) builder.append(this.scheme).append(':');
		if (this.authority != null) builder.append("//").append(this.authority);

		builder.append(this.path);

		if (this.query != null) builder.append('?').append(this.query);
		if (this.fragment != null) builder.append('#').append(this.fragment);

		return builder.toString();
	}
}
