package muri;

import java.util.ArrayList;
import java.util.List;

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
