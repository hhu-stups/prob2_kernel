package de.prob.cli;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.prob.cli.ModuleCli.OsName;

import java.io.File;

/**
 * Creates {@link OsSpecificInfo} for each instance of the ProB 2.0 software.
 * This is determined from the System settings. The resulting
 * {@link OsSpecificInfo} can be injected into any desired class.
 * 
 * @author joy
 * 
 */
@Singleton
public class OsInfoProvider implements Provider<OsSpecificInfo> {
	private static final String CLI_BINARIES_RESOURCE_PREFIX = "/de/prob/cli/binaries/";

	private final OsSpecificInfo osInfo;

	@Inject
	public OsInfoProvider(@OsName final String osString) {
		osInfo = whichOs(osString);
	}

	@Override
	public OsSpecificInfo get() {
		return osInfo;
	}

	private static OsSpecificInfo whichOs(final String osString) {
		final String os = osString.toLowerCase();
		if (os.contains("win")) {
			return new OsSpecificInfo(
				CLI_BINARIES_RESOURCE_PREFIX + "probcli_win64.zip",
				CLI_BINARIES_RESOURCE_PREFIX + "windowslib64.zip",
				CLI_BINARIES_RESOURCE_PREFIX + "windows-cspmf.exe",
				"probcli.exe",
				"lib" + File.separator + "send_user_interrupt.exe",
				"win64",
				"lib\\cspmf.exe"
			);
		} else if (os.contains("mac")) {
			return new OsSpecificInfo(
				CLI_BINARIES_RESOURCE_PREFIX + "probcli_leopard64.zip",
				null,
				CLI_BINARIES_RESOURCE_PREFIX + "leopard64-cspmf",
				"probcli.sh",
				"send_user_interrupt",
				"leopard64",
				"lib/cspmf"
			);
		} else if (os.contains("linux")) {
			return new OsSpecificInfo(
				CLI_BINARIES_RESOURCE_PREFIX + "probcli_linux64.zip",
				null,
				CLI_BINARIES_RESOURCE_PREFIX + "linux64-cspmf",
				"probcli.sh",
				"send_user_interrupt",
				"linux64",
				"lib/cspmf"
			);
		} else {
			throw new UnsupportedOperationException("Unsupported operating system: " + osString);
		}
	}
}
