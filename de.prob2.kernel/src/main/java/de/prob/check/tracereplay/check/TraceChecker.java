package de.prob.check.tracereplay.check;

import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.ReplayOptions;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TraceChecker {

	private final TypeFinder typeFinder;
	private final DeltaFinder deltaFinder;
	private final TraceModifier traceModifier;
	private final Map<String, OperationInfo> oldOperationInfos;
	private final Map<String, OperationInfo> newOperationInfos;
	private final PersistentTrace trace;
	private final Map<String, List<Delta>> typeIICandidates;

	public TraceChecker(PersistentTrace trace, Map<String, OperationInfo> oldInfos, Map<String, OperationInfo> newInfos,
						Set<String> oldVars, Set<String> newVars, String oldPath, String newPath, Injector injector, StateSpace stateSpace)
			throws IOException, ModelTranslationError {

		this.trace = trace;
		this.oldOperationInfos = oldInfos;
		this.newOperationInfos = newInfos;



		traceModifier = new TraceModifier(trace, TraceCheckerUtils.createStateSpace(newPath, injector));


		PersistentTrace traceWithoutInit = new PersistentTrace(trace.getDescription(),
				trace.getTransitionList().stream()
						.filter(element -> !element.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME))
						.collect(Collectors.toList()));
		typeFinder = new TypeFinder(traceWithoutInit, oldInfos, newInfos, oldVars, newVars);
		typeFinder.check();


		ReusableAnimator animator = injector.getInstance(ReusableAnimator.class);

		deltaFinder = new DeltaFinder(typeFinder.getTypeIorII(), typeFinder.getTypeIIPermutation(), typeFinder.getInitIsTypeIorIICandidate(), animator, oldPath,
				newPath, injector);

		deltaFinder.calculateDelta();

		List<Delta> deltasTypeIorII = deltaFinder.getResultTypeIorII().entrySet().stream()
				.map(entry -> new Delta(entry.getValue(), oldInfos.get(entry.getKey()))).collect(Collectors.toList());

		if(!deltaFinder.getResultTypeIIInit().isEmpty())
		{
			Delta init = new Delta(Transition.INITIALISE_MACHINE_NAME, Transition.INITIALISE_MACHINE_NAME,
					Collections.emptyMap(), Collections.emptyMap(), deltaFinder.getResultTypeIIInit());
			deltasTypeIorII.add(init);
		}



		typeIICandidates = deltaFinder.getResultTypeIIWithCandidates().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry ->
				{
					Map<String, Map<String, String>> candidate = entry.getValue();
					return candidate.values().stream()
							.map(stringStringMap -> new Delta(stringStringMap, oldInfos.get(entry.getKey()))).collect(Collectors.toList());
				}));


		traceModifier.insertMultipleUnambiguousChanges(deltasTypeIorII);

		traceModifier.insertAmbiguousChanges(typeIICandidates);


		traceModifier.makeTypeIII(typeFinder.getTypeIII(), newInfos);




	}


	public PersistentTrace getTrace(){
		return trace;
	}

	public TypeFinder getTypeFinder() {
		return typeFinder;
	}

	public DeltaFinder getDeltaFinder() {
		return deltaFinder;
	}

	public TraceModifier getTraceModifier() {
		return traceModifier;
	}

	public Map<String, OperationInfo> getOldOperationInfos(){
		return oldOperationInfos;
	}

	public Map<String, OperationInfo> getNewOperationInfos(){
		return newOperationInfos;
	}


	public Map<String, List<Delta>> getTypeIICandidates() {
		return typeIICandidates;
	}


}
