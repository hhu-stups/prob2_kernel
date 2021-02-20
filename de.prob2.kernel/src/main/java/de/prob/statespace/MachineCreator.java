package de.prob.statespace;

import com.google.inject.Inject;
import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.scripting.ClassicalBFactory;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.scripting.ModelTranslationError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Creates a Loaded Machine
 */
public class MachineCreator {

	private final Injector injector;
	private final ClassicalBFactory classicalBFactory;
	private final AnimationSelector animationSelector;
	private final ReusableAnimator reusableAnimator;

	@Inject
	public MachineCreator(Injector injector, ClassicalBFactory classicalBFactory, AnimationSelector animationSelector, ReusableAnimator reusableAnimator){
		this.injector = injector;
		this.classicalBFactory = classicalBFactory;
		this.animationSelector = animationSelector;
		this.reusableAnimator = reusableAnimator;
	}

	/**
	 *
	 * @param path the path to the machine to be loaded
	 * @return the loaded machine
	 */
	public LoadedMachine load(Path path){
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension("mch"));
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
		return stateSpace.getLoadedMachine();
	}

}
