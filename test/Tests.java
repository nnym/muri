import muri.IPvFutureAddress;
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

		var uri = uri("e:");
		assertEquals("e", uri.scheme);
		assertEquals("", uri.path.toString());
		assertNull(uri.authority);
		assertFalse(uri.path.absolute);
		assertTrue(uri.path.isEmpty());

		assertEquals("test", uri("test:").scheme);
		assertEquals("TEST", uri("TEST:").scheme);
		assertEquals("TeST1+-.", uri("TeST1+-.:").scheme);
		assertEquals("TeST1.2+-3", uri("TeST1.2+-3:").scheme);

		uri = uri("test://googol");
		assertEquals("test", uri.scheme);
		assertEquals("googol", uri.authority.host.toString());
		assertTrue(uri.path.isEmpty());

		uri = uri("test://user:pass:@googol:65536");
		assertEquals("test", uri.scheme);
		assertEquals("user", uri.authority.userinfo.user);
		assertEquals("pass:", uri.authority.userinfo.password);
		assertEquals("googol", uri.authority.host.toString());
		assertEquals(65536, uri.authority.port);
		assertTrue(uri.path.isEmpty());

		uri = uri("test://a:b@[vDeadBeef.milkyWay-earth_nauru+arijejen('country-street')!)]:123");
		assertEquals("test", uri.scheme);
		assertEquals("a:b", uri.authority.userinfo.toString());

		var address = (IPvFutureAddress) uri.authority.host;
		assertEquals(0xDEADBEEFL, address.version);
		assertEquals("milkyWay-earth_nauru+arijejen('country-street')!)", address.address);
		assertEquals("vdeadbeef.milkyWay-earth_nauru+arijejen('country-street')!)", address.toString());
		assertEquals(123, uri.authority.port);
		assertTrue(uri.path.isEmpty());

		uri = uri("test://:user:pass:@[A123:b:c234:d:e:f:1:2]:65536");
		assertEquals("test", uri.scheme);
		assertEquals("", uri.authority.userinfo.user);
		assertEquals("user:pass:", uri.authority.userinfo.password);
		assertEquals("a123:b:c234:d:e:f:1:2", uri.authority.host.toString());
		assertEquals(65536, uri.authority.port);
		assertTrue(uri.path.isEmpty());

		uri = uri("e:/");
		assertEquals("/", uri.path.toString());
		assertTrue(uri.path.absolute);

		uri = uri("test://googol/");
		assertEquals("test", uri.scheme);
		assertEquals("googol", uri.authority.host.toString());
		assertEquals("/", uri.path.toString());
		assertFalse(uri.path.isEmpty());
		assertTrue(uri.path.absolute);

		uri = uri("test://googol:40/");
		assertEquals("test", uri.scheme);
		assertEquals("googol", uri.authority.host.toString());
		assertEquals("/", uri.path.toString());
		assertEquals(40, uri.authority.port);
		assertFalse(uri.path.isEmpty());
		assertTrue(uri.path.absolute);

		uri = uri("test://a:b@192.168.0.1:40/1998/12/12.md");
		assertEquals("test", uri.scheme);
		assertEquals("a", uri.authority.userinfo.user);
		assertEquals("b", uri.authority.userinfo.password);
		assertEquals("192.168.0.1", uri.authority.host.toString());
		assertEquals(40, uri.authority.port);
		assertEquals("/1998/12/12.md", uri.path.toString());
		assertEquals(3, uri.path.segments.size());
		assertTrue(uri.path.absolute);

		uri = uri("file:/email/a@b.net/re:hello.eml");
		assertEquals("/email/a@b.net/re:hello.eml", uri.path.toString());
		assertNull(uri.authority);
		assertEquals(3, uri.path.segments.size());
		assertTrue(uri.path.absolute);

		uri = uri("file2:gradle/wrapper/gradle-wrapper.properties");
		assertEquals("file2", uri.scheme);
		assertNull(uri.authority);
		assertEquals("gradle/wrapper/gradle-wrapper.properties", uri.path.toString());
		assertEquals(3, uri.path.segments.size());
		assertFalse(uri.path.absolute);

		uri = uri("test:?flag&123field=!$'()*+,;&b=true&c==false");
		var query = uri.query;
		assertEquals("test", uri.scheme);
		assertEquals("", uri.path.toString());
		assertNull(uri.authority);
		assertEquals("flag&123field=!$'()*+,;&b=true&c==false", query.toString());
		assertFalse(query.has("f"));
		assertTrue(query.has("flag") && query.has("123field") && query.has("b") && query.has("c"));
		assertNull(query.value("flag"));
		assertEquals("!$'()*+,;", query.value("123field"));
		assertEquals("true", query.value("b"));
		assertEquals("=false", query.value("c"));

		uri = uri("test:/1998/12/?a=1&b=2");
		query = uri.query;
		assertEquals("test", uri.scheme);
		assertEquals("/1998/12/", uri.path.toString());
		assertEquals("a=1&b=2", query.toString());
		assertEquals("1", query.value("a"));
		assertEquals("2", query.value("b"));

		uri = uri("test://user:pass@/1998/12/?a=1");
		query = uri.query;
		assertEquals("test", uri.scheme);
		assertEquals("user:pass", uri.authority.userinfo.toString());
		assertEquals("", uri.authority.host.toString());
		assertEquals("/1998/12/", uri.path.toString());
		assertEquals("a=1", query.toString());
		assertEquals("1", query.value("a"));

		uri = uri("test:1998/12/?a=1");
		assertEquals("test", uri.scheme);
		assertEquals("1998/12/", uri.path.toString());
		assertEquals("1", uri.query.value("a"));

		uri = uri("test:1998/12/12.md?a=1");
		assertEquals("1998/12/12.md", uri.path.toString());
		assertEquals("1", uri.query.value("a"));

		uri = uri("test:#frag");
		assertEquals("", uri.path.toString());
		assertEquals("frag", uri.fragment);

		uri = uri("test://#frag");
		assertEquals("", uri.path.toString());
		assertEquals("", uri.authority.toString());
		assertEquals("frag", uri.fragment);

		uri = uri("test://a:b@example.net?a=1#frag");
		assertEquals("a:b@example.net", uri.authority.toString());
		assertEquals("", uri.path.toString());
		assertEquals("a=1", uri.query.toString());
		assertEquals("frag", uri.fragment);

		uri = uri("test://a:b@example.net/path?a=1#frag");
		assertEquals("a:b@example.net", uri.authority.toString());
		assertEquals("/path", uri.path.toString());
		assertEquals("a=1", uri.query.toString());
		assertEquals("frag", uri.fragment);

		uri = uri("test:1998/12/12.md?a=1#:@frag?/!$&'()*+,;=_~-.123");
		assertEquals("1998/12/12.md", uri.path.toString());
		assertEquals("1", uri.query.value("a"));
		assertEquals(":@frag?/!$&'()*+,;=_~-.123", uri.fragment);

		var bp = true;
	}
}
