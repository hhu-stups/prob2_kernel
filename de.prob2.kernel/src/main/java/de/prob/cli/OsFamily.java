package de.prob.cli;

enum OsFamily {
	WINDOWS,
	MACOS,
	LINUX,
	;
	
	static OsFamily fromName(final String osName) {
		final String os = osName.toLowerCase();
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
}
