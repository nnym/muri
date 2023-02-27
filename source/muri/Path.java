package muri;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Path {
	static final Path empty = new Path(false, List.of());

	public final boolean absolute;
	public final List<String> segments;

	Path(boolean absolute, List<String> segments) {
		this.absolute = absolute;
		this.segments = segments;
	}

	public boolean isEmpty() {
		return !this.absolute && this.segments.isEmpty();
	}

	public String filename() {
		return this.segments.isEmpty() ? "" : this.segments.get(this.segments.size() - 1);
	}

	@Override public int hashCode() {
		return Objects.hash(this.absolute, this.segments);
	}

	@Override public boolean equals(Object o) {
		return o instanceof Path that && this.absolute == that.absolute && this.segments.equals(that.segments);
	}

	@Override public String toString() {
		var builder = new StringBuilder();

		if (this.absolute) builder.append('/');

		return builder.append(String.join("/", this.segments)).toString();
	}

	Path removeDotSegments() {
		var segments = new ArrayList<String>();

		for (var iterator = this.segments.iterator(); iterator.hasNext();) {
			var segment = iterator.next();

			if (!segment.equals(".")) {
				if (segment.equals("..")) {
					if (!segments.isEmpty()) {
						segments.remove(segments.size() - 1);
					}
				} else {
					segments.add(segment);
					continue;
				}
			}

			if (!iterator.hasNext()) {
				segments.add("");
			}
		}

		return new Path(this.absolute, List.copyOf(segments));
	}
}
