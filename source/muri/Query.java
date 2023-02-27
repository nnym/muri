package muri;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Query {
	public final List<Parameter> parameters;

	private final String source;

	Query(String source, List<Parameter> parameters) {
		this.source = source;
		this.parameters = parameters;
	}

	public boolean has(String name) {
		return this.parameter(name) != null;
	}

	public String value(String name) {
		var parameter = this.parameter(name);
		return parameter == null ? null : parameter.value;
	}

	public Parameter parameter(String name) {
		Objects.requireNonNull(name, "name");

		for (var parameter : this.parameters) {
			if (parameter.name.equals(name)) {
				return parameter;
			}
		}

		return null;
	}

	@Override public int hashCode() {
		return this.source == null ? this.parameters.hashCode() : this.source.hashCode();
	}

	@Override public boolean equals(Object o) {
		return o instanceof Query that && (this.source == null ? this.parameters.equals(that.parameters) : this.source.equals(that.toString()));
	}

	@Override public String toString() {
		if (this.source != null) {
			return this.source;
		}

		var joiner = new StringJoiner("&");

		for (var parameter : this.parameters) {
			joiner.add(parameter.toString());
		}

		return joiner.toString();
	}
}
