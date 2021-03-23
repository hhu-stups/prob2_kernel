package de.prob.check.tracereplay.check;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Injector;

import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.GetEnabledOperationsCommand;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.formula.PredicateBuilder;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public class RefinementChecker {

	private final Injector injector;
	private final StateSpace abstractStateSpace;
	private final StateSpace refinementStateSpace;
	private final List<PersistentTransition> transitionList;

	public RefinementChecker(Path abstractMachine, Path refinementMachine, List<PersistentTransition> transitionList, Injector injector) throws IOException {
		this.injector = injector;
		this.refinementStateSpace = createStateSpaceWithPath(refinementMachine);
		this.abstractStateSpace = createStateSpaceWithPath(abstractMachine);
		this.transitionList = transitionList;
	}

	public void replay() throws IOException {




	}

	public void executeParallel(String opName, PersistentTransition transition){
		Trace t = new Trace(abstractStateSpace);

		Trace t2 = new Trace(refinementStateSpace);

		PredicateBuilder predicateBuilder = new PredicateBuilder();
		predicateBuilder.addMap(transition.getDestinationStateVariables());
		predicateBuilder.addMap(transition.getOutputParameters());
		predicateBuilder.addMap(transition.getParameters());

		final IEvalElement pred = abstractStateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

		GetOperationByPredicateCommand getOperationByPredicateCommand =
				new GetOperationByPredicateCommand(t.getStateSpace(), t.getCurrentState().getId(), t.getCurrentState().getId(), pred,1 );

		t.getStateSpace().execute(getOperationByPredicateCommand);

		Transition goal = getOperationByPredicateCommand.getNewTransitions().get(0);


		GetEnabledOperationsCommand getEnabledOperationsCommand = new GetEnabledOperationsCommand(t2.getStateSpace(), t2.getCurrentState().getId());
		t2.getStateSpace().execute(getEnabledOperationsCommand);

		List<Transition> enabledOperation = getEnabledOperationsCommand.getEnabledOperations()
				.stream()
				.filter(operation -> operation
						.getName()
						.equals(transition.getOperationName()))
				.collect(Collectors.toList());

		List<Trace> traces = enabledOperation.stream().map(t2::add).collect(Collectors.toList());
/*
		traces.stream().map(trace -> {
			PredicateBuilder predicateBuilder = replayOptions.createMapping(persistentTransition);

			final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);

			EvaluateFormulaCommand evaluateFormulaCommand = new EvaluateFormulaCommand()
		})
		*/

	}

	public StateSpace createStateSpaceWithPath(Path path) throws IOException {
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension("mch"));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(path.toString()).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}

}
