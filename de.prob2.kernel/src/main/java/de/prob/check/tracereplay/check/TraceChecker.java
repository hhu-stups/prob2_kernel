package de.prob.check.tracereplay.check;

import com.google.inject.Injector;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exceptions.DeltaCalculationException;
import de.prob.check.tracereplay.check.exceptions.MappingFactoryInterface;
import de.prob.check.tracereplay.check.exceptions.PrologTermNotDefinedException;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;

import java.io.IOException;
import java.util.*;


public class TraceChecker {

	private final TypeFinder typeFinder;
	private final TraceModifier traceModifier;
	private final Map<String, OperationInfo> oldOperationInfos;
	private final Map<String, OperationInfo> newOperationInfos;
	private final RenamingAnalyzerInterface renamingAnalyzer;


	public TraceChecker(List<PersistentTransition> transitionList,
						Map<String, OperationInfo> oldInfos,
						Map<String, OperationInfo> newInfos,
						Set<String> newVars,
						Set<String> oldVars,
						String newPath,
						Injector injector,
						MappingFactoryInterface mappingFactory,
						ReplayOptions replayOptions,
						ProgressMemoryInterface progressMemoryInterface) throws IOException, ModelTranslationError, DeltaCalculationException {

		this.newOperationInfos = newInfos;
		this.oldOperationInfos = oldInfos;

		typeFinder = new TypeFinder(transitionList, oldInfos, newInfos, oldVars, newVars);
		typeFinder.check();
		progressMemoryInterface.nextStep();


		renamingAnalyzer = new StaticRenamingAnalyzer(typeFinder.getTypeIorII(), typeFinder.getTypeIIPermutation(), oldInfos, newInfos, mappingFactory);
		renamingAnalyzer.calculateDelta();
		progressMemoryInterface.nextStep();

		traceModifier = new TraceModifier(transitionList, TraceCheckerUtils.createStateSpace(newPath, injector), progressMemoryInterface);

		traceModifier.insertMultipleUnambiguousChanges(renamingAnalyzer.getResultTypeIIAsDeltaList());
		traceModifier.insertAmbiguousChanges(renamingAnalyzer.getResultTypeIIWithCandidatesAsDeltaMap());

		TraceExplorer traceExplorer = new TraceExplorer(false, mappingFactory, replayOptions, progressMemoryInterface);


		traceModifier.makeTypeIII(typeFinder.getTypeIII(), typeFinder.getTypeIV(), newInfos, oldInfos,  traceExplorer);

	}


	public TraceChecker(List<PersistentTransition> transitionList,
						Map<String, OperationInfo> oldInfos,
						Map<String, OperationInfo> newInfos,
						Set<String> oldVars,
						Set<String> newVars,
						String oldPath,
						String newPath,
						Injector injector,
						MappingFactoryInterface mappingFactory,
						ReplayOptions replayOptions,
						ProgressMemoryInterface progressMemoryInterface)
			throws IOException, ModelTranslationError, DeltaCalculationException {

		this.oldOperationInfos = oldInfos;
		this.newOperationInfos = newInfos;


		typeFinder = new TypeFinder(transitionList, oldInfos, newInfos, oldVars, newVars);
		typeFinder.check();
		progressMemoryInterface.nextStep();

		renamingAnalyzer = new RenamingAnalyzer(typeFinder.getTypeIorII(), typeFinder.getTypeIIPermutation(),
				typeFinder.getInitIsTypeIorIICandidate(), oldPath, newPath, injector, oldInfos);
		renamingAnalyzer.calculateDelta();
		progressMemoryInterface.nextStep();


		traceModifier = new TraceModifier(transitionList, TraceCheckerUtils.createStateSpace(newPath, injector), progressMemoryInterface);

		List<RenamingDelta> deltasTypeII = renamingAnalyzer.getResultTypeIIAsDeltaList();


		traceModifier.insertMultipleUnambiguousChanges(deltasTypeII);


		Map<String, List<RenamingDelta>> deltasTypeIIWithCandidates = renamingAnalyzer.getResultTypeIIWithCandidatesAsDeltaMap();
		traceModifier.insertAmbiguousChanges(deltasTypeIIWithCandidates);


		boolean initSet = !renamingAnalyzer.getResultTypeIIInit().isEmpty();
		TraceExplorer traceExplorer  = new TraceExplorer(initSet, mappingFactory, replayOptions, progressMemoryInterface);


		traceModifier.makeTypeIII(typeFinder.getTypeIII(), typeFinder.getTypeIV(), newInfos, oldInfos, traceExplorer);


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

	public RenamingAnalyzerInterface getRenamingAnalyzer(){
		return renamingAnalyzer;
	}


}
