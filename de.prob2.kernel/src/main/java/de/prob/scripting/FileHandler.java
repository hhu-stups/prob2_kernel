package de.prob.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Provides helper methods for handling files.
 *
 * @author joy
 */
public final class FileHandler {

	private FileHandler() {
		throw new AssertionError("Utility class");
	}

	public static void extractZip(final InputStream zipFileStream, final Path targetDir) throws IOException {
		Files.createDirectories(targetDir);
		final Path realTargetDir = targetDir.toRealPath();

		try (final ZipInputStream inStream = new ZipInputStream(zipFileStream)) {
			for (ZipEntry entry; (entry = inStream.getNextEntry()) != null; ) {
				final String name = entry.getName();
				if (name.isEmpty() || "/".equals(name)) {
					// extract directory is created above
					continue;
				}

				final Path dest = realTargetDir.resolve(name).toAbsolutePath().normalize();
				if (!dest.startsWith(realTargetDir)) {
					throw new IOException("Entry is outside of target dir: " + name);
				}

				if (entry.isDirectory()) {
					Files.createDirectories(dest);
				} else {
					Path parent = dest.getParent();
					if (parent != null) {
						Files.createDirectories(parent);
					}

					Files.copy(inStream, dest, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}
}
