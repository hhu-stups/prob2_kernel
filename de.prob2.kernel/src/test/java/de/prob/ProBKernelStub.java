package de.prob;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;

import com.google.inject.Injector;
import de.prob.animator.ReusableAnimator;
import de.prob.model.eventb.Event;
import de.prob.scripting.ClassicalBFactory;
import de.prob.scripting.EventBFactory;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.statespace.StateSpace;

public class ProBKernelStub {
	private final ClassicalBFactory classicalBFactory;
	private final Injector injector;
	private final ReusableAnimator reusableAnimator;
	private final EventBFactory eventBFactory;

	@Inject
	public ProBKernelStub(ClassicalBFactory classicalBFactory, EventBFactory eventBFactory,  Injector injector, ReusableAnimator reusableAnimator){
		this.classicalBFactory = classicalBFactory;
		this.reusableAnimator = reusableAnimator;
		this.injector = injector;
		this.eventBFactory  = eventBFactory;
	}

	public StateSpace createStateSpace(Path path) throws IOException {
		killCurrentStateSpace();
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		classicalBFactory.extract(path.toString()).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}

	public StateSpace createStateSpace(String path) throws IOException {
		killCurrentStateSpace();
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(path.substring(path.lastIndexOf(".")+1)));
		StateSpace stateSpace = reusableAnimator.createStateSpace();
		factory.extract(path).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}

	public StateSpace createEventB(Path path) throws IOException {
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
