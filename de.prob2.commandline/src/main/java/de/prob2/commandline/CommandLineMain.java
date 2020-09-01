package de.prob2.commandline;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptException;

import ch.qos.logback.classic.util.ContextInitializer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;

import de.prob.Main;
import de.prob.annotations.Home;
import de.prob.cli.ProBInstanceProvider;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommandLineMain {
	private static Logger logger;
	
	private final CommandLineParser parser;
	private final Options options;
	private final Shell shell;
	
	@Inject
	private CommandLineMain(final CommandLineParser parser, final Options options, final Shell shell, @Home String probdir) {
		this.parser = parser;
		this.options = options;
		this.shell = shell;
		System.setProperty("prob.stdlib", probdir + File.separator + "stdlib");
		logger.debug("Java version: {}", System.getProperty("java.version"));
	}
	
	private void run(final String[] args) throws IOException, ScriptException {
		final CommandLine line;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			logger.debug("Failed to parse CLI", e);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar probcli.jar", options);
			System.exit(-1);
			throw new AssertionError("System.exit shouldn't return", e);
		}
		
		if (line.hasOption("maxCacheSize")) {
			logger.debug("setting maximum cache size requested");
			String value = line.getOptionValue("maxCacheSize");
			logger.debug("retrieved maxSize");
			Main.setMaxCacheSize(Integer.valueOf(value));
			logger.debug("Max size set successfully to {}", value);
		}
		
		if (line.hasOption("script")) {
			logger.debug("Run Script");
			String value = line.getOptionValue("script");
			shell.runScript(new File(value), false);
		}
	}

	/**
	 * Start the ProB 2.0 shell with argument -s. Run integration tests with -test /path/to/testDir
	 *
	 * @param args command-line arguments
	 */
	public static void main(final String[] args) {
		if (!System.getProperties().containsKey(ContextInitializer.CONFIG_FILE_PROPERTY)) {
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "de/prob/logging/production.xml");
		}
		logger = LoggerFactory.getLogger(CommandLineMain.class);
		
		final Injector injector = Guice.createInjector(Stage.PRODUCTION, new CommandLineModule());
		Main.setInjector(injector);
		final CommandLineMain main = injector.getInstance(CommandLineMain.class);
		try {
			main.run(args);
		} catch (IOException | ScriptException e) {
			logger.error("Unhandled exception", e);
			System.exit(-1);
		} finally {
			injector.getInstance(ProBInstanceProvider.class).shutdownAll();
		}
		System.exit(0);
	}
}
