package de.prob.scripting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.translate.EventBDatabaseTranslator;
import de.prob.model.eventb.translate.EventBFileNotFoundException;
import de.prob.statespace.StateSpace;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;

public class EventBFactory implements ModelFactory<EventBModel> {
	private final Provider<EventBModel> modelCreator;
	private final EventBPackageFactory eventBPackageFactory;

	@Inject
	public EventBFactory(final Provider<EventBModel> modelCreator, final EventBPackageFactory eventBPackageFactory) {
		this.modelCreator = modelCreator;
		this.eventBPackageFactory = eventBPackageFactory;
	}

	@Override
	public ExtractedModel<EventBModel> extract(String modelPath) throws IOException {
		if (modelPath.endsWith(".eventb")) {
			throw new IllegalArgumentException("This is an EventB package file, it must be loaded using EventBPackageFactory instead of EventBFactory.\nPath: " + modelPath);
		}
		File file = new File(modelPath);
		if(!file.exists()) {
			throw new EventBFileNotFoundException(file.getAbsolutePath(), "", false);
		}
		final EventBModel model = modelCreator.get();
		final String validFileName = getValidFileName(modelPath);
		final EventBDatabaseTranslator translator = new EventBDatabaseTranslator(model, validFileName);
		return new ExtractedModel<>(translator.getModel(), translator.getMainComponent());
	}

	private String getValidFileName(String fileName) {
		if (fileName.endsWith(".buc")) {
			fileName = fileName.replaceAll("\\.buc$", ".bcc");
		}
		if (fileName.endsWith(".bum")) {
			fileName = fileName.replaceAll("\\.bum$", ".bcm");
		}
		if (!(fileName.endsWith(".bcc") || fileName.endsWith(".bcm"))) {
			throw new IllegalArgumentException(fileName + " is not a valid Event-B file");
		}
		return fileName;
	}

	/**
	 * @deprecated Use {@link EventBPackageFactory} instead: {@code factory.extract(fileName).load(prefs)}
	 */
	@Deprecated
	public StateSpace loadModelFromEventBFile(final String fileName, final Map<String, String> prefs) throws IOException {
		return this.eventBPackageFactory.extract(fileName).load(prefs);
	}

	public EventBModel extractModelFromZip(final String zipfile) throws IOException {
		final File tempdir = createTempDir();
		try (final InputStream is = new FileInputStream(zipfile)) {
			FileHandler.extractZip(is, tempdir.toPath());
		}

		final Pattern pattern = Pattern.compile(".*.bcc$|.*.bcm$");
		final List<File> modelFiles = new ArrayList<>();
		Files.walkFileTree(tempdir.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
				if (pattern.matcher(file.getFileName().toString()).matches()) {
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
			String modelPath = f.getAbsolutePath();
			String name = modelPath.substring(modelPath.lastIndexOf(File.separatorChar) + 1, modelPath.lastIndexOf("."));
			if (model.getComponent(name) == null) {
				EventBDatabaseTranslator translator = new EventBDatabaseTranslator(model, modelPath);
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
