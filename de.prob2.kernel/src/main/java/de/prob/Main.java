package de.prob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.script.ScriptException;

import ch.qos.logback.classic.util.ContextInitializer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;

import de.prob.animator.IAnimator;
import de.prob.annotations.Home;
import de.prob.cli.Installer;
import de.prob.cli.ProBInstanceProvider;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Main class initializes ProB 2.0. This class should NOT be instantiated
 * but should rather be started from a .jar file, accessed through Guice via
 * {@code ServletContextListener.getInjector().getInstance()} with Main.class as
 * parameter, or started in a jetty server via {@code WebConsole.run()}.
 *
 * @author joy
 */
public class Main {

	private static int maxCacheSize = 100;
	private static Logger logger;
	private final CommandLineParser parser;
	private final Options options;
	private final Shell shell;

	private static Injector injector = null;

	private static final Properties buildProperties;
	static {
		buildProperties = new Properties();
		final InputStream is = Main.class.getResourceAsStream("build.properties");
		if (is == null) {
			throw new IllegalStateException("Build properties not found, this should never happen!");
		} else {
			try (final Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				buildProperties.load(r);
			} catch (IOException e) {
				throw new UncheckedIOException("IOException while loading build properties, this should never happen!", e);
			}
		}
	}

	public static synchronized Injector getInjector() {
		if (injector == null) {
			injector = Guice.createInjector(Stage.PRODUCTION, new MainModule());
		}
		return injector;
	}

	/**
	 * Allows to customize the Injector. Handle with care!
	 *
	 * @param i
	 *            the new injector to use
	 */
	public static synchronized void setInjector(final Injector i) {
		injector = i;
	}

	/**
	 * Parameters are injected by Guice via {@link MainModule}. This class
	 * should NOT be instantiated by hand.
	 *
	 * @param parser
	 *            command-line parser
	 * @param options
	 *            command-line options
	 * @param shell
	 *            ProB shell
	 * @param probdir
	 *            the ProB home directory
	 */
	@Inject
	public Main(final CommandLineParser parser, final Options options, final Shell shell, @Home String probdir) {
		this.parser = parser;
		this.options = options;
		this.shell = shell;
		System.setProperty("prob.stdlib", probdir + File.separator + "stdlib");
		logger.debug("Java version: {}", System.getProperty("java.version"));
	}

	private void run(final String[] args) throws IOException, ScriptException {
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("maxCacheSize")) {
				logger.debug("setting maximum cache size requested");
				String value = line.getOptionValue("maxCacheSize");
				logger.debug("retrieved maxSize");
				maxCacheSize = Integer.valueOf(value);
				logger.debug("Max size set successfully to {}", value);
			}

			if (line.hasOption("script")) {
				logger.debug("Run Script");
				String value = line.getOptionValue("script");
				shell.runScript(new File(value), false);
			}
		} catch (ParseException e) {
			logger.debug("Failed to parse CLI", e);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar probcli.jar", options);
			System.exit(-1);
		}
	}

	/**
	 * <p>
	 * Returns the path to the ProB home directory,
	 * in which the binary files and libraries for the ProB Prolog core (probcli) are stored.
	 * Note that the files might not actually be installed into this directory until an instance of probcli is started
	 * (by loading a model or by directly injecting an {@link IAnimator} instance).
	 * </p>
	 * <p>
	 * By default, the ProB home directory is located somewhere in the .prob directory in the user's home directory.
	 * Before an instance of probcli is started for the first time,
	 * ProB 2 installs probcli and other related files into the ProB home directory.
	 * </p>
	 * <p>
	 * If the system property {@code prob.home} is set,
	 * the ProB home directory is changed to the value of the property.
	 * In this case, the directory must already contain a valid installation of probcli -
	 * if {@code prob.home} is set, ProB 2 will <em>not</em> install probcli automatically.
	 * </p>
	 *
	 * @return the directory in which the binary files and libraries for ProB are stored
	 */
	public static Path getProBHomePath() {
		final String homePathOverride = System.getProperty("prob.home");
		if (homePathOverride != null) {
			return Paths.get(homePathOverride);
		} else {
			return Installer.DEFAULT_HOME;
		}
	}

	/**
	 * Returns the path of the ProB home directory as a string.
	 * Consider using {@link #getProBHomePath()} instead, which returns a {@link Path} object instead of a string.
	 *
	 * @return the return value of {@link #getProBHomePath()}, converted to a string, with {@link File#separator} appended
	 */
	public static String getProBDirectory() {
		return getProBHomePath() + File.separator;
	}

	public static String getVersion() {
		return buildProperties.getProperty("version");
	}

	public static String getGitSha() {
		return buildProperties.getProperty("git");
	}

	public static int getMaxCacheSize() {
		return maxCacheSize;
	}

	/**
	 * Start the ProB 2.0 shell with argument -s. Run integration tests with
	 * -test /path/to/testDir
	 *
	 * @param args
	 *            command-line arguments
	 */
	public static void main(final String[] args) {
		if (!System.getProperties().containsKey(ContextInitializer.CONFIG_FILE_PROPERTY)) {
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "de/prob/logging/production.xml");
		}
		logger = LoggerFactory.getLogger(Main.class);
		try {
			Main main = getInjector().getInstance(Main.class);
			main.run(args);
		} catch (Exception e) {
			logger.error("Unhandled exception", e);
			System.exit(-1);
		} finally {
			getInjector().getInstance(ProBInstanceProvider.class).shutdownAll();
		}
		System.exit(0);
	}

}
