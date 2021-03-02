package de.prob.check;

import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.check.RenamingDelta;
import de.prob.check.tracereplay.check.StaticRenamingAnalyzer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

public class StaticRenamingAnalyzerTest {

	@Test
	public void test1() throws IOException, ModelTranslationError {

		Path pathOld = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND2.mch");
		String pathAsStringOld = pathOld.toAbsolutePath().toString();


		Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "typeIV", "always_intermediate", "ISLAND.prob2trace");
		TraceJsonFile traceJsonFile = CliTestCommon.getInjector().getInstance(TraceManager.class).load(path);


		Injector injector = CliTestCommon.getInjector();
		ReusableAnimator reusableAnimator = injector.getInstance(ReusableAnimator.class);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(pathAsStringOld.substring(pathAsStringOld.lastIndexOf(".") + 1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(pathAsStringOld).loadIntoStateSpace(stateSpace);

		Set<String> typeIorIICandidates = new HashSet<>();
		typeIorIICandidates.add("off");
		Map<String, Set<String>> typeIICandidates = new HashMap<>();
		typeIICandidates.put("off", singleton("off"));

		StaticRenamingAnalyzer renamingAnalyzer = new StaticRenamingAnalyzer(typeIorIICandidates, typeIICandidates,
				traceJsonFile.getMachineOperationInfos(), stateSpace.getLoadedMachine().getOperations(), new TestUtils.StubFactoryImplementation());


		renamingAnalyzer.calculateDelta();

		List<RenamingDelta> result1 = renamingAnalyzer.getResultTypeIIAsDeltaList();
		Map<String, List<RenamingDelta>> result2 = renamingAnalyzer.getResultTypeIIWithCandidates();

		Assertions.assertEquals(0, result1.size());
		Assertions.assertEquals(0, result2.size());


	}
}
