package de.prob.animator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import de.prob.cli.ProBInstance;
import de.prob.statespace.AnimationSelector;

/**
 * The Guice module responsible for initializing classes having to do with the
 * interaction with the prolog core.
 * 
 * @author joy
 * 
 */
public class AnimatorModule extends AbstractModule {
	@Provides
	static IAnimator provideAnimator(ProBInstance proBInstance, AnimationSelector animations) {
		AnimatorImpl animator = new AnimatorImpl(proBInstance);
		animator.addBusyListener(busy -> animations.notifyAnimatorStatus(animator.getId(), busy));
		return animator;
	}
}
