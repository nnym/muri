package muri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		return uri(uri, true);
	}

	public static Uri uri(URI uri) {
		return uri(uri, true);
	}

	public static Uri uri(URL url) {
		return uri(url, true);
	}

	public static Uri uri(String uri, boolean normalize) {
		return new Parser(uri, normalize).parse();
	}

	public static Uri uri(URI uri, boolean normalize) {
		return uri(uri.toString());
	}

	public static Uri uri(URL url, boolean normalize) {
		return uri(url.toString());
	}

	public Uri resolve(Uri reference) {
		if (reference.scheme != null) return new Uri(reference.scheme, reference.authority, reference.path.removeDotSegments(), reference.query, reference.fragment);
		if (reference.authority != null) return new Uri(this.scheme, reference.authority, reference.path.removeDotSegments(), reference.query, reference.fragment);
		if (reference.path.isEmpty()) return new Uri(this.scheme, this.authority, this.path, reference.query == null ? this.query : reference.query, reference.fragment);

		return new Uri(this.scheme, this.authority, (reference.path.absolute ? reference.path : this.mergePath(reference.path)).removeDotSegments(), this.query, reference.fragment);
	}

	public Uri resolve(String reference) {
		return this.resolve(uri(reference));
	}

	public Uri absolute() {
		if (this.scheme == null) throw new UnsupportedOperationException("this URI is a relative reference");
		return new Uri(this.scheme, this.authority, this.path, this.query, null);
	}

	public boolean isAbsolute() {
		return this.scheme != null && this.fragment == null;
	}

	public URI toURI() {
		return URI.create(this.toString());
	}

	public URL toURL() {
		try {
			return new URL(this.toString());
		} catch (MalformedURLException exception) {
			throw new IllegalArgumentException(exception.getMessage(), exception);
		}
	}

	@Override public int hashCode() {
		return Objects.hash(this.scheme, this.authority, this.path, this.query, this.fragment);
	}

	@Override public boolean equals(Object o) {
		return o instanceof Uri that
			&& Objects.equals(this.scheme, that.scheme)
			&& Objects.equals(this.authority, that.authority)
			&& this.path.equals(that.path)
			&& Objects.equals(this.query, that.query)
			&& Objects.equals(this.fragment, that.fragment);
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

	private Path mergePath(Path path) {
		if (this.authority != null && this.path.isEmpty()) return new Path(true, path.segments);

		var segments = new ArrayList<>(this.path.segments.subList(0, Math.max(0, this.path.segments.size() - 1)));
		segments.addAll(path.segments);

		return new Path(this.path.absolute, List.copyOf(segments));
	}
}
