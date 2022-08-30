package de.prob;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;

import de.prob.animator.ReusableAnimator;
import de.prob.scripting.ClassicalBFactory;
import de.prob.scripting.EventBFactory;
import de.prob.statespace.StateSpace;

public class ProBEventBKernelStub {
	private final EventBFactory eventBFactory;
	private final ReusableAnimator reusableAnimator;


	@Inject
	public ProBEventBKernelStub(EventBFactory eventBFactory, ReusableAnimator reusableAnimator){
		this.eventBFactory = eventBFactory;
		this.reusableAnimator = reusableAnimator;
	}

	public StateSpace createStateSpace(Path path) throws IOException {
		killCurrentStateSpace();
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		eventBFactory.extract(path.toString()).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}

	public void killCurrentStateSpace(){
		if(reusableAnimator.getCurrentStateSpace()!=null)
		{
			reusableAnimator.getCurrentStateSpace().kill();

		}
	}

	public void killCurrentAnimator(){
		reusableAnimator.kill();
	}
}
