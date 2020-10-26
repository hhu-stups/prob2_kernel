package de.prob.cli;

/**
 * The OsSpecificInfo takes on the following form based on the present Operating
 * System. Supports Windows, Mac, and Linux.
 * 
 * @author joy
 * 
 */
public class OsSpecificInfo {
	private final String cliZipResourceName;
	private final String libsZipResourceName;
	private final String cspmfResourceName;
	private final String cliName;
	private final String userInterruptCmd;
	private final String dirName;
	private final String cspmfName;

	public OsSpecificInfo(
		final String cliZipResourceName, final String libsZipResourceName, final String cspmfResourceName, final String cliName,
		final String userInterruptCmd,
		final String dirName,
		final String cspmfName
	) {
		this.cliZipResourceName = cliZipResourceName;
		this.libsZipResourceName = libsZipResourceName;
		this.cspmfResourceName = cspmfResourceName;
		this.cliName = cliName;
		this.userInterruptCmd = userInterruptCmd;
		this.dirName = dirName;
		this.cspmfName = cspmfName;
	}

	public String getCliZipResourceName() {
		return cliZipResourceName;
	}

	public String getLibsZipResourceName() {
		return libsZipResourceName;
	}

	public String getCspmfResourceName() {
		return cspmfResourceName;
	}

	/**
	 * @return cliName - Windows: "probcli.exe", Mac and Linux: "probcli.sh"
	 */
	public String getCliName() {
		return cliName;
	}

	/**
	 * @return userInterruptCmd - Windows: "send_user_interrupt.exe", Mac and
	 *         Linux: "send_user_interrupt"
	 */
	public String getUserInterruptCmd() {
		return userInterruptCmd;
	}

	/**
	 * @return dirName Windows: "win64", Mac: "leopard64", Linux: "linux64"
	 */
	public String getDirName() {
		return dirName;
	}
	
	public String getCspmfName() {
		return this.cspmfName;
	}
}
