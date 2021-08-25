package de.prob.check.tracereplay.check.refinement;

import com.google.inject.Injector;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.scripting.ClassicalBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class HorizontalTraceRefiner {
	private final Injector injector;
	private final List<PersistentTransition> transitionList;
	private final Path adaptFrom;
	private final Path adaptTo;

	public HorizontalTraceRefiner(Injector injector, List<PersistentTransition> transitionList, Path adaptFrom, Path adaptTo) {
		this.injector = injector;
		this.transitionList = transitionList;
		this.adaptFrom = adaptFrom;
		this.adaptTo = adaptTo;
	}

	/**
	 * Loads an EventB File into a new StateSpace
	 * @return the file loaded into the state space
	 * @throws IOException file reading went wrong
	 */
	private StateSpace loadClassicalBFileAsStateSpace() throws IOException {
		ReusableAnimator animator = injector.getInstance(ReusableAnimator.class);
		StateSpace stateSpace = animator.createStateSpace();
		ClassicalBFactory classicalBFactory = injector.getInstance(ClassicalBFactory.class);
		classicalBFactory.extract(adaptFrom.toString()).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}

	private String removeFileExtension(Path path){
		return path.getFileName().toString().substring(0, path.getFileName().toString().lastIndexOf("."));
	}


	/**
	 * Refines a classical B Trace horizontally. Respects Includes/Extends/Promotes/Imports
	 * @return A trace replayable on the target machine if possible, preserving the originals traces properties
	 * @throws IOException file reading went wrong
	 * @throws BCompoundException construction of predicates for kernel went wrong
	 * @throws TraceConstructionError no possible trace was found
	 */
	public List<PersistentTransition> refineTrace() throws IOException, BCompoundException, TraceConstructionError {
		BParser betaParser = new BParser(adaptTo.toString());
		Start betaStart = betaParser.parseFile(adaptTo.toFile(), false);

		OperationsFinder operationsFinder = new OperationsFinder(removeFileExtension(adaptFrom), betaStart);
		operationsFinder.explore();

		StateSpace stateSpace = TraceCheckerUtils.createStateSpace(adaptTo.toString(), injector);

		StateSpace stateSpace2 = loadClassicalBFileAsStateSpace();
		Map<String, OperationsFinder.RenamingContainer> promotedOperations =
				handlePromotedOperations(operationsFinder.getPromoted(), removeFileExtension(adaptFrom), new ArrayList<>(stateSpace2.getLoadedMachine().getOperations().keySet()), operationsFinder.getExtendedMachines(), operationsFinder.getIncludedImportedMachines());

		Map<String, Set<String>> internal = operationsFinder.usedOperationsReversed();

		Set<String> usedOperations = TraceCheckerUtils.usedOperations(transitionList);

		Map<String, List<String>> alternatives = usedOperations.stream().collect(toMap(entry -> entry, entry -> {
			Set<String> result = new HashSet<>();
			if(promotedOperations.containsKey(entry)){
				result.add(promotedOperations.get(entry).toString());
			}else if(internal.containsKey(entry)){
				result.addAll(internal.get(entry));
			}
			return new ArrayList<>(result);
		}));

		alternatives.remove("INITIALISATION");
		alternatives.put(Transition.INITIALISE_MACHINE_NAME, singletonList(Transition.INITIALISE_MACHINE_NAME));
		alternatives.put(Transition.SETUP_CONSTANTS_NAME, singletonList(Transition.SETUP_CONSTANTS_NAME));

		List<Transition> resultRaw = AdvancedTraceConstructor.constructTraceEventB(transitionList, stateSpace, alternatives, emptyList(), emptyList());

		return PersistentTransition.createFromList(resultRaw);
	}

	/**
	 * Provided with the necessary input this function calculates which operations are exposed via promotes
	 * The problem this method deals with is renaming of machines in the promotes clause and thus resulting name clashes
	 * if the operations are used without prefix
	 * The method will create a mapping from origin operations to the names used for the operations in the target machine
	 * @param promotedOperations extracted promoted operations
	 * @param targetMachine the machine to adapt everything for
	 * @param operationsOfOrigin the operation names from the machine the trace was created on
	 * @param extendedMachines the machines that are declared to be extended
	 * @param includedImportedMachines the machines that are declared to be imported
	 * @return A map. mapping origin names to target names
	 */
	public static Map<String, OperationsFinder.RenamingContainer> handlePromotedOperations(Set<OperationsFinder.RenamingContainer> promotedOperations, String targetMachine, List<String> operationsOfOrigin, List<OperationsFinder.RenamingContainer> extendedMachines, List<OperationsFinder.RenamingContainer> includedImportedMachines){
		if(extendedMachines.stream().anyMatch(entry -> entry.complies(targetMachine)))
		{
			OperationsFinder.RenamingContainer renamingContainer = extendedMachines.stream()
					.filter(entry -> entry.complies(targetMachine))
					.collect(toList())
					.get(0);
			return operationsOfOrigin.stream()
					.collect(toMap(entry -> entry, entry -> new OperationsFinder.RenamingContainer(renamingContainer.prefix, entry)));
		}

		if(includedImportedMachines.stream().anyMatch(entry -> entry.complies(targetMachine))){
			OperationsFinder.RenamingContainer complyingMachines = includedImportedMachines.stream().filter(entry -> entry.complies(targetMachine)).collect(toList()).get(0);

			Set<OperationsFinder.RenamingContainer> promoteCandidates = operationsOfOrigin.stream().map(entry -> new OperationsFinder.RenamingContainer(complyingMachines.prefix, entry)).collect(Collectors.toSet());

			return promotedOperations.stream()
					.filter(promoteCandidates::contains)
					.collect(toMap(entry -> entry.suffix, entry -> entry));
		}

		return emptyMap();

	}

}
