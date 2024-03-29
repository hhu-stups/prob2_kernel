package de.prob.cli;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.prob.Main;
import de.prob.scripting.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Singleton
public final class Installer {

	public static final Path DEFAULT_HOME = Paths.get(System.getProperty("user.home"), ".prob", "prob2-" + Main.getVersion());
	private static final Path LOCK_FILE_PATH = DEFAULT_HOME.resolve("installer.lock");
	private static final Logger LOGGER = LoggerFactory.getLogger(Installer.class);

	private final OsSpecificInfo osInfo;

	@Inject
	private Installer(final OsSpecificInfo osInfo) {
		this.osInfo = osInfo;
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
	 * Install all CLI binaries, if necessary.
	 */
	@SuppressWarnings("try") // javac warns about unused resource (lockFileChannel.lock()) in try-with-resources
	public void ensureCLIsInstalled() {
		if (System.getProperty("prob.home") != null) {
			LOGGER.info("prob.home is set. Not installing ProB CLI binaries from ProB Java API resources.");
			return;
		}

		LOGGER.info("Attempting to install CLI binaries");
		try {
			// Create ProB home directory if necessary.
			Files.createDirectories(DEFAULT_HOME);
		} catch (IOException e) {
			LOGGER.error("Failed to create ProB home directory", e);
			return;
		}

		// Ensure that the CLI isn't installed multiple times in parallel.
		// The FileChannel/FileLock guards against multiple different processes installing at once.
		// The installLock guards against multiple threads inside the same process installing at once.
		// (FileChannel.lock works "on behalf of the entire Java virtual machine" according to the docs
		// and can throw a OverlappingFileLockException when locking a file from two threads in the same process.)
		synchronized (Installer.class) {
			LOGGER.trace("Acquired process-local lock for installing CLI binaries");
			try (
				final FileChannel lockFileChannel = FileChannel.open(LOCK_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
				final FileLock ignored = lockFileChannel.lock()
			) {
				LOGGER.trace("Acquired lock file for installing CLI binaries");
				extractBundledProbcli(DEFAULT_HOME, this.osInfo);
				LOGGER.info("CLI binaries successfully installed");
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to install ProB CLI binaries", e);
			} finally {
				try {
					// try to delete the lock file when this VM exits
					LOCK_FILE_PATH.toFile().deleteOnExit();
				} catch (Exception ignored) {
					// silently ignore errors
					// it is not a problem if the file persists
				}
			}
		}
	}
}
