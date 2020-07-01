package de.prob;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import de.prob.clistarter.ProBInstanceProvider;
import de.prob.scripting.ScriptEngineProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

class Shell {

	private final ScriptEngineProvider sep;
	private final Logger logger = LoggerFactory.getLogger(Shell.class);
	private final ProBInstanceProvider proBs;

	@Inject
	public Shell(final ScriptEngineProvider sep, final ProBInstanceProvider proBs) {
		this.sep = sep;
		this.proBs = proBs;
	}

	public void runScript(final File script, final boolean silent) throws IOException, ScriptException {
		if (script.isDirectory()) {
			final File[] files = script.listFiles((dir, name) -> name.endsWith(".groovy"));
			if (files != null) {
				final Stopwatch stopwatch = Stopwatch.createStarted();
				for (File file : files) {
					runScript(file, silent);
				}
				stopwatch.stop();
				if (!silent) {
					final double seconds = stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0;
					System.out.printf("TOTAL TIME: %.4g s%n", seconds);
				}
			}
		} else {
			runSingleScript(script.getAbsoluteFile().getParent(), script, silent);
		}
	}

	private void runSingleScript(final String dir, final File scriptFile, final boolean silent) throws IOException, ScriptException {
		final Stopwatch stopwatch = Stopwatch.createStarted();
		logger.debug("Running script: {}", scriptFile.getAbsolutePath());
		ScriptEngine executor = sep.get();
		executor.put("dir", dir);
		executor.put("inConsole", false);

		if (!silent) {
			System.out.print(scriptFile.getName());
		}
		final Object res;
		try (FileReader fr = new FileReader(scriptFile)) {
			res = executor.eval(fr);
		} catch (IOException e) {
			System.err.printf("Could not read script %s: %s%n", scriptFile, e);
			logger.error("Could not read script", e);
			throw e;
		} catch (ScriptException e) {
			System.err.printf("Exception thrown by script %s: %s%n", scriptFile, e);
			logger.error("Exception thrown by script", e);
			throw e;
		}
		proBs.shutdownAll();
		stopwatch.stop();
		if (!silent) {
			final double seconds = stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0;
			System.out.printf(" - %s (%.4g s)%n", res, seconds);
		}
	}
}
