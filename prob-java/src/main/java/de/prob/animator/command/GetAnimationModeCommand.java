package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Language;

public class GetAnimationModeCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_get_animation_mode";
	private static final String ANIMATION_MODE = "Mode";
	private static final String ANIMATION_MINOR_MODE = "MinorMode";

	private Language language;

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(ANIMATION_MODE);
		pto.printVariable(ANIMATION_MINOR_MODE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		String animationMode = bindings.get(ANIMATION_MODE).getFunctor();
		String animationMinorMode = bindings.get(ANIMATION_MINOR_MODE).getFunctor();

		this.language = Language.fromPrologName(animationMinorMode);
		// try animation_minor_mode first, then animation_mode
		if (this.language == null) {
			this.language = Language.fromPrologName(animationMode);
		}
	}

	public Language getLanguage() {
		return language;
	}

}
