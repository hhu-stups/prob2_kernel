package de.prob.check.tracereplay;

import de.hhu.stups.prob.translator.BValue;
import de.hhu.stups.prob.translator.Translator;
import de.hhu.stups.prob.translator.exceptions.TranslationException;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ComputationNotCompletedResult;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.exception.ProBError;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TraceReplay {

	private static final Logger LOGGER = LoggerFactory.getLogger(TraceReplay.class);

	public enum TraceReplayError {
		COMMAND, NO_OPERATION_POSSIBLE, TRACE_REPLAY, MISMATCH_OUTPUT
	}

	public enum PostconditionResult {
		SUCCESS, FAIL, PARSE_ERROR
	}

	public static Trace replayTrace(PersistentTrace persistentTrace, StateSpace stateSpace) {
		return replayTrace(persistentTrace, stateSpace, true, new HashMap<>(), new DefaultTraceChecker());
	}

	private static List<PostconditionResult> checkPostconditions(State state, List<Postcondition> postconditions) {
		List<PostconditionResult> result = new ArrayList<>();
		for(Postcondition postcondition : postconditions) {
			try {
				switch (postcondition.getKind()) {
					case PREDICATE: {
						// TODO: Evaluation of a formula always bounds a variable when there should be a scope error
						AbstractEvalResult evalResult = state.eval(((PostconditionPredicate) postcondition).getPredicate(), FormulaExpand.EXPAND);
						if(evalResult instanceof ComputationNotCompletedResult) {
							result.add(PostconditionResult.PARSE_ERROR);
						} else {
							PostconditionResult postconditionResult = "TRUE".equals(evalResult.toString()) ? PostconditionResult.SUCCESS : PostconditionResult.FAIL;
							result.add(postconditionResult);
						}
						break;
					}
					case ENABLEDNESS: {
						String predicate = ((OperationEnabledness) postcondition).getPredicate();
						predicate = predicate.isEmpty() ? "1=1" : predicate;
						Transition transition = state.findTransition(((OperationEnabledness) postcondition).getOperation(), predicate);
						PostconditionResult postconditionResult = transition != null ? PostconditionResult.SUCCESS : PostconditionResult.FAIL;
						result.add(postconditionResult);
						break;
					}
					case DISABLEDNESS: {
						String predicate = ((OperationDisabledness) postcondition).getPredicate();
						predicate = predicate.isEmpty() ? "1=1" : predicate;
						Transition transition = state.findTransition(((OperationDisabledness) postcondition).getOperation(), predicate);
						PostconditionResult postconditionResult = transition == null ? PostconditionResult.SUCCESS : PostconditionResult.FAIL;
						result.add(postconditionResult);
						break;
					}
					default:
						throw new RuntimeException("Postcondition class is unknown: " + postcondition.getKind());
				}
			} catch (EvaluationException | ProBError e) {
				result.add(PostconditionResult.PARSE_ERROR);
			}
		}
		return result;
	}

	/**
	 * Iterate through a transition list and tries to replay every transition contained
	 * @param persistentTrace the trace to replay
	 * @param stateSpace the current stateSpace - will be used to execute commands and replay trace
	 * @param setCurrentAnimation if true the replayed trace will be set as the current animation
	 * @param replayInformation a reference to an map in which additional results will be stored
	 * @param traceChecker an interface implementation for processing results
	 * @return A Trace when the replay was successful else return null
	 */
	public static Trace replayTrace(PersistentTrace persistentTrace, StateSpace stateSpace, final boolean setCurrentAnimation,
									 Map<String, Object> replayInformation, ITraceChecker traceChecker) {
		Trace trace = new Trace(stateSpace);
		trace.setExploreStateByDefault(false);
		boolean success = true;
		final List<PersistentTransition> transitionList = persistentTrace.getTransitionList();
		final List<List<PostconditionResult>> postcondtionsResults = new ArrayList<>();

		boolean replaySuccess = true;
		for (int i = 0; i < transitionList.size(); i++) {
			traceChecker.updateProgress((double) i / transitionList.size(), replayInformation);
			PersistentTransition persistentTransition = transitionList.get(i);
			if(!replaySuccess) {
				postcondtionsResults.add(persistentTransition.getPostconditions().stream()
						.map(post -> PostconditionResult.FAIL).collect(Collectors.toList()));
			} else {
				Transition trans = replayPersistentTransition(trace, persistentTransition, setCurrentAnimation, replayInformation, traceChecker);
				if (trans != null) {
					trace = trace.add(trans);
					List<PostconditionResult> postconditionResult = checkPostconditions(trace.getCurrentState(), persistentTransition.getPostconditions());
					postcondtionsResults.add(postconditionResult);
					success = success && postconditionResult.stream().map(r -> r == PostconditionResult.SUCCESS).reduce(true, (a, e) -> a && e);
				} else {
					success = false;
					replaySuccess = false;
					postcondtionsResults.add(persistentTransition.getPostconditions().stream()
							.map(post -> PostconditionResult.FAIL).collect(Collectors.toList()));
				}
			}

			if (Thread.currentThread().isInterrupted()) {
				traceChecker.afterInterrupt();
				return trace;
			}

		}
		traceChecker.showTestError(persistentTrace, postcondtionsResults);
		traceChecker.setResult(success, postcondtionsResults, replayInformation);
		trace.setExploreStateByDefault(true);
		trace.getCurrentState().explore();

		return trace;
	}

	private static Transition replayPersistentTransition(Trace t, PersistentTransition persistentTransition,
														 boolean setCurrentAnimation, Map<String, Object> replayInformation,
														 ITraceChecker traceChecker) {
		StateSpace stateSpace = t.getStateSpace();
		PredicateBuilder predicateBuilder = new PredicateBuilder();
		if (persistentTransition.getParameters() != null) {
			predicateBuilder.addMap(persistentTransition.getParameters());
		}
		if (persistentTransition.getDestinationStateVariables() != null) {
			predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());
		}
		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);
		final GetOperationByPredicateCommand command = new GetOperationByPredicateCommand(stateSpace,
				t.getCurrentState().getId(), persistentTransition.getOperationName(), pred, 1);
		stateSpace.execute(command);
		replayInformation.put("persistentTransition", persistentTransition);
		replayInformation.put("predicateBuilder", predicateBuilder);
		replayInformation.put("command", command);
		if (command.hasErrors()) {
			traceChecker.showError(TraceReplayError.COMMAND, replayInformation);
			return null;
		}
		List<Transition> possibleTransitions = command.getNewTransitions();
		if (possibleTransitions.isEmpty()) {
			traceChecker.showError(TraceReplayError.NO_OPERATION_POSSIBLE, replayInformation);
			return null;
		}
		Transition trans = possibleTransitions.get(0);
		if (!checkOutputParams(trans, persistentTransition, setCurrentAnimation, replayInformation, traceChecker)) {
			return null;
		}
		return trans;
	}

	private static boolean checkOutputParams(Transition trans, PersistentTransition persistentTransition, boolean setCurrentAnimation,
											 Map<String,Object> replayInformation, ITraceChecker traceChecker) {
		String operationName = trans.getName();
		OperationInfo machineOperationInfo = trans.getStateSpace().getLoadedMachine().getMachineOperationInfo(operationName);
		final Map<String, String> ouputParameters = persistentTransition.getOutputParameters();
		if (machineOperationInfo != null && ouputParameters != null) {
			List<String> outputParameterNames = machineOperationInfo.getOutputParameterNames();
			try {
				List<BValue> translatedReturnValues = trans.getTranslatedReturnValues();
				for (int i = 0; i < outputParameterNames.size(); i++) {
					String outputParamName = outputParameterNames.get(i);
					BValue paramValueFromTransition = translatedReturnValues.get(i);
					if (ouputParameters.containsKey(outputParamName)) {
						String stringValue = ouputParameters.get(outputParamName);
						BValue bValue = Translator.translate(stringValue);
						if (!bValue.equals(paramValueFromTransition)) {
							// do we need further checks here?
							// because the value translator does not
							// support enum values properly
							if (setCurrentAnimation) {
								replayInformation.put("operationName", operationName);
								replayInformation.put("outputParamName", outputParamName);
								replayInformation.put("bValue", bValue.toString());
								replayInformation.put("paramValue", paramValueFromTransition.toString());
								traceChecker.showError(TraceReplayError.MISMATCH_OUTPUT, replayInformation);
							}
							return false;
						}
					}

				}
			} catch (TranslationException e) {
				replayInformation.put("error", e);
				traceChecker.showError(TraceReplayError.TRACE_REPLAY, replayInformation);
				LOGGER.error("", e);
				return false;
			}
		}
		return true;
	}

}
