package de.prob.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import de.prob.Main;
import de.prob.scripting.FileHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For internal use only.
 * External code should not use this class directly.
 */
public final class Installer {
	private static final Path DEFAULT_HOME = Paths.get(System.getProperty("user.home"), ".prob", "prob2-" + Main.getVersion());

	private static final Path LOCK_FILE_PATH = DEFAULT_HOME.resolve("installer.lock");
	private static final Logger LOGGER = LoggerFactory.getLogger(Installer.class);

	private static Path proBDirectory = null;

	private Installer() {
		throw new AssertionError("Utility class");
	}

	/**
	 * Set or clear the executable bits of the given path.
	 *
	 * @param path       the path of the file to make (non-)executable
	 */
	private static void setExecutable(final Path path) throws IOException {
		LOGGER.trace("Attempting to set executable status of {}", path);
		try {
			final PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
			if (view == null) {
				// If the PosixFileAttributeView is not available, we're probably on Windows, so nothing needs to be done
				LOGGER.debug("Could not get POSIX attribute view for {} (this is usually not an error)", path);
				return;
			}

			final Set<PosixFilePermission> perms = new HashSet<>(view.readAttributes().permissions());
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			perms.add(PosixFilePermission.GROUP_EXECUTE);
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
			view.setPermissions(perms);
		} catch (UnsupportedOperationException e) {
			// If POSIX attributes are unsupported, we're probably on Windows, so nothing needs to be done
			LOGGER.debug("Could not set executable status of {} (this is usually not an error)", path);
		}
	}

	private static void extractBundledProbcli(Path installDirectory, OsSpecificInfo osInfo) throws IOException {
		final String binariesZipResourceName = osInfo.getBinariesZipResourceName();
		LOGGER.trace("Extracting binaries from resource {} to directory {}", binariesZipResourceName, installDirectory);

		try (final InputStream is = Installer.class.getResourceAsStream(binariesZipResourceName)) {
			if (is == null) {
				throw new IllegalArgumentException("Binaries zip not found in resources (make sure that you did not build the ProB Java API with -PprobHome=... set): " + binariesZipResourceName);
			}
			FileHandler.extractZip(is, installDirectory);
		}

		for (final String path : new String[] {
			osInfo.getCliName(),
			osInfo.getUserInterruptCmd(),
			osInfo.getCspmfName(),
			osInfo.getFuzzName(),
		}) {
			setExecutable(installDirectory.resolve(path));
		}
	}

	/**
	 * <p>
	 * Install all CLI binaries to the static ProB directory ({@link #DEFAULT_HOME}).
	 * </p>
	 * <p>
	 * This installation method has known issues when multiple parallel processes use the ProB Java API,
	 * which can be avoided by using {@link #installToTempDirectory(OsSpecificInfo)} instead.
	 * Known issues include:
	 * </p>
	 * <ul>
	 * <li>
	 * On Windows, if one instance of the ProB Java API has a running probcli process,
	 * it's impossible to start another instance of the same version of the ProB Java API
	 * (because the second instance will try to overwrite the currently running probcli.exe file,
	 * which is not allowed on Windows).
	 * </li>
	 * <li>
	 * Running different SNAPSHOT versions of the ProB Java API
	 * (same version number string, but different builds)
	 * in parallel can fail unpredictably,
	 * because they will overwrite each other's files with potentially incompatible versions.
	 * </li>
	 * </ul>
	 * 
	 * @param osInfo determines which OS the installed ProB should be for
	 */
	@SuppressWarnings("try") // javac warns about unused resource (lockFileChannel.lock()) in try-with-resources
	private static void installToStaticDirectory(OsSpecificInfo osInfo) {
		LOGGER.info("Installing CLI binaries to static directory");
		try {
			// Create the static ProB home directory if necessary.
			Files.createDirectories(DEFAULT_HOME);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to create static ProB home directory", e);
		}

		// Ensure that the CLI isn't installed multiple times in parallel.
		// The FileChannel/FileLock guards against multiple different processes installing at once.
		// The lock in ensureInstalled guards against multiple threads inside the same process installing at once.
		// (FileChannel.lock works "on behalf of the entire Java virtual machine" according to the docs
		// and can throw a OverlappingFileLockException when locking a file from two threads in the same process.)
		try (
			final FileChannel lockFileChannel = FileChannel.open(LOCK_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			final FileLock ignored = lockFileChannel.lock()
		) {
			LOGGER.trace("Acquired lock file for installing CLI binaries");
			extractBundledProbcli(DEFAULT_HOME, osInfo);
			LOGGER.info("CLI binaries successfully installed");
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to install ProB CLI binaries", e);
		} finally {
			// Delete the installer lock file once we're done with it.
			// This is only for cleanliness and is *not* required for releasing the lock -
			// that is already done by the try-with-resources block closing/releasing the FileLock.
			try {
				Files.delete(LOCK_FILE_PATH);
			} catch (IOException | RuntimeException e) {
				// It's safe to ignore exceptions here -
				// the lock is already released (see above)
				// and the locking code also works fine if the lock file already exists.
				// If we fail to delete it here,
				// a future run of the ProB Java API will reuse the already existing lock file
				// (and then try to delete it again).
				LOGGER.warn("Failed to delete installer lock file - ignoring", e);
			}
		}
	}
	
	/**
	 * <p>
	 * Install all CLI binaries into a new temporary directory,
	 * which will be deleted automatically when the JVM shuts down.
	 * </p>
	 * <p>
	 * Compared to installing into a static directory ({@link #installToStaticDirectory(OsSpecificInfo)}),
	 * this avoids certain conflicts when multiple parallel processes use the ProB Java API.
	 * </p>
	 * 
	 * @param osInfo determines which OS the installed ProB should be for
	 * @return path of the new temporary installation directory
	 */
	private static Path installToTempDirectory(OsSpecificInfo osInfo) {
		try {
			LOGGER.info("Installing CLI binaries to a new temporary directory");
			Path installDirectory = Files.createTempDirectory("prob-java");
			LOGGER.trace("Created temporary directory for CLI binaries: {}", installDirectory);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				LOGGER.debug("Deleting temporary ProB installation directory: {}", installDirectory);
				try {
					MoreFiles.deleteRecursively(installDirectory, RecursiveDeleteOption.ALLOW_INSECURE);
				} catch (IOException | RuntimeException exc) {
					LOGGER.error("Failed to delete temporary ProB installation directory", exc);
				}
			}, "ProB temp install dir deleter"));

			// No lock file needed here - a fresh temp directory cannot conflict with other processes.
			extractBundledProbcli(installDirectory, osInfo);

			return installDirectory;
		} catch (IOException exc) {
			throw new UncheckedIOException("Failed to install ProB CLI binaries to temporary directory", exc);
		}
	}

	/**
	 * For internal use only! No touching!
	 * This method will soon become non-public again without warning!
	 * You do not need to call this method as a user of the ProB Java API!
	 * 
	 * @param osInfo determines which OS the installed ProB should be for
	 * @return path of the (possibly temporary) ProB installation directory
	 */
	public static Path ensureInstalled(OsSpecificInfo osInfo) {
		synchronized (Installer.class) {
			LOGGER.trace("Acquired process-local lock for installing CLI binaries");
			if (proBDirectory == null) {
				String probHomeTempString = System.getProperty("prob.home.temp");
				// Use the temp directory installation method by default
				// and only install to the static directory if explicitly requested with -Dprob.home.temp=false.
				// The static directory installation has known issues -
				// see the installToStaticDirectory Javadoc.
				if (!"false".equalsIgnoreCase(probHomeTempString)) {
					proBDirectory = installToTempDirectory(osInfo);
				} else {
					installToStaticDirectory(osInfo);
					proBDirectory = DEFAULT_HOME;
				}
			} else {
				LOGGER.trace("CLI binaries have already been installed for this process");
			}

			return proBDirectory;
		}
	}
}
