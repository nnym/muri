package muri;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Parser {
	private final String uri;
	private final boolean normalize;
	private int index = -1;
	private int start = -1;
	private int character;
	private int value = -1;

	Parser(String uri, boolean normalize) {
		this.uri = uri;
		this.normalize = normalize;
	}

	Uri parse() {
		if (!this.advance()) return new Uri(null, null, Path.empty, null, null);
		if (this.character == ':') throw new IllegalArgumentException("empty scheme");

		String scheme = null;
		var path = Path.empty;

		if (this.character == '/') {
			this.index = -1;
		} else if (!this.in("?#")) A: {
			var character = this.character;
			var alpha = alpha(character);

			while (this.advance()) {
				if (this.character == ':') {
					if (!alpha) throw new IllegalArgumentException("0th character ('%c') is not A-z".formatted(character));

					scheme = this.uri.substring(0, this.index);
					if (this.normalize) scheme = scheme.toLowerCase(Locale.ROOT);

					break A;
				};

				if (!part(this.character) && this.character != '+') break;
			}

			for (;;) {
				if (this.character == ':') throw new IllegalArgumentException("non-scheme ([A-Za-z\\d+-.]) character ('%c')".formatted(this.character));

				if (!this.pcharNc() || !this.advance()) {
					if (!this.in("/?#") && !this.finished()) throw this.illegalCharacter();

					var segments = new ArrayList<String>();
					segments.add(this.uri.substring(0, this.index));
					path = this.character == '/' ? this.path(false, segments) : new Path(false, segments);

					break;
				}
			}
		}

		Authority authority = null;
		Query query = null;
		String fragment = null;

		if (this.hasNext()) A: {
			if (path == Path.empty) {
				if (this.advance('/')) B: {
					if (!this.hasNext() || this.peek("?#")) {
						this.advance();
						path = new Path(true, List.of());
						break B;
					}

					if (this.advance('/')) {
						if (this.advanceEncoded()) {
							authority = this.authority();

							if (this.finished() || this.character != '/') {
								break B;
							}
						} else {
							break B;
						}
					}

					path = this.path(true);
				} else if (!this.in("?#")) {
					path = this.path(false);
				}
			}

			if (this.finished()) break A;

			if (this.character == '?') {
				query = this.query();
			}

			if (this.finished()) break A;
			if (this.character != '#') throw this.illegalCharacter();

			fragment = this.fragment();
		}

		return new Uri(scheme, authority, path, query, fragment);
	}

	private boolean pchar() {
		return this.ups() || this.character == ':' || this.character == '@';
	}

	private boolean pcharNc() {
		return this.ups() || this.character == '@';
	}

	private boolean qfchar() {
		return this.pchar() || this.character == '/' || this.character == '?';
	}

	private static boolean part(int character) {
		return alphanumeric(character) || character == '-' || character == '.';
	}

	private static boolean alpha(int character) {
		return character >= 'a' && character <= 'z'
			|| character >= 'A' && character <= 'Z';
	}

	private static boolean numeric(int character) {
		return character >= '0' && character <= '9';
	}

	private static boolean alphanumeric(int character) {
		return alpha(character) || numeric(character);
	}

	private static boolean hex(int character) {
		return numeric(character)
			|| character >= 'a' && character <= 'f'
			|| character >= 'A' && character <= 'F';
	}

	private static boolean subDelim(int character) {
		return switch (character) {
			case '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=' -> true;
			default -> false;
		};
	}

	private static boolean us(int character) {
		return part(character) || character == '_' || character == '~' || subDelim(character);
	}

	private IllegalArgumentException illegalCharacter() {
		return new IllegalArgumentException("illegal character '%c' at index %s".formatted(this.character, this.index));
	}

	private String fragment() {
		var index = this.index + 1;

		while (this.advance()) {
			if (!this.qfchar()) throw this.illegalCharacter();
		}

		return this.index == index ? "" : this.decodeSubstring(index, this.index);
	}

	private Query query() {
		var parameters = new ArrayList<Parameter>();
		int index = this.index + 1, start = index;
		var finished = false;
		String name = null, value;

		while (!finished) {
			if ((finished = !this.advanceEncoded() || !this.qfchar()) || this.value == '&') {
				value = this.decodeSubstring(index, this.start);

				if (name == null) {
					name = value;
					value = null;
				}

				parameters.add(new Parameter(name, value));
				name = null;
				index = this.index + 1;
			} else if (this.value == '=' && name == null) {
				name = this.decodeSubstring(index, this.start);
				index = this.index + 1;
			}
		}

		return new Query(this.normalize ? null : this.uri.substring(start, this.index), List.copyOf(parameters));
	}

	private Path path(boolean absolute) {
		return this.path(absolute, new ArrayList<>());
	}

	private Path path(boolean absolute, List<String> segments) {
		var index = this.index + 1;
		var finished = false;

		while (!finished) {
			if ((finished = !this.advance()) || this.character == '/' || (finished |= !this.pchar())) {
				segments.add(this.index == index ? "" : this.decodeSubstring(index, this.index));
				index = this.index + 1;
			}
		}

		return new Path(absolute, List.copyOf(segments));
	}

	private IPAddress ipLiteral() {
		if (this.character == '[') {
			if (!this.advance()) throw new IllegalArgumentException("missing ']' for host IP address");

			var address = this.character == 'v' ? this.ipFuture() : this.ip6();
			this.advance();

			return address;
		}

		return null;
	}

	private IPvFutureAddress ipFuture() {
		var start = this.index;

		if (!this.advance()) throw new IllegalArgumentException("expected IP address future version");

		var index = this.index;

		while (this.character != '.') {
			if (!hex(this.character)) throw new IllegalArgumentException("non-hexadecimal IP address future version character '%c' at index %s".formatted(this.character, this.index));
			if (!this.advance()) throw new IllegalArgumentException("expected '.'");
		}

		var version = Long.parseUnsignedLong(this.uri.substring(index, this.index), 16);

		if (!this.advance()) throw new IllegalArgumentException("expected future IP address");

		index = this.index;

		while (this.character != ']') {
			if (!us(this.character) && this.character != ':') {
				throw new IllegalArgumentException("illegal character '%c' in future IP address at index %s".formatted(this.character, this.index));
			}

			if (!this.advance()) throw new IllegalArgumentException("missing ']' for host IP address");
		}

		return new IPvFutureAddress(this.normalize ? null : this.uri.substring(start, this.index), version, this.uri.substring(index, this.index));
	}

	private IPv6Address ip6() {
		int index = this.index, start = index;
		var address = new int[]{-1, -1, -1, -1, -1, -1, -1, -1};
		var elision = 8;
		var octet = 0;

		for (;;) {
			if (this.character == ']') {
				if (elision == 8 && (octet < 7 || address[octet] == -1)) throw new IllegalArgumentException("incomplete IPv6 address");

				break;
			}

			if (this.character == ':') {
				if (this.peek(':')) {
					if (elision != 8) throw new IllegalArgumentException("illegal second elision at index %s".formatted(this.index));

					this.advance();
					elision = octet += address[octet] == -1 ? 0 : 1;
				} else {
					if (this.index == index) throw new IllegalArgumentException("illegal colon at index %s".formatted(this.index));
				}

				if (++octet == 8) throw new IllegalArgumentException("too many fields");

				index = this.index + 1;
			} else if (hex(this.character)) {
				if (this.index - index >= 4) throw new IllegalArgumentException("field at index %s exceeds 4 octets".formatted(index));

				address[octet] = (short) (Math.max(0, address[octet]) * 16 + Character.digit(this.character, 16));
			} else {
				throw new IllegalArgumentException("invalid character '%c' in IPv6 address at index %s".formatted(this.character, this.index));
			}

			if (!this.advance()) throw new IllegalArgumentException("unterminated IPv6 address");
		}

		var compact = new short[8];

		for (index = 0; index < elision; ++index) compact[index] = (short) address[index];
		for (index = octet - (address[octet] == -1 ? 1 : 0), octet = 7; index > elision; --index, --octet) compact[octet] = (short) address[index];

		return new IPv6Address(this.normalize ? null : this.uri.substring(start, this.index), compact);
	}

	private IPv4Address ip4() {
		var address = new int[]{-1, -1, -1, -1};
		var octet = 0;

		for (;;) {
			if (this.character == '.') {
				++octet;
			} else if (numeric(this.character)) {
				if (address[octet] == 0) {
					break;
				}

				var value = this.character - '0';

				if (address[octet] == -1) {
					address[octet] = value;
				} else if (255 < (address[octet] = address[octet] * 10 + value)) {
					break;
				}
			} else {
				break;
			}

			if (!this.advance() || this.in(":/?#")) {
				if (octet == 3 && address[octet] != -1) {
					return new IPv4Address(address[3] | address[2] << 8 | address[1] << 16 | address[0] << 24);
				}

				break;
			}
		}

		return null;
	}

	private Authority authority() {
		var index = this.start;
		UserInfo userinfo = null;
		Host host;
		var port = -1;

		if (null == (host = this.ipLiteral())) A: {
			var colon = 0;
			var colonEnd = 0;

			do {
				if (this.character == '@') {
					var source = this.normalize && colon != 0 ? null : this.uri.substring(index, this.index);
					userinfo = new UserInfo(
						this.normalize ? null : source,
						colon == 0 ? source : this.decodeSubstring(index, colon),
						colon == 0 ? null : this.decodeSubstring(colonEnd + 1, this.index)
					);

					if (this.advance() && null != (host = this.ipLiteral())) {
						break A;
					}

					index = this.index;
					break;
				}

				if (this.value == ':') {
					if (colon == 0) {
						colon = this.start;
						colonEnd = this.index;
					}
				} else if (!this.ups()) {
					if (!this.in("/?#")) throw this.illegalCharacter();

					break;
				}
			} while (this.advanceEncoded());

			this.index = index - 1;

			if (this.advance() && null != (host = this.ip4())) {
				break A;
			}

			while (!this.in(":/?#") && this.advance()) {}

			host = new Name(this.normalize ? null : this.uri.substring(index, this.index), this.decodeSubstring(index, this.index).toLowerCase(Locale.ROOT));
		}

		if (this.character == ':') {
			index = this.index + 1;

			while (this.advance()) {
				if (this.in("/?#")) break;
				if (!numeric(this.character)) throw new IllegalArgumentException("non-digit '%c' in port at index %s".formatted(this.character, this.index));
			}

			if (this.index != index) {
				port = Integer.parseInt(this.uri.substring(index, this.index));
			}
		}

		return new Authority(userinfo, host, port);
	}

	private boolean ups() {
		return us(this.character) || this.skipEscape();
	}

	private boolean advanceEncoded() {
		if (this.advance()) {
			this.skipEscape();
			return true;
		}

		this.start = this.index;
		return false;
	}

	private boolean skipEscape() {
		this.start = this.index;

		if (this.character == '%') {
			this.value = 0;

			for (var x = 0; x < 2; ++x) {
				if (!this.advance() || !hex(this.character)) throw new IllegalArgumentException("hexadecimal character expected in escape at index " + this.index);
				this.value = this.value * 16 + Character.digit(this.character, 16);
			}

			return true;
		}

		this.value = this.character;
		return false;
	}

	private String decodeSubstring(int start, int end) {
		return Uri.decode(this.uri, start, end);
	}

	private boolean in(String characters) {
		return characters.indexOf(this.character) >= 0;
	}

	private boolean peek(int character) {
		var index = this.index + 1;
		return index < this.uri.length() && character == this.uri.charAt(index);
	}

	private boolean peek(String characters) {
		var index = this.index + 1;
		return index < this.uri.length() && characters.indexOf(this.uri.charAt(index)) >= 0;
	}

	private boolean hasNext() {
		return this.index + 1 < this.uri.length();
	}

	private boolean finished() {
		return this.index >= this.uri.length();
	}

	private boolean advance(int character) {
		if (this.peek(character)) {
			++this.index;
			this.character = character;

			return true;
		}

		return false;
	}

	private boolean advance() {
		if (++this.index >= this.uri.length()) {
			this.index = this.uri.length();
			return false;
		}

		this.character = this.uri.charAt(this.index);
		return true;
	}
}
