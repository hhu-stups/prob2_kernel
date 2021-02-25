package de.prob;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;

import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.AbstractCommand;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.scripting.ClassicalBFactory;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

public class ProBKernelStub {
	private final ClassicalBFactory classicalBFactory;
	private final ReusableAnimator reusableAnimator;
	private Trace trace;

	@Inject
	public ProBKernelStub(ClassicalBFactory classicalBFactory, ReusableAnimator reusableAnimator){
		this.classicalBFactory = classicalBFactory;
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
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		classicalBFactory.extract(path.toString()).loadIntoStateSpace(stateSpace);
		trace = new Trace(stateSpace);
		return stateSpace.getLoadedMachine();
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

	public PersistentTrace getATrace(){

		return new PersistentTrace(trace);
	}
}
