package de.prob.model.classicalb;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.google.inject.Inject;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadBProjectCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.DependencyGraph;
import de.prob.model.representation.Machine;
import de.prob.model.representation.ModelElementList;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;

public class ClassicalBModel extends AbstractModel {
	private final ClassicalBMachine mainMachine;
	private final BParser bparser;
	private final RecursiveMachineLoader rml;

	@Inject
	public ClassicalBModel(final StateSpaceProvider ssProvider) {
		this(ssProvider, PersistentHashMap.emptyMap(), new DependencyGraph(), null, null, null, null);
	}

	public ClassicalBModel(
		final StateSpaceProvider ssProvider,
		final PersistentHashMap<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children,
		final DependencyGraph graph,
		final File modelFile,
		final BParser bparser,
		final RecursiveMachineLoader rml,
		final ClassicalBMachine mainMachine
	) {
		super(ssProvider, children, graph, modelFile);
		this.bparser = bparser;
		this.rml = rml;
		this.mainMachine = mainMachine;
	}

	public ClassicalBModel create(final Start mainAST, final RecursiveMachineLoader rml, final File modelFile, final BParser bparser) {
		DependencyGraph graph = new DependencyGraph();

		final DomBuilder d = new DomBuilder(rml.getMainMachineName(), null);
		final ClassicalBMachine classicalBMachine = d.build(mainAST);

		ModelElementList<ClassicalBMachine> machines = new ModelElementList<>();
		machines = machines.addElement(classicalBMachine);
		graph = graph.addVertex(classicalBMachine.getName());

		final Set<String> vertices = new HashSet<>();
		vertices.add(rml.getMainMachineName());
		final Set<String> done = new HashSet<>();
		boolean fpReached = false;

		while (!fpReached) {
			fpReached = true;
			final Set<String> newVertices = new HashSet<>();
			for (final String machineId : vertices) {
				final int lastDot = machineId.lastIndexOf('.');
				final String machineName;
				if (lastDot == -1) {
					machineName = machineId;
				} else {
					machineName = machineId.substring(lastDot + 1);
				}

				if (!done.contains(machineId)) {
					final DependencyWalker walker = new DependencyWalker(machineId, machines, graph, rml.getParsedMachines());
					walker.addReferences(rml.getMachineReferenceInfo().get(machineName));
					graph = walker.getGraph();
					machines = walker.getMachines();
					newVertices.addAll(walker.getMachineIds());
					done.add(machineId);
					fpReached = false;
				}
			}

			vertices.addAll(newVertices);
		}

		return new ClassicalBModel(getStateSpaceProvider(), assoc(Machine.class, machines), graph, modelFile, bparser, rml, classicalBMachine);
	}

	public ClassicalBMachine getMainMachine() {
		return mainMachine;
	}

	public List<Path> getLoadedMachineFiles() {
		return this.rml.getMachineFilesLoaded().stream()
			.map(File::toPath)
			.collect(Collectors.toList());
	}

	public Map<String, Path> getMachineFilesByName() {
		final Map<String, Path> machineFilePaths = new TreeMap<>();
		this.rml.getParsedFiles().forEach((name, file) -> machineFilePaths.put(name, file.toPath()));
		return machineFilePaths;
	}

	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		try {
			return new ClassicalB(bparser.parseFormula(formula), expand, formula);
		} catch (BCompoundException e) {
			final Throwable cause = e.getCause();
			if (cause != null && cause.getCause() instanceof BParseException) {
				throw new EvaluationException(((BParseException)e.getCause().getCause()).getRealMsg(), e);
			} else {
				throw new EvaluationException(e.getFirstException().getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public IEvalElement formulaFromIdentifier(final List<String> identifier, final FormulaExpand expansion) {
		return ClassicalB.fromIdentifier(identifier, expansion);
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.B;
	}

	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadBProjectCommand(rml, getModelFile());
	}

	@Override
	public AbstractElement getComponent(final String name) {
		return getChildrenOfType(Machine.class).getElement(name);
	}

	@Override
	public Object getProperty(final String name) {
		final AbstractElement component = getComponent(name);
		if (component != null) {
			return component;
		} else {
			return super.getProperty(name);
		}
	}

	public AbstractElement getAt(final String name) {
		return getComponent(name);
	}

	public IDefinitions getDefinitions() {
		return bparser.getDefinitions();
	}
}
