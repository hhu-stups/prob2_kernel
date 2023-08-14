package de.prob.scripting;

import java.io.File;
import java.io.FileInputStream;
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
import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.translate.EventBDatabaseTranslator;
import de.prob.model.eventb.translate.EventBFileNotFoundException;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;

public class EventBFactory implements ModelFactory<EventBModel> {
	private final Provider<EventBModel> modelCreator;
	public static final String RODIN_MACHINE_EXTENSION = "bum";
	public static final String RODIN_CONTEXT_EXTENSION = "buc";
	public static final String CHECKED_RODIN_MACHINE_EXTENSION = "bcm";
	public static final String CHECKED_RODIN_CONTEXT_EXTENSION = "bcc";

	@Inject
	public EventBFactory(final Provider<EventBModel> modelCreator) {
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<EventBModel> extract(String modelPath) throws IOException {
		if (EventBPackageFactory.EXTENSION.equals(MoreFiles.getFileExtension(Paths.get(modelPath)))) {
			throw new IllegalArgumentException("This is an EventB package file, it must be loaded using EventBPackageFactory instead of EventBFactory.\nPath: " + modelPath);
		}
		File file = new File(modelPath);
		if(!file.exists()) {
			throw new EventBFileNotFoundException(file.getAbsolutePath(), "", false, null);
		}
		final EventBModel model = modelCreator.get();
		final String validFileName = getValidFileName(modelPath);
		final EventBDatabaseTranslator translator = new EventBDatabaseTranslator(model, validFileName);
		return new ExtractedModel<>(translator.getModel(), translator.getMainComponent());
	}

	private String getValidFileName(String fileName) {
		String extension = MoreFiles.getFileExtension(Paths.get(fileName));
		if (CHECKED_RODIN_CONTEXT_EXTENSION.equals(extension) || CHECKED_RODIN_MACHINE_EXTENSION.equals(extension)) {
			return fileName;
		} else if (RODIN_CONTEXT_EXTENSION.equals(extension)) {
			return fileName.substring(0, fileName.length() - RODIN_CONTEXT_EXTENSION.length()) + CHECKED_RODIN_CONTEXT_EXTENSION;
		} else if (RODIN_MACHINE_EXTENSION.equals(extension)) {
			return fileName.substring(0, fileName.length() - RODIN_MACHINE_EXTENSION.length()) + CHECKED_RODIN_MACHINE_EXTENSION;
		} else {
			throw new IllegalArgumentException(fileName + " is not a valid Event-B file");
		}
	}

	public EventBModel extractModelFromZip(final String zipfile) throws IOException {
		final File tempdir = createTempDir();
		try (final InputStream is = new FileInputStream(zipfile)) {
			FileHandler.extractZip(is, tempdir.toPath());
		}

		final List<File> modelFiles = new ArrayList<>();
		Files.walkFileTree(tempdir.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
				String extension = MoreFiles.getFileExtension(file);
				if (CHECKED_RODIN_CONTEXT_EXTENSION.equals(extension) || CHECKED_RODIN_MACHINE_EXTENSION.equals(extension)) {
					modelFiles.add(file.toFile());
				}
				return FileVisitResult.CONTINUE;
			}
		});
		if (modelFiles.isEmpty()) {
			ResourceGroovyMethods.deleteDir(tempdir);
			throw new IllegalArgumentException("No static checked Event-B files were found in that zip archive!");
		}
		EventBModel model = modelCreator.get();
		for (File f : modelFiles) {
			String name = MoreFiles.getNameWithoutExtension(f.toPath());
			if (model.getComponent(name) == null) {
				EventBDatabaseTranslator translator = new EventBDatabaseTranslator(model, f.getAbsolutePath());
				model = translator.getModel();
			}
		}
		return model;
	}

	private File createTempDir() throws IOException {
		final File tempdir = Files.createTempDirectory("eventb-model").toFile();
		// the temporary directory will be deleted on shutdown of the JVM
		Runtime.getRuntime().addShutdownHook(new Thread("EventB TempDir Deleter") {
			@Override
			public void run() {
				ResourceGroovyMethods.deleteDir(tempdir);
			}
		});
		return tempdir;
	}
}
