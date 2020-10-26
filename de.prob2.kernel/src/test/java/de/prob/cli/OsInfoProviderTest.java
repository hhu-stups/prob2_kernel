package de.prob.cli;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class OsInfoProviderTest {
	private static final String LINUX = "linux64";
	private static final String MAC = "leopard64";
	private static final String WIN = "win64";

	private static final Map<String, String> SUPPORTED = new HashMap<>();
	static {
		SUPPORTED.put("Linux", LINUX);
		SUPPORTED.put("Mac OS", MAC);
		SUPPORTED.put("Mac OS X", MAC);
		SUPPORTED.put("Windows 95", WIN);
		SUPPORTED.put("Windows 98", WIN);
		SUPPORTED.put("Windows Me", WIN);
		SUPPORTED.put("Windows NT", WIN);
		SUPPORTED.put("Windows 2000", WIN);
		SUPPORTED.put("Windows 2003", WIN);
		SUPPORTED.put("Windows XP", WIN);
		SUPPORTED.put("Windows CE", WIN);
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
		SUPPORTED.forEach((key, value) -> assertEquals(value, new OsInfoProvider(key).get().getDirName()));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnsupportedOS() {
		for (String string : UNSUPPORTED) {
			new OsInfoProvider(string).get();
		}
	}

}
