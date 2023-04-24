package de.prob2.commandline;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptException;

import ch.qos.logback.classic.ClassicConstants;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;

import de.prob.MainModule;

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
	
	@Inject
	private CommandLineMain(final CommandLineParser parser, final Options options) {
		this.parser = parser;
		this.options = options;
		logger.debug("Java version: {}", System.getProperty("java.version"));
	}
	
	private void run(final String[] args) {
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
		
		final MainModule mainModule = new MainModule();
		
		if (line.hasOption("maxCacheSize")) {
			logger.debug("setting maximum cache size requested");
			String value = line.getOptionValue("maxCacheSize");
			logger.debug("retrieved maxSize");
			mainModule.setMaxCacheSize(Integer.parseInt(value));
			logger.debug("Max size set successfully to {}", value);
		}
		
		final Injector proBInjector = Guice.createInjector(Stage.PRODUCTION, mainModule);
		
		if (line.hasOption("script")) {
			logger.debug("Run Script");
			String value = line.getOptionValue("script");
			try {
				proBInjector.getInstance(Shell.class).runScript(new File(value), false);
			} catch (IOException | ScriptException e) {
				logger.error("Exception while executing script", e);
				System.exit(-1);
			}
		}
	}

	/**
	 * Start the ProB 2.0 shell with argument -s. Run integration tests with -test /path/to/testDir
	 *
	 * @param args command-line arguments
	 */
	public static void main(final String[] args) {
		if (!System.getProperties().containsKey(ClassicConstants.CONFIG_FILE_PROPERTY)) {
			System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, "de/prob/logging/production.xml");
		}
		logger = LoggerFactory.getLogger(CommandLineMain.class);
		
		final Injector cliInjector = Guice.createInjector(Stage.PRODUCTION, new CommandLineModule());
		cliInjector.getInstance(CommandLineMain.class).run(args);
		System.exit(0);
	}
}
