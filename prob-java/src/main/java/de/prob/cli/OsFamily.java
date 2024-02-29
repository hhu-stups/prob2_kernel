package de.prob.cli;

import java.util.Locale;

enum OsFamily {

	WINDOWS,
	MACOS,
	LINUX;

	static OsFamily fromName(final String osName) {
		final String os = osName.toLowerCase(Locale.ROOT);
		if (os.contains("win")) {
			return WINDOWS;
		} else if (os.contains("mac")) {
			return MACOS;
		} else if (os.contains("linux")) {
			return LINUX;
		} else {
			throw new UnsupportedOperationException("Unsupported operating system: " + osName);
		}
	}

	static OsFamily detect() {
		return OsFamily.fromName(System.getProperty("os.name"));
	}
}
