package de.prob;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;

import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.AbstractCommand;
import de.prob.scripting.ClassicalBFactory;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;

public class ProBKernelStub {
	private final ClassicalBFactory classicalBFactory;
	private final ReusableAnimator reusableAnimator;

	@Inject
	public ProBKernelStub(ClassicalBFactory classicalBFactory, ReusableAnimator reusableAnimator){
		this.classicalBFactory = classicalBFactory;
		this.reusableAnimator = reusableAnimator;
	}

	public StateSpace createStateSpace(Path path) throws IOException, ModelTranslationError {
		killCurrentStateSpace();
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		classicalBFactory.extract(path.toString()).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}
	
	public void executeCommand(AbstractCommand command){
		reusableAnimator.execute(command);
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
