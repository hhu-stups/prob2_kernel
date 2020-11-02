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
		if (os == OsFamily.WINDOWS) {
			dirName = "win64";
			cliName = "probcli.exe";
			userInterruptCmd = "lib\\send_user_interrupt.exe";
			cspmfName = "lib\\cspmf.exe";
		} else {
			if (os == OsFamily.MACOS) {
				dirName = "leopard64";
			} else if (os == OsFamily.LINUX) {
				dirName = "linux64";
			} else {
				throw new AssertionError("Unhandled operating system: " + os);
			}
			cliName = "probcli.sh";
			userInterruptCmd = "send_user_interrupt";
			cspmfName = "lib/cspmf";
		}
		
		final String binariesZipResourceName = CLI_BINARIES_RESOURCE_PREFIX + dirName + ".zip";
		return new OsSpecificInfo(binariesZipResourceName, cliName, userInterruptCmd, cspmfName);
	}
}
