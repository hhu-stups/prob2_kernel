package de.prob.animator;

import java.util.List;

import de.prob.animator.domainobjects.ErrorItem;

@FunctionalInterface
public interface IWarningListener {
	void warningsOccurred(final List<ErrorItem> warnings);
}
