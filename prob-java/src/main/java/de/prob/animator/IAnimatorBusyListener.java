package de.prob.animator;

@FunctionalInterface
public interface IAnimatorBusyListener {
	void animatorStatus(boolean busy);
}
