package de.prob;

import com.google.inject.Inject;
import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.AbstractCommand;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.scripting.ClassicalBFactory;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.AnimationSelector;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class ProBKernelStub {

	private final Injector injector;
	private final ClassicalBFactory classicalBFactory;
	private final AnimationSelector animationSelector;
	private final ReusableAnimator reusableAnimator;
	private Trace trace;

	@Inject
	public ProBKernelStub(Injector injector, ClassicalBFactory classicalBFactory, AnimationSelector animationSelector, ReusableAnimator reusableAnimator){
		this.injector = injector;
		this.classicalBFactory = classicalBFactory;
		this.animationSelector = animationSelector;
		this.reusableAnimator = reusableAnimator;
	}

	/**
	 * Just load something
	 * @param path machine to load
	 * @return the loaded machine
	 * @throws IOException exceptions thrown from prob
	 * @throws ModelTranslationError exceptions thrown from prob
	 */
	public LoadedMachine load(Path path) throws IOException, ModelTranslationError {

		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension("mch"));
		ClassicalBModel bla = (ClassicalBModel) factory.extract(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch").toString()).getModel();
			StateSpace stateSpace = reusableAnimator.createStateSpace();
		Function<StateSpace, Trace> function = stateSpace1 -> {
			try {
				factory.extract(path.toString()).loadIntoStateSpace(stateSpace);
			} catch (IOException | ModelTranslationError e) {
				e.printStackTrace();

			}
			return new Trace(stateSpace);
		};
		animationSelector.changeCurrentAnimation(function.apply(stateSpace));
		trace = new Trace(stateSpace);
		return stateSpace.getLoadedMachine();
	}


	public StateSpace createStateSpace(Path path) throws IOException, ModelTranslationError {
		killCurrent();
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension("mch"));
		ClassicalBModel bla = (ClassicalBModel) factory.extract(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch").toString()).getModel();
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(path.toString()).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}
	
	public void executeCommand(AbstractCommand command){
		reusableAnimator.execute(command);
	}

	public void killCurrent(){
		if(reusableAnimator.getCurrentStateSpace()!=null)
		{
			reusableAnimator.getCurrentStateSpace().kill();
		}
	}

	public PersistentTrace getATrace(){

		return new PersistentTrace(trace);
	}
}
