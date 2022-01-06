package de.prob.check.tracereplay.check.refinement;

import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.model.eventb.EventBModel;
import de.prob.scripting.EventBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;

public class TraceRefinerEventB extends AbstractTraceRefinement {


	private final StateSpace stateSpace;

	public TraceRefinerEventB(Injector injector, List<PersistentTransition> transitionList, Path adaptFrom) throws IOException {
		super(injector, transitionList, adaptFrom);
		this.stateSpace = loadEventBFileAsStateSpace();
	}


	/**
	 * Checks an EventB machine if it is able to perform the given Trace. For the algorithm see prolog code.
	 * The idea of this method is to already do some static precalculation before letting ProB solve the problem.
	 * The precalculations are the theoretical possible alternatives for each situation. For example:
	 * Let T be a Trace. Let p_e and p_g be two operation such that T = p_e, p_g, p_e, p_e....
	 * Let p_ee and p_eee refine p_e and p_g refine p_g then this method will prepare
	 * T to be [[p_ee, p_eee][p_g]....
	 * Further the method will prepare the difference between explicit refined and extended events and skip events.
	 * @return The adapted Trace
	 * @throws IOException File reading went wrong
	 * @throws TraceConstructionError no suitable adaptation was found
	 */
	public List<PersistentTransition> refineTrace() throws IOException, TraceConstructionError {

		EventBModel model = (EventBModel) stateSpace.getModel();

		Map<String, List<String>> alternatives = model.pairNameChanges().entrySet().stream()
				.collect(groupingBy(Map.Entry::getValue))
				.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
						.map(Map.Entry::getKey)
						.collect(toList())));


		alternatives.remove("INITIALISATION");
		alternatives.put(Transition.INITIALISE_MACHINE_NAME, singletonList(Transition.INITIALISE_MACHINE_NAME));
		alternatives.put(Transition.SETUP_CONSTANTS_NAME, singletonList(Transition.SETUP_CONSTANTS_NAME));

		List<Transition> resultRaw = AdvancedTraceConstructor.constructTraceEventB(transitionList, stateSpace, alternatives, model.extendEvents(), model.introducedBySkip());


		return PersistentTransition.createFromList(resultRaw);
	}


	/**
	 * Loads an EventB File into a new StateSpace
	 * @return the file loaded into the state space
	 * @throws IOException file reading went wrong
	 */
	private StateSpace loadEventBFileAsStateSpace() throws IOException {
		ReusableAnimator animator = injector.getInstance(ReusableAnimator.class);
		StateSpace stateSpace = animator.createStateSpace();
		EventBFactory eventBFactory = injector.getInstance(EventBFactory.class);
		eventBFactory.extract(adaptFrom.toString()).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}


}
