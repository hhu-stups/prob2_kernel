package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import com.google.inject.Inject;

import de.prob.animator.ReusableAnimator;
import de.prob.scripting.Api;
import de.prob.scripting.ClassicalBFactory;

/**
 * Creates a Loaded Machine
 * 
 * @deprecated Use {@link Api#b_load(String)} or {@link ClassicalBFactory#extract(String)} instead.
 */
@Deprecated
public class MachineCreator {
	private final ClassicalBFactory classicalBFactory;
	private final AnimationSelector animationSelector;
	private final ReusableAnimator reusableAnimator;

	@Inject
	public MachineCreator(ClassicalBFactory classicalBFactory, AnimationSelector animationSelector, ReusableAnimator reusableAnimator){
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
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		Function<StateSpace, Trace> function = stateSpace1 -> {
			try {
				classicalBFactory.extract(path.toString()).loadIntoStateSpace(stateSpace);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new Trace(stateSpace);
		};
		animationSelector.changeCurrentAnimation(function.apply(stateSpace));
		return stateSpace.getLoadedMachine();
	}

}
