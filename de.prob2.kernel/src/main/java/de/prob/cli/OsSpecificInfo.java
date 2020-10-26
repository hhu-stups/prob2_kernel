package de.prob.cli;

/**
 * The OsSpecificInfo takes on the following form based on the present Operating
 * System. Supports Windows, Mac, and Linux.
 * 
 * @author joy
 * 
 */
public class OsSpecificInfo {
	private final String cliName;
	private final String userInterruptCmd;
	private final String name;
	private final String dirName;
	private final String cspmfName;

	public OsSpecificInfo(
		final String cliName,
		final String userInterruptCmd,
		final String name,
		final String dirName,
		final String cspmfName
	) {
		this.cliName = cliName;
		this.userInterruptCmd = userInterruptCmd;
		this.name = name;
		this.dirName = dirName;
		this.cspmfName = cspmfName;
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
	 * @return name Windows: "Windows", Mac: "MacOs", Linux: "Linux"
	 */
	public String getName() {
		return name;
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
