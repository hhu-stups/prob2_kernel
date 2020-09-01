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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

import de.prob.animator.IAnimator;
import de.prob.cli.Installer;

/**
 * Provides static information about ProB 2's version, the location of the ProB home directory, etc.
 * This is <i>not</i> the actual main class!
 * ProB 2's command-line interface is now in a separate de.prob2.commandline subproject.
 * The old name of this class is kept to avoid breaking existing code.
 */
public class Main {
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

	private Main() {
		super();
	}

	/**
	 * @deprecated ProB 2's global injector should not be used anymore. Create your own injector instead, using {@code Guice.createInjector(new MainModule())}.
	 */
	@Deprecated
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
}
