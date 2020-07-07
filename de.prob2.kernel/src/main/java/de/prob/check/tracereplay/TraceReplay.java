package de.prob.check.tracereplay;

import de.hhu.stups.prob.translator.BValue;
import de.hhu.stups.prob.translator.Translator;
import de.hhu.stups.prob.translator.exceptions.TranslationException;

import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.formula.PredicateBuilder;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.util.List;
import java.util.Map;

public class TraceReplay {

    public static Trace replayTrace(PersistentTrace persistentTrace, StateSpace stateSpace, final boolean setCurrentAnimation, Map<String, Object> replayInformation,
                                    ITraceChecker traceChecker) {
        Trace trace = new Trace(stateSpace);
        trace.setExploreStateByDefault(false);
        boolean success = true;
        final List<PersistentTransition> transitionList = persistentTrace.getTransitionList();
        for (int i = 0; i < transitionList.size(); i++) {
            final int finalI = i;
            traceChecker.updateProgress((double) finalI / transitionList.size(), replayInformation);
            Transition trans = replayPersistentTransition(trace, transitionList.get(i), setCurrentAnimation, replayInformation, traceChecker);
            if (trans != null) {
                trace = trace.add(trans);
            } else {
                success = false;
                break;
            }

            if (Thread.currentThread().isInterrupted()) {
                traceChecker.interrupt();
                return trace;
            }

        }
        traceChecker.setResult(success, replayInformation);
        trace.setExploreStateByDefault(true);
        trace.getCurrentState().explore();

        return trace;
    }


    private static Transition replayPersistentTransition(Trace t, PersistentTransition persistentTransition, boolean setCurrentAnimation, Map<String, Object> replayInformation, ITraceChecker traceChecker) {
        StateSpace stateSpace = t.getStateSpace();
        PredicateBuilder predicateBuilder = new PredicateBuilder().addMap(persistentTransition.getParameters());
        predicateBuilder.addMap(persistentTransition.getDestinationStateVariables());
        final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);
        final GetOperationByPredicateCommand command = new GetOperationByPredicateCommand(stateSpace,
                t.getCurrentState().getId(), persistentTransition.getOperationName(), pred, 1);
        stateSpace.execute(command);
        replayInformation.put("persistentTransition", persistentTransition);
        replayInformation.put("predicateBuilder", predicateBuilder);
        replayInformation.put("command", command);
        if (command.hasErrors()) {
            traceChecker.showCommandErrors(replayInformation);
            return null;
        }
        List<Transition> possibleTransitions = command.getNewTransitions();
        if (possibleTransitions.isEmpty()) {
            traceChecker.showNoOperationPossible(replayInformation);
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
        final Map<String, String> ouputParameters = persistentTransition.getOuputParameters();
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
                                traceChecker.showMismatchOutputParameters(replayInformation);
                            }
                            return false;
                        }
                    }

                }
            } catch (TranslationException e) {
                traceChecker.showTraceReplayError(e);
                return false;
            }
        }
        return true;
    }

}
