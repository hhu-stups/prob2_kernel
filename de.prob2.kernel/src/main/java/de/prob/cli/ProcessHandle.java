package de.prob.cli;

/**
 * @deprecated No longer used, because the debugging key {@link #key} has no use anymore. probcli no longer supports the debug_console/2 command.
 */
@Deprecated
public class ProcessHandle {

	private final String key;
	private final Process process;

	public ProcessHandle(final Process process, final String key) {
		this.process = process;
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public Process getProcess() {
		return process;
	}

}
