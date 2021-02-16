package de.prob.check.tracereplay.check;

import com.google.inject.Injector;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exceptions.MappingFactoryInterface;
import de.prob.check.tracereplay.check.exceptions.PrologTermNotDefinedException;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

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
						ProgressMemoryInterface progressMemoryInterface) throws IOException, ModelTranslationError {

		this.newOperationInfos = newInfos;
		this.oldOperationInfos = oldInfos;

		typeFinder = new TypeFinder(transitionList, oldInfos, newInfos, oldVars, newVars);
		typeFinder.check();
		progressMemoryInterface.nextStep();
		progressMemoryInterface.nextStep();


		traceModifier = new TraceModifier(transitionList, TraceCheckerUtils.createStateSpace(newPath, injector), progressMemoryInterface);

		traceModifier.insertAmbiguousChanges(emptyMap());

		TraceExplorer traceExplorer = new TraceExplorer(false, mappingFactory, replayOptions, progressMemoryInterface);


		Set<String> notHandledTypeIAndIIAsTypeIII = new HashSet<>();
		notHandledTypeIAndIIAsTypeIII.addAll(typeFinder.getTypeIIPermutation().entrySet().stream().filter(entry -> entry.getValue().contains(entry.getKey())).map(Map.Entry::getKey).collect(Collectors.toList()));
		notHandledTypeIAndIIAsTypeIII.addAll(typeFinder.getTypeIorII());
		notHandledTypeIAndIIAsTypeIII.addAll(typeFinder.getTypeIII());


		Set<String> notHandledTypeIAndIIAsTypeIV = new HashSet<>();
		notHandledTypeIAndIIAsTypeIV.addAll(typeFinder.getTypeIIPermutation().entrySet().stream().filter(entry -> entry.getValue().contains(entry.getKey())).map(Map.Entry::getKey).collect(Collectors.toList()));
		notHandledTypeIAndIIAsTypeIV.addAll(typeFinder.getTypeIorII());
		notHandledTypeIAndIIAsTypeIV.addAll(typeFinder.getTypeIV());


		traceModifier.makeTypeIII(notHandledTypeIAndIIAsTypeIII, typeFinder.getTypeIV(), newInfos, oldInfos,  traceExplorer);

		this.renamingAnalyzer = new RenamingAnalyzerInterface() {
			@Override
			public Map<String, Map<String, String>> getResultTypeII() {
				return emptyMap();
			}

			@Override
			public Map<String, String> getResultTypeIIInit() {
				return emptyMap();
			}

			@Override
			public Map<String, List<RenamingDelta>> getResultTypeIIWithCandidatesAsDeltaMap() {
				return emptyMap();
			}
		};

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
			throws IOException, ModelTranslationError, PrologTermNotDefinedException {

		this.oldOperationInfos = oldInfos;
		this.newOperationInfos = newInfos;


		typeFinder = new TypeFinder(transitionList, oldInfos, newInfos, oldVars, newVars);
		typeFinder.check();
		progressMemoryInterface.nextStep();

		RenamingAnalyzer renamingAnalyzer = new RenamingAnalyzer(typeFinder.getTypeIorII(), typeFinder.getTypeIIPermutation(),
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

		this.renamingAnalyzer = renamingAnalyzer;

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
