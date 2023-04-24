package de.prob.cli;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.prob.Main;
import de.prob.scripting.FileHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class Installer {
	public static final Path DEFAULT_HOME = Paths.get(System.getProperty("user.home"), ".prob", "prob2-" + Main.getVersion());
	private static final Path LOCK_FILE_PATH = DEFAULT_HOME.resolve("installer.lock");
	private static final Logger logger = LoggerFactory.getLogger(Installer.class);
	private final OsSpecificInfo osInfo;

	@Inject
	private Installer(final OsSpecificInfo osInfo) {
		this.osInfo = osInfo;
	}

	/**
	 * Set or clear the executable bits of the given path.
	 *
	 * @param path the path of the file to make (non-)executable
	 * @param executable whether the file should be executable
	 */
	private static void setExecutable(final Path path, final boolean executable) throws IOException {
		logger.trace("Attempting to set executable status of {} to {}", path, executable);
		try {
			final Set<PosixFilePermission> perms = new HashSet<>(Files.readAttributes(path, PosixFileAttributes.class).permissions());
			final PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
			if (view == null) {
				// If the PosixFileAttributeView is not available, we're probably on Windows, so nothing needs to be done
				logger.info("Could not get POSIX attribute view for {} (this is usually not an error)", path);
				return;
			}
			if (executable) {
				perms.add(PosixFilePermission.OWNER_EXECUTE);
				perms.add(PosixFilePermission.GROUP_EXECUTE);
				perms.add(PosixFilePermission.OTHERS_EXECUTE);
			} else {
				perms.remove(PosixFilePermission.OWNER_EXECUTE);
				perms.remove(PosixFilePermission.GROUP_EXECUTE);
				perms.remove(PosixFilePermission.OTHERS_EXECUTE);
			}
			view.setPermissions(perms);
		} catch (UnsupportedOperationException e) {
			// If POSIX attributes are unsupported, we're probably on Windows, so nothing needs to be done
			logger.info("Could not set executable status of {} (this is usually not an error)", path);
		}
	}

	/**
	 * Install all CLI binaries, if necessary.
	 */
	@SuppressWarnings("try") // don't warn about unused resource in try
	public void ensureCLIsInstalled() {
		if (System.getProperty("prob.home") != null) {
			logger.info("prob.home is set. Not installing CLI from kernel resources.");
			return;
		}

		logger.info("Attempting to install CLI binaries");
		try {
			// Create ProB home directory if necessary.
			Files.createDirectories(DEFAULT_HOME);
		} catch (IOException e) {
			logger.error("Failed to create ProB home directory", e);
			return;
		}
		try (
			final FileChannel lockFileChannel = FileChannel.open(LOCK_FILE_PATH, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			final FileLock lock = lockFileChannel.lock();
		) {
			final String binariesZipResourceName = osInfo.getBinariesZipResourceName();
			logger.trace("Extracting binaries from {}", binariesZipResourceName);
			
			try (final InputStream is = this.getClass().getResourceAsStream(binariesZipResourceName)) {
				if (is == null) {
					throw new IllegalArgumentException("Binaries zip not found in resources (make sure that you did not build ProB 2 with -PprobHome=... set): " + binariesZipResourceName);
				}
				FileHandler.extractZip(is, DEFAULT_HOME);
			}
			
			for (final String path : Arrays.asList(
				this.osInfo.getCliName(),
				this.osInfo.getUserInterruptCmd(),
				this.osInfo.getCspmfName(),
				this.osInfo.getFuzzName()
			)) {
				setExecutable(DEFAULT_HOME.resolve(path), true);
			}
			
			logger.info("CLI binaries successfully installed");
		} catch (IOException e) {
			logger.error("Failed to install CLI binaries", e);
		}
	}
}
