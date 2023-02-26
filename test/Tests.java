import muri.IPvFutureAddress;
import muri.Uri;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import static org.junit.jupiter.api.Assertions.*;
import static muri.Uri.*;

@Testable
public class Tests {
	@Test void test() {
		assertThrows(IllegalArgumentException.class, () -> uri(""));
		assertThrows(IllegalArgumentException.class, () -> uri(":"));
		assertThrows(IllegalArgumentException.class, () -> uri("1:"));
		assertThrows(IllegalArgumentException.class, () -> uri("+:"));
		assertThrows(IllegalArgumentException.class, () -> uri("-:"));
		assertThrows(IllegalArgumentException.class, () -> uri(".:"));

		assertEquals("e", uri("e:").scheme);
		assertEquals("test", uri("test:").scheme);
		assertEquals("TEST", uri("TEST:").scheme);
		assertEquals("TeST1+-.", uri("TeST1+-.:").scheme);
		assertEquals("TeST1.2+-3", uri("TeST1.2+-3:").scheme);

		var uri = uri("test://googol");
		assertEquals("test", uri.scheme);
		assertEquals("googol", uri.authority.host.toString());

		uri = uri("test://user:pass:@googol:65536");
		assertEquals("test", uri.scheme);
		assertEquals("user", uri.authority.userinfo.user);
		assertEquals("pass:", uri.authority.userinfo.password);
		assertEquals("googol", uri.authority.host.toString());
		assertEquals("65536", String.valueOf(uri.authority.port));

		uri = uri("test://a:b@[vDeadBeef.milkyWay-earth_nauru+arijejen('country-street')!)]:123");
		assertEquals("test", uri.scheme);
		assertEquals("a:b", uri.authority.userinfo.toString());

		var address = (IPvFutureAddress) uri.authority.host;
		assertEquals(0xDEADBEEFL, address.version);
		assertEquals("milkyWay-earth_nauru+arijejen('country-street')!)", address.address);
		assertEquals("vdeadbeef.milkyWay-earth_nauru+arijejen('country-street')!)", address.toString());
		assertEquals("123", String.valueOf(uri.authority.port));

		uri = uri("test://:user:pass:@[A123:b:c234:d:e:f:1:2]:65536");
		assertEquals("test", uri.scheme);
		assertEquals("", uri.authority.userinfo.user);
		assertEquals("user:pass:", uri.authority.userinfo.password);
		assertEquals("a123:b:c234:d:e:f:1:2", uri.authority.host.toString());
		assertEquals("65536", String.valueOf(uri.authority.port));

		var bp = true;
	}
}
