package de.prob.check.tracereplay.check.refinement;

import com.google.inject.Injector;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.traceConstruction.AdvancedTraceConstructor;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.scripting.ClassicalBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class VerticalTraceRefiner extends AbstractTraceRefinement{


	private final Path adaptTo; //Machine the trace has to be adapted to

	public VerticalTraceRefiner(Injector injector, List<PersistentTransition> transitionList, Path adaptFrom, Path adaptTo) {
		super(injector, transitionList, adaptFrom);
		this.adaptTo = adaptTo;
	}



	/**
	 * Checks an EventB machine if it is able to perform the given Trace. For the algorithm see prolog code.
	 * For that both machines are merged and run and the original trace is executed.
	 * @return The transformed trace
	 * @throws IOException file reading went wrong
	 * @throws BCompoundException predicate translation went wrong
	 * @throws TraceConstructionError trace could not be found
	 */
	@Override
	public TraceRefinementResult refineTraceExtendedFeedback() throws IOException, BCompoundException, TraceConstructionError {
		BParser alphaParser = new BParser(adaptFrom.toString());
		Start alphaStart = alphaParser.parseFile(adaptFrom.toFile(), false);

		BParser betaParser = new BParser(adaptTo.toString());
		Start betaStart = betaParser.parseFile(adaptTo.toFile(), false);

		NodeCollector nodeCollector = new NodeCollector(alphaStart);
		ASTManipulator astManipulator = new ASTManipulator(betaStart, nodeCollector);

		AAbstractMachineParseUnit aAbstractMachineParseUnit = (AAbstractMachineParseUnit) astManipulator.getStart().getPParseUnit();

		PrettyPrinter prettyPrinter = new PrettyPrinter();
		prettyPrinter.caseAAbstractMachineParseUnit(aAbstractMachineParseUnit);

		File tempFile = File.createTempFile("machine", "." + ClassicalBFactory.CLASSICAL_B_MACHINE_EXTENSION, adaptFrom.getParent().toFile());


		FileWriter writer = new FileWriter(tempFile);
		writer.write(prettyPrinter.getPrettyPrint());
		writer.close();


		StateSpace stateSpace = TraceCheckerUtils.createStateSpace(tempFile.toPath().toString(), injector);

		List<Transition> resultRaw = AdvancedTraceConstructor.constructTrace(transitionList, stateSpace);

		return new TraceRefinementResult(true, resultRaw);
	}
}
