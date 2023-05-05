package de.prob.check.tracereplay.check;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Injector;

import de.prob.animator.ReusableAnimator;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.scripting.FactoryProvider;
import de.prob.scripting.ModelFactory;
import de.prob.statespace.StateSpace;

public class TraceCheckerUtils {
	private static ReusableAnimator reusableAnimator;
	public static ReusableAnimator getReusableAnimator(Injector injector){
		if (reusableAnimator==null){
			reusableAnimator = injector.getInstance(ReusableAnimator.class);
		}
		return reusableAnimator;
	}

	public static StateSpace createStateSpace(String path, Injector injector) throws IOException {
		ReusableAnimator animator = getReusableAnimator(injector);
		ModelFactory<?> factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(path.substring(path.lastIndexOf(".")+1)));
		if(animator.getCurrentStateSpace()!=null)
		{
			animator.getCurrentStateSpace().kill();
		}
		StateSpace stateSpace = animator.createStateSpace();
		factory.extract(path).loadIntoStateSpace(stateSpace);
		return stateSpace;
	}


	/**
	 * Returns the operations actually used by the trace
	 * @param transitionList the trace to analyse
	 * @return a set of operations used in the trace
	 */
	public static Set<String> usedOperations(List<PersistentTransition> transitionList){
		return transitionList.stream().map(PersistentTransition::getOperationName).collect(Collectors.toSet());
	}
}
