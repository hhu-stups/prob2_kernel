package de.prob.check;

public final class ModelCheckLimitReached implements IModelCheckingResult {
	private final String message;

	public ModelCheckLimitReached(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
