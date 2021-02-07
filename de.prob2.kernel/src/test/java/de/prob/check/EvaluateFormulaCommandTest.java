package de.prob.check;

import com.google.inject.*;
import com.google.inject.Module;
import com.google.inject.spi.TypeConverterBinding;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.EvaluateFormulaCommand;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.formula.PredicateBuilder;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvaluateFormulaCommandTest {

	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;


	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}

	}

	@Test
	public void test_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("/home", "sebastian", "Desktop", "TrafficLightRef.ref"));
		PredicateBuilder predicateBuilder = new PredicateBuilder();
	//	predicateBuilder.add("cars_go=FALSE");
	//	predicateBuilder.add("peds_go=FALSE");
	//	predicateBuilder.add("peds_color=red");
	//	predicateBuilder.add("cars_color={RED}");
	//		predicateBuilder.add("(peds_go = TRUE <=> peds_color = green)");
	//		predicateBuilder.add("(cars_go = TRUE <=> green : cars_color)");
	//	predicateBuilder.add("red : cars_color");

		predicateBuilder.add("LET cars_go, peds_go BE cars_go=FALSE & peds_go=FALSE IN (peds_go = TRUE <=> peds_color = green) & (cars_go = TRUE <=> green : cars_color) END");

		Trace t = new Trace(stateSpace);


		PredicateBuilder predicateBuilder1 = new PredicateBuilder();
		predicateBuilder1.add("1=1");
		final IEvalElement pred1 = stateSpace.getModel().parseFormula(predicateBuilder1.toString(), FormulaExpand.EXPAND);
		GetOperationByPredicateCommand getOperationByPredicateCommand = new GetOperationByPredicateCommand(t.getStateSpace(), t.getCurrentState().getId(), Transition.INITIALISE_MACHINE_NAME, pred1,1);

		t.getStateSpace().execute(getOperationByPredicateCommand);

		t = t.add(getOperationByPredicateCommand.getNewTransitions().get(0));

		System.out.println(predicateBuilder.toString());
		final IEvalElement pred = stateSpace.getModel().parseFormula(predicateBuilder.toString(), FormulaExpand.EXPAND);
		System.out.println(pred);

		EvaluateFormulaCommand evaluateFormulaCommand = new EvaluateFormulaCommand(pred, t.getCurrentState().getId());

		stateSpace.execute(evaluateFormulaCommand);

		System.out.println(evaluateFormulaCommand.getValue());

	}
}
