package de.prob.check.tracereplay.check;

import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Transition;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TraceChecker {

	private final TypeFinder typeFinder;
	private final DeltaFinder deltaFinder;
	private final TraceModifier traceModifier;
	private final Map<String, OperationInfo> oldOperationInfos;
	private final PersistentTrace trace;

	public TraceChecker(PersistentTrace trace, Map<String, OperationInfo> oldInfos, Map<String, OperationInfo> newInfos,
						Set<String> oldVars,
						Set<String> newVars,
						String oldPath, String newPath, Injector injector) throws IOException, ModelTranslationError {

		this.trace = trace;
		this.oldOperationInfos = oldInfos;



		traceModifier = new TraceModifier(trace);


		PersistentTrace traceWithoutInit = new PersistentTrace(trace.getDescription(),
				trace.getTransitionList().stream()
						.filter(element -> !element.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME))
						.collect(Collectors.toList()));
		typeFinder = new TypeFinder(traceWithoutInit, oldInfos, newInfos);
		typeFinder.check();

		VariableTypeFinder variableTypeFinder = new VariableTypeFinder(trace, oldVars, newVars);

		ReusableAnimator animator = injector.getInstance(ReusableAnimator.class);

		deltaFinder = new DeltaFinder(typeFinder.getTypeIorII(), typeFinder.getTypeIIpermutation(), animator, oldPath,
				newPath, injector);

		deltaFinder.calculateDelta();

		Map<String, Delta> deltasTypeIorII = deltaFinder.getResultTypeIorII().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> new Delta(entry.getValue(), oldInfos.get(entry.getKey()))));

		if(!deltaFinder.getResultTypeIIInit().isEmpty())
		{
			Delta init = new Delta(Transition.INITIALISE_MACHINE_NAME, Collections.emptyMap(), Collections.emptyMap(), deltaFinder.getResultTypeIIInit());
			deltasTypeIorII.put(Transition.INITIALISE_MACHINE_NAME, init);
		}



		Map<String, List<Delta>> deltasTypeII = deltaFinder.getResultTypeIIWithCandidates().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry ->
				{
					Map<String, Map<String, String>> candidate = entry.getValue();
					return candidate.values().stream()
							.map(stringStringMap -> new Delta(stringStringMap, oldInfos.get(entry.getKey()))).collect(Collectors.toList());
				}));

		traceModifier.insertMultipleUnambiguousChanges(deltasTypeIorII);

		traceModifier.insertMultipleAmbiguousChanges(deltasTypeII);


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


}
