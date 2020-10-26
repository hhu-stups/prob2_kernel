package de.prob.cli;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class OsInfoProviderTest {
	private static final Map<String, OsFamily> SUPPORTED = new HashMap<>();
	static {
		SUPPORTED.put("Linux", OsFamily.LINUX);
		SUPPORTED.put("Mac OS", OsFamily.MACOS);
		SUPPORTED.put("Mac OS X", OsFamily.MACOS);
		SUPPORTED.put("Windows 95", OsFamily.WINDOWS);
		SUPPORTED.put("Windows 98", OsFamily.WINDOWS);
		SUPPORTED.put("Windows Me", OsFamily.WINDOWS);
		SUPPORTED.put("Windows NT", OsFamily.WINDOWS);
		SUPPORTED.put("Windows 2000", OsFamily.WINDOWS);
		SUPPORTED.put("Windows 2003", OsFamily.WINDOWS);
		SUPPORTED.put("Windows XP", OsFamily.WINDOWS);
		SUPPORTED.put("Windows CE", OsFamily.WINDOWS);
	}

	private static final String[] UNSUPPORTED = {
		"OS/2",
		"Solaris",
		"SunOS",
		"MPE/iX",
		"HP-UX",
		"AIX",
		"OS/390",
		"FreeBSD",
		"Irix",
		"Digital Unix",
		"NetWare 4.11",
		"OSF1",
		"OpenVMS",
	};

	@Test
	public void testSupportedOS() {
		SUPPORTED.forEach((key, value) -> assertEquals(value, OsFamily.fromName(key)));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnsupportedOS() {
		for (String string : UNSUPPORTED) {
			OsFamily.fromName(string);
		}
	}

}
