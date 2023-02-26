package muri;

import java.util.List;

public class Path {
	static final Path empty = new Path(false, List.of());

	public final boolean absolute;
	public final List<String> segments;

	Path(boolean absolute, List<String> segments) {
		this.absolute = absolute;
		this.segments = segments;
	}

	Path() {
		this(false, List.of());
	}

	public boolean isEmpty() {
		return !this.absolute && this.segments.isEmpty();
	}

	@Override public String toString() {
		var builder = new StringBuilder();

		if (this.absolute) builder.append('/');

		return builder.append(String.join("/", this.segments)).toString();
	}
}
