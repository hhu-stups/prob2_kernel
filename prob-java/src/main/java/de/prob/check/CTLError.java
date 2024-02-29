package de.prob.check;

import de.prob.animator.domainobjects.CTL;
import de.prob.animator.domainobjects.ErrorItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CTLError implements IModelCheckingResult {

	private final CTL formula;
	private final List<ErrorItem> errors;

	public CTLError(final CTL formula, final List<ErrorItem> errors) {
		super();
		this.formula = formula;
		this.errors = errors;
	}

	public CTLError(final CTL formula, final String reason) {
		this(formula, Collections.singletonList(ErrorItem.fromErrorMessage(reason)));
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
