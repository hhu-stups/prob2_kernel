package de.prob.check.tracereplay.check;

import com.google.inject.Injector;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exceptions.MappingFactoryInterface;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.emptyMap;

public class TraceChecker {

	private final TypeFinder typeFinder;
	private final TraceModifier traceModifier;
	private final Map<String, OperationInfo> oldOperationInfos;
	private final Map<String, OperationInfo> newOperationInfos;
	private final IDeltaFinder deltaFinder;


	public TraceChecker(List<PersistentTransition> transitionList,
						Map<String, OperationInfo> oldInfos,
						Map<String, OperationInfo> newInfos,
						Set<String> newVars,
						Set<String> oldVars,
						Set<String> newSets,
						Set<String> newConst,
						String newPath,
						Injector injector,
						MappingFactoryInterface mappingFactory) throws IOException, ModelTranslationError {

		this.newOperationInfos = newInfos;
		this.oldOperationInfos = oldInfos;

		typeFinder = new TypeFinder(transitionList, oldInfos, newInfos, oldVars, newVars);
		typeFinder.check();


		traceModifier = new TraceModifier(transitionList, TraceCheckerUtils.createStateSpace(newPath, injector));

		TraceExplorer traceExplorer = new TraceExplorer(false, mappingFactory);

		traceModifier.makeTypeIII(typeFinder.getTypeIII(), newInfos, oldOperationInfos, newVars, newSets, newConst, traceExplorer);

		this.deltaFinder = new IDeltaFinder() {
			@Override
			public Map<String, Map<String, String>> getResultTypeII() {
				return emptyMap();
			}

			@Override
			public Map<String, String> getResultTypeIIInit() {
				return emptyMap();
			}

			@Override
			public Map<String, List<Delta>> getResultTypeIIWithCandidatesAsDeltaMap() {
				return emptyMap();
			}
		};

	}


	public TraceChecker(List<PersistentTransition> transitionList,
						Map<String, OperationInfo> oldInfos,
						Map<String, OperationInfo> newInfos,
						Set<String> oldVars,
						Set<String> newVars,
						Set<String> newSets,
						Set<String> newConst,
						String oldPath,
						String newPath,
						Injector injector,
						MappingFactoryInterface mappingFactory)
			throws IOException, ModelTranslationError {

		this.oldOperationInfos = oldInfos;
		this.newOperationInfos = newInfos;


		typeFinder = new TypeFinder(transitionList, oldInfos, newInfos, oldVars, newVars);
		typeFinder.check();


		DeltaFinder deltaFinder = new DeltaFinder(typeFinder.getTypeIorII(), typeFinder.getTypeIIPermutation(),
				typeFinder.getInitIsTypeIorIICandidate(), oldPath, newPath, injector, oldInfos);
		deltaFinder.calculateDelta();


		traceModifier = new TraceModifier(transitionList, TraceCheckerUtils.createStateSpace(newPath, injector));

		List<Delta> deltasTypeII = deltaFinder.getResultTypeIIAsDeltaList();

		traceModifier.insertMultipleUnambiguousChanges(deltasTypeII);


		Map<String, List<Delta>> deltasTypeIIWithCandidates = deltaFinder.getResultTypeIIWithCandidatesAsDeltaMap();
		traceModifier.insertAmbiguousChanges(deltasTypeIIWithCandidates);

		TraceExplorer traceExplorer;
		if(!deltaFinder.getResultTypeIIInit().isEmpty())
		{
			traceExplorer = new TraceExplorer(true, mappingFactory);
		}else{
			traceExplorer = new TraceExplorer(false, mappingFactory);
		}


		traceModifier.makeTypeIII(typeFinder.getTypeIII(), newInfos, oldInfos, newVars, newSets, newConst, traceExplorer);

		this.deltaFinder = deltaFinder;

	}

	public TypeFinder getTypeFinder() {
		return typeFinder;
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

	public IDeltaFinder getDeltaFinder(){
		return deltaFinder;
	}


}
