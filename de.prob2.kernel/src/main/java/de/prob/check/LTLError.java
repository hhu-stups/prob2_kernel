package de.prob.check;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.ErrorItem;
import de.prob.animator.domainobjects.LTL;

public class LTLError implements IModelCheckingResult {

	private final LTL formula;
	private final List<ErrorItem> errors;

	public LTLError(final LTL formula, final List<ErrorItem> errors) {
		super();
		this.formula = formula;
		this.errors = errors;
	}

	public LTLError(final LTL formula, final String reason) {
		this(formula, Collections.singletonList(
			new ErrorItem(reason, ErrorItem.Type.ERROR, Collections.emptyList())
		));
	}

	public List<ErrorItem> getErrors() {
		return Collections.unmodifiableList(this.errors);
	}

	@Override
	public String getMessage() {
		return this.getErrors().stream()
			.map(ErrorItem::toString)
			.collect(Collectors.joining("\n"));
	}

	public String getCode() {
		return formula.getCode();
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
