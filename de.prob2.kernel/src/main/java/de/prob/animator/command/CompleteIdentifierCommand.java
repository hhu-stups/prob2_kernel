package de.prob.animator.command;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.util.Collections;
import java.util.List;

public final class CompleteIdentifierCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "get_possible_completions";
	private static final String IGNORE_CASE_ATOM = "lower_case";

	private static final String COMPLETIONS_VAR = "Completions";

	private final String identifier;
	private boolean ignoreCase;
	private KeywordContext keywords;
	private List<String> completions;

	public CompleteIdentifierCommand(final String identifier) {
		super();

		this.identifier = identifier;
		this.ignoreCase = false;
		this.keywords = null;
		this.completions = null;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public boolean isIgnoreCase() {
		return this.ignoreCase;
	}

	public void setIgnoreCase(final boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public boolean hasKeywords() {
		return this.keywords != null;
	}

	public KeywordContext getKeywords() {
		return keywords;
	}

	public void setKeywords(final KeywordContext keywords) {
		this.keywords = keywords;
	}

	public List<String> getCompletions() {
		return Collections.unmodifiableList(this.completions);
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(this.getIdentifier());
		pto.openList();
		if (this.isIgnoreCase()) {
			pto.printAtom(IGNORE_CASE_ATOM);
		}
		if (this.hasKeywords()) {
			pto.printAtom(this.keywords.getAtom());
		}
		pto.closeList();
		pto.printVariable(COMPLETIONS_VAR);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.completions = PrologTerm.atomsToStrings(BindingGenerator.getList(bindings, COMPLETIONS_VAR));
	}

	public enum KeywordContext {

		LATEX("latex"),
		EXPR("expr"),
		ALL("all");

		private final String atom;

		KeywordContext(String atom) {
			this.atom = atom;
		}

		public String getAtom() {
			return this.atom;
		}
	}
}
