package de.prob.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.translate.EventBDatabaseTranslator;
import de.prob.model.eventb.translate.EventBFileNotFoundException;
import de.prob.statespace.StateSpace;

public class EventBFactory implements ModelFactory<EventBModel> {
	public static final String RODIN_MACHINE_EXTENSION = "bum";
	public static final String RODIN_CONTEXT_EXTENSION = "buc";
	public static final String CHECKED_RODIN_MACHINE_EXTENSION = "bcm";
	public static final String CHECKED_RODIN_CONTEXT_EXTENSION = "bcc";

	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<EventBModel> modelCreator;

	@Inject
	EventBFactory(Provider<StateSpace> stateSpaceProvider, Provider<EventBModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<EventBModel> extract(String modelPath) throws IOException {
		Path file = Paths.get(modelPath);
		if (EventBPackageFactory.EXTENSION.equals(MoreFiles.getFileExtension(file))) {
			throw new IllegalArgumentException("This is an EventB package file, it must be loaded using EventBPackageFactory instead of EventBFactory.\nPath: " + modelPath);
		} else if (!Files.exists(file)) {
			throw new EventBFileNotFoundException(file.toAbsolutePath().toString(), "", false, null);
		}

		final EventBModel model = modelCreator.get();
		final String validFileName = getValidFileName(modelPath);
		final EventBDatabaseTranslator translator = new EventBDatabaseTranslator(model, validFileName);
		return new ExtractedModel<>(stateSpaceProvider, translator.getModel(), translator.getMainComponent());
	}

	private String getValidFileName(String fileName) {
		String extension = MoreFiles.getFileExtension(Paths.get(fileName));
		switch (extension) {
			case CHECKED_RODIN_CONTEXT_EXTENSION:
			case CHECKED_RODIN_MACHINE_EXTENSION:
				return fileName;
			case RODIN_CONTEXT_EXTENSION:
				return fileName.substring(0, fileName.length() - RODIN_CONTEXT_EXTENSION.length()) + CHECKED_RODIN_CONTEXT_EXTENSION;
			case RODIN_MACHINE_EXTENSION:
				return fileName.substring(0, fileName.length() - RODIN_MACHINE_EXTENSION.length()) + CHECKED_RODIN_MACHINE_EXTENSION;
			default:
				throw new IllegalArgumentException(fileName + " is not a valid Event-B file");
		}
	}

	public EventBModel extractModelFromZip(final String zipfile) throws IOException {
		final Path tempdir = createTempDir();
		try (final InputStream is = Files.newInputStream(Paths.get(zipfile))) {
			FileHandler.extractZip(is, tempdir);
		}

		final List<Path> modelFiles = new ArrayList<>();
		Files.walkFileTree(tempdir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				String extension = MoreFiles.getFileExtension(file);
				if (CHECKED_RODIN_CONTEXT_EXTENSION.equals(extension) || CHECKED_RODIN_MACHINE_EXTENSION.equals(extension)) {
					modelFiles.add(file.toRealPath());
				}
				return FileVisitResult.CONTINUE;
			}
		});
		if (modelFiles.isEmpty()) {
			Throwable suppressed = null;
			try {
				MoreFiles.deleteRecursively(tempdir, RecursiveDeleteOption.ALLOW_INSECURE);
			} catch (Exception e) {
				suppressed = e;
			}

			IllegalArgumentException e = new IllegalArgumentException("No static checked Event-B files were found in that zip archive!");
			if (suppressed != null) {
				e.addSuppressed(suppressed);
			}
			throw e;
		}

		EventBModel model = modelCreator.get();
		for (Path p : modelFiles) {
			String name = MoreFiles.getNameWithoutExtension(p);
			if (model.getComponent(name) == null) {
				EventBDatabaseTranslator translator = new EventBDatabaseTranslator(model, p.toString());
				model = translator.getModel();
			}
		}
		return model;
	}

	private Path createTempDir() throws IOException {
		final Path tempdir = Files.createTempDirectory("eventb-model");
		// the temporary directory will be deleted on shutdown of the JVM
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				MoreFiles.deleteRecursively(tempdir, RecursiveDeleteOption.ALLOW_INSECURE);
			} catch (Exception ignored) {
				// silently ignore any errors in this cleanup thread
			}
		}, "EventB TempDir Deleter"));
		return tempdir;
	}
}
