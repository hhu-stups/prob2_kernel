package de.prob.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

import de.prob.annotations.Home;
import de.prob.exception.CliError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PrologProcessProvider {
	private static final Logger logger = LoggerFactory.getLogger(PrologProcessProvider.class);
	private static final List<Process> toDestroyOnShutdown = Collections.synchronizedList(new ArrayList<>());

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (toDestroyOnShutdown) {
				for (final Process process : toDestroyOnShutdown) {
					process.destroy();
				}
			}
		}, "Prolog Process Destroyer"));
	}

	private final String dir;
	private final OsSpecificInfo osInfo;

	@Inject
	PrologProcessProvider(final OsSpecificInfo osInfo, @Home final String path) {
		this.osInfo = osInfo;
		// Create ProB home directory if necessary.
		new File(path).mkdirs();
		dir = path;

	}

	Process makeProcess() {
		final String executable = dir + osInfo.getCliName();
		List<String> command = makeCommand(executable);
		final ProcessBuilder pb = new ProcessBuilder();
		pb.command(command);
		pb.environment().put("TRAILSTKSIZE", "1M");
		pb.environment().put("PROLOGINCSIZE", "50M");
		pb.environment().put("PROB_HOME", dir);
		pb.redirectErrorStream(true);
		final Process prologProcess;
		try {
			logger.info("\nStarting ProB's Prolog Core. Path is {}", executable);
			prologProcess = pb.start();
			logger.debug("probcli -sf started");
		} catch (IOException e) {
			throw new CliError("Problem while starting up ProB CLI: " + e.getMessage(), e);
		}

		toDestroyOnShutdown.add(prologProcess);
		return prologProcess;
	}

	private List<String> makeCommand(final String executable) {
		List<String> command = new ArrayList<>();
		command.add(executable);
		command.add("-sf");
		return command;
	}
}
