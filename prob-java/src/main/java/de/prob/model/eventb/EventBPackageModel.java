package de.prob.model.eventb;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

import com.google.common.io.MoreFiles;
import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadEventBFileCommand;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.DependencyGraph;
import de.prob.model.representation.Named;
import de.prob.scripting.EventBPackageFactory;
import de.prob.scripting.StateSpaceProvider;

public final class EventBPackageModel extends EventBModel {
	private static final class DummyMainComponent extends AbstractElement implements Named {
		private String name;
		
		private DummyMainComponent(final String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		@Override
		public String getName() {
			return name;
		}
	}
	
	private final String loadCommandPrologCode;
	
	@Inject
	public EventBPackageModel(final StateSpaceProvider stateSpaceProvider) {
		this(stateSpaceProvider, null, null, null);
	}
	
	private EventBPackageModel(StateSpaceProvider stateSpaceProvider, File modelFile, String mainComponentName, String loadCommandPrologCode) {
		super(
			stateSpaceProvider,
			Collections.emptyMap(),
			mainComponentName == null ? new DependencyGraph() : new DependencyGraph().addVertex(mainComponentName),
			modelFile,
			mainComponentName == null ? null : new DummyMainComponent(mainComponentName),
			modelFile == null ? Collections.emptyList() : Collections.singletonList(modelFile.toPath()),
			Collections.emptySet()
		);
		
		this.loadCommandPrologCode = loadCommandPrologCode;
	}
	
	public String getLoadCommandPrologCode() {
		return this.loadCommandPrologCode;
	}
	
	public EventBPackageModel create(Path modelFile, String loadCommandPrologCode) {
		String mainComponentName;
		if (EventBPackageFactory.EXTENSION.equals(MoreFiles.getFileExtension(modelFile))) {
			mainComponentName = MoreFiles.getNameWithoutExtension(modelFile);
		} else {
			mainComponentName = modelFile.getFileName().toString();
		}
		return new EventBPackageModel(this.stateSpaceProvider, modelFile.toFile(), mainComponentName, loadCommandPrologCode);
	}
	
	@Override
	public AbstractCommand getLoadCommand() {
		if (this.getLoadCommandPrologCode() == null) {
			throw new IllegalStateException("loadCommandPrologCode must be set before loading an EventBPackageModel");
		}

		return new LoadEventBFileCommand(this.getLoadCommandPrologCode());
	}
}
