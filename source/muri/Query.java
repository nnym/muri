package muri;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Query {
	public final List<Parameter> parameters;

	Query(List<Parameter> parameters) {
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

	@Override public String toString() {
		var joiner = new StringJoiner("&");

		for (var parameter : this.parameters) {
			joiner.add(parameter.toString());
		}

		return joiner.toString();
	}
}
