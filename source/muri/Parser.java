package muri;

import java.util.Arrays;

class Parser {
	private final String uri;
	private int index = -1;
	private int character;

	Parser(String uri) {
		this.uri = uri;
	}

	Uri parse() {
		if (this.advance()) {
			if (this.character == ':') throw new IllegalArgumentException("empty scheme");
			if (!alpha(this.character)) throw new IllegalArgumentException("0th character ('%c') is not A-z".formatted(this.character));
		} else {
			throw new IllegalArgumentException("empty URI");
		}

		while (true) {
			if (!this.advance()) throw new IllegalArgumentException("':' missing");
			if (this.character == ':') break;
			if (!schemePart(this.character)) throw new IllegalArgumentException("non-scheme ([A-Za-z\\d+-.]) character ('%c') at index %s".formatted(this.character, this.index));
		}

		var scheme = this.uri.substring(0, this.index);
		UserInfo userinfo = null;
		Host host = null;
		var port = -1;

		if (this.advance()) {
			if (this.character == '/') {
				if (this.advance() && this.character == '/') {
					if (this.advance()) {
						var index = this.index;

						if (null == (host = this.ipLiteral())) A: {
							var colon = 0;

							do {
								if (this.character == '@') {
									if (colon == 0) {
										colon = this.index;
									}

									userinfo = new UserInfo(this.uri.substring(index, colon == 0 ? this.index : colon), colon == 0 ? null : this.uri.substring(colon + 1, this.index));
									index = this.index + 1;

									break;
								}

								if (this.character == ':') {
									if (colon == 0) {
										colon = this.index;
									}
								} else if (!this.ups()) {
									if (this.character != '/') throw new IllegalArgumentException("illegal character '%c' at index %s".formatted(this.character, this.index));

									break;
								}
							} while (this.advance());

							this.index = index - 1;

							if (this.advance() && null == (host = this.ipLiteral())) {
								var ipAddress = new int[]{-1, -1, -1, -1};
								var octet = 0;

								while (true) {
									if (this.character == '.') {
										++octet;
									} else if (numeric(this.character)) {
										if (ipAddress[octet] == 0) {
											break;
										}

										var value = this.character - '0';

										if (ipAddress[octet] == -1) {
											ipAddress[octet] = value;
										} else if (255 < (ipAddress[octet] = ipAddress[octet] * 10 + value)) {
											break;
										}

										if (octet == 3) {
											octet = -1;
										}
									} else {
										break;
									}

									if (this.advance() && this.character != ':' && this.character != '/') {
										if (octet == -1) {
											break;
										}
									} else {
										if (octet == -1) {
											host = new IPv4Address(ipAddress[3] | ipAddress[2] << 8 | ipAddress[1] << 16 | ipAddress[0] << 24);
											break A;
										}

										break;
									}
								}

								while (this.character != ':' && this.character != '/' && this.advance()) {}

								host = new RegisteredName(this.uri.substring(index, this.index));
							}
						}

						if (this.character == ':') {
							index = this.index + 1;

							while (this.advance()) {
								if (!numeric(this.character)) throw new IllegalArgumentException("non-digit '%c' in port at index %s".formatted(this.character, this.index));
								if (this.character == '/') break;
							}

							if (this.index != index) {
								port = Integer.parseInt(this.uri.substring(index, this.index));
							}
						}
					}
				}
			}
		}

		return new Uri(scheme, new Authority(userinfo, host, port));
	}

	private static boolean schemePart(int character) {
		return part(character) || character == '+';
	}

	private static boolean unreserved(int character) {
		return part(character) || character == '_' || character == '~';
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

	private static boolean reserved(int character) {
		return genDelim(character) || subDelim(character);
	}

	private static boolean subDelim(int character) {
		return switch (character) {
			case '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=' -> true;
			default -> false;
		};
	}

	private static boolean genDelim(int character) {
		return switch (character) {
			case ':', '/', '?', '#', '[', ']', '@' -> true;
			default -> false;
		};
	}

	private static boolean us(int character) {
		return unreserved(character) || subDelim(character);
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

		return new IPvFutureAddress(version, this.uri.substring(index, this.index));
	}

	private IPv6Address ip6() {
		var index = this.index;
		var address = new int[]{-1, -1, -1, -1, -1, -1, -1, -1};
		var elision = -1;
		var octet = 0;

		for (;;) {
			if (this.character == ']') {
				if (elision == -1 && octet < 7 && address[octet] == -1) throw new IllegalArgumentException("incomplete IPv6 address");

				break;
			}

			if (this.character == ':') {
				if (this.peek(':')) {
					if (elision != -1) throw new IllegalArgumentException("illegal second elision at index %s".formatted(this.index));

					this.advance();
					elision = octet;
				} else {
					if (this.index == index) throw new IllegalArgumentException("illegal colon at index %s".formatted(this.index));
				}

				if (++octet == 8) throw new IllegalArgumentException("too many fields");

				index = this.index + 1;
			} else if (hex(this.character)) {
				if (this.index - index >= 4) throw new IllegalArgumentException("field at index %s exceeds 4 octets".formatted(index));
				if (elision != -1 && octet == 7) throw new IllegalArgumentException("too many fields");

				var value = address[octet];
				var digit = Character.digit(this.character, 16);
				address[octet] = value == -1 ? digit : (short) (value * 16 + digit);
			} else {
				throw new IllegalArgumentException("invalid character '%c' in IPv6 address at index %s".formatted(this.character, this.index));
			}

			if (!this.advance()) throw new IllegalArgumentException("unterminated IPv6 address");
		}

		if (elision != -1) {
			var firstZero = elision + (address[elision] == -1 ? 0 : 1);
			var length = address[firstZero] == -1 ? 0 : octet - elision;
			var start = address.length - length;
			System.arraycopy(address, elision + 1, address, start, length);
			Arrays.fill(address, firstZero, start, 0);
		}

		var compact = new short[8];

		for (index = 0; index < 8; ++index) {
			compact[index] = (short) address[index];
		}

		return new IPv6Address(compact);
	}

	private boolean ups() {
		return us(this.character) || this.skipEscape();
	}

	private boolean advanceEncoded() {
		if (this.advance()) {
			this.skipEscape();
			return true;
		}

		return false;
	}

	private boolean skipEscape() {
		if (this.character == '%') {
			for (var e = 0; e < 2; ++e) {
				if (!this.advance() || !hex(this.character)) throw new IllegalArgumentException("hexadecimal character expected in escape at index " + this.index);
			}

			return true;
		}

		return false;
	}

	private boolean peek(int character) {
		var index = this.index + 1;
		return index < this.uri.length() && character == this.uri.charAt(index);
	}

	private boolean advance() {
		if (++this.index >= this.uri.length()) {
			return false;
		}

		this.character = this.uri.codePointAt(this.index);
		return true;
	}
}
