package de.prob.cli;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Creates {@link OsSpecificInfo} for each instance of the ProB 2.0 software.
 * This is determined from the System settings. The resulting
 * {@link OsSpecificInfo} can be injected into any desired class.
 * 
 * @author joy
 * 
 */
@Singleton
class OsInfoProvider implements Provider<OsSpecificInfo> {
	private static final String CLI_BINARIES_RESOURCE_PREFIX = "/de/prob/cli/binaries/";

	private final OsSpecificInfo osInfo;

	@Inject
	OsInfoProvider(final OsFamily osFamily) {
		osInfo = makeOsInfo(osFamily);
	}

	@Override
	public OsSpecificInfo get() {
		return osInfo;
	}

	private static OsSpecificInfo makeOsInfo(final OsFamily os) {
		final String dirName;
		final String cliName;
		final String userInterruptCmd;
		final String cspmfName;
		final String fuzzName;
		if (os == OsFamily.WINDOWS) {
			dirName = "win64";
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
}
