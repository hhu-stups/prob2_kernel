package de.prob.model.representation;

import java.util.concurrent.atomic.AtomicInteger;

public final class FormulaUUID implements IFormulaUUID {

	private static final AtomicInteger COUNT = new AtomicInteger();

	private final String uuid;

	public FormulaUUID() {
		this.uuid = "formula_" + COUNT.incrementAndGet();
	}

	@Override
	public String getUUID() {
		return this.uuid;
	}
}
