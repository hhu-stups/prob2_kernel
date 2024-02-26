package de.prob.cli;

/**
 * The OsSpecificInfo takes on the following form based on the present Operating
 * System. Supports Windows, Mac, and Linux.
 *
 * @author joy
 */
public final class OsSpecificInfo {
	private static final String CLI_BINARIES_RESOURCE_PREFIX = "/de/prob/cli/binaries/";

	private static OsSpecificInfo detected = null;

	private final String binariesZipResourceName;
	private final String cliName;
	private final String userInterruptCmd;
	private final String cspmfName;
	private final String fuzzName;

	public OsSpecificInfo(
		final String binariesZipResourceName,
		final String cliName,
		final String userInterruptCmd,
		final String cspmfName,
		final String fuzzName
	) {
		this.binariesZipResourceName = binariesZipResourceName;
		this.cliName = cliName;
		this.userInterruptCmd = userInterruptCmd;
		this.cspmfName = cspmfName;
		this.fuzzName = fuzzName;
	}

	// Note: we may need to re-add architecture detection in the future,
	// e. g. to support arm64 on non-macOS systems.
	// So please don't make this public yet, or ever, unless there's a good reason for it.
	// We may need to add more arguments in the future.
	private static OsSpecificInfo forOsFamily(OsFamily os) {
		String dirName;
		String cliName;
		String userInterruptCmd;
		String cspmfName;
		String fuzzName;
		if (os == OsFamily.WINDOWS) {
			dirName = "windows64";
			cliName = "probcli.exe";
			userInterruptCmd = "lib\\send_user_interrupt.exe";
			cspmfName = "lib\\cspmf.exe";
			fuzzName = "lib\\fuzz.exe";
		} else {
			if (os == OsFamily.MACOS) {
				dirName = "macos";
			} else if (os == OsFamily.LINUX) {
				dirName = "linux64";
			} else {
				throw new AssertionError("Unhandled operating system: " + os);
			}
			cliName = "probcli.sh";
			// Since ProB 1.12.0, send_user_interrupt is in the lib directory on all platforms.
			// On older versions, it was in the lib directory only on Windows
			// and outside the lib directory on macOS and Linux.
			userInterruptCmd = "lib/send_user_interrupt";
			cspmfName = "lib/cspmf";
			fuzzName = "lib/fuzz";
		}

		final String binariesZipResourceName = CLI_BINARIES_RESOURCE_PREFIX + "probcli_" + dirName + ".zip";
		return new OsSpecificInfo(binariesZipResourceName, cliName, userInterruptCmd, cspmfName, fuzzName);
	}

	public static OsSpecificInfo detect() {
		synchronized (OsSpecificInfo.class) {
			if (detected == null) {
				detected = OsSpecificInfo.forOsFamily(OsFamily.detect());
			}
			return detected;
		}
	}

	public String getBinariesZipResourceName() {
		return this.binariesZipResourceName;
	}

	/**
	 * @return cliName - Windows: "probcli.exe", Mac and Linux: "probcli.sh"
	 */
	public String getCliName() {
		return this.cliName;
	}

	/**
	 * @return userInterruptCmd - Windows: "send_user_interrupt.exe", Mac and Linux: "send_user_interrupt"
	 */
	public String getUserInterruptCmd() {
		return this.userInterruptCmd;
	}

	public String getCspmfName() {
		return this.cspmfName;
	}

	public String getFuzzName() {
		return this.fuzzName;
	}
}
