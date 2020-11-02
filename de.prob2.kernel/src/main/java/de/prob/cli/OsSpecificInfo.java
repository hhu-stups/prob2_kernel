package de.prob.cli;

/**
 * The OsSpecificInfo takes on the following form based on the present Operating
 * System. Supports Windows, Mac, and Linux.
 * 
 * @author joy
 * 
 */
public class OsSpecificInfo {
	private final String binariesZipResourceName;
	private final String cliName;
	private final String userInterruptCmd;
	private final String cspmfName;

	public OsSpecificInfo(
		final String binariesZipResourceName,
		final String cliName,
		final String userInterruptCmd,
		final String cspmfName
	) {
		this.binariesZipResourceName = binariesZipResourceName;
		this.cliName = cliName;
		this.userInterruptCmd = userInterruptCmd;
		this.cspmfName = cspmfName;
	}

	public String getBinariesZipResourceName() {
		return this.binariesZipResourceName;
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

	public String getCspmfName() {
		return this.cspmfName;
	}
}
