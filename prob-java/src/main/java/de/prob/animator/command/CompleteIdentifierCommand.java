package de.prob.animator.command;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CompleteIdentifierCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "get_possible_completions";
	private static final String LATEX_TO_UNICODE = "latex_to_unicode";
	private static final String ASCII_TO_UNICODE = "ascii_to_unicode";
	private static final String IGNORE_CASE_ATOM = "lower_case";
	private static final String LATEX_ONLY_ATOM = "latex_only";
	private static final String KEYWORDS_ATOM = "keywords";

	private static final String COMPLETIONS_VAR = "Completions";

	private final String identifier;
	private boolean ignoreCase;
	private boolean latexToUnicode;
	private boolean asciiToUnicode;
	private boolean latexOnly;
	private final List<KeywordContext> keywords;
	private List<String> completions;

	public CompleteIdentifierCommand(final String identifier) {
		super();

		this.identifier = identifier;
		this.ignoreCase = false;
		this.keywords = new ArrayList<>();
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

	public boolean isLatexToUnicode() {
		return latexToUnicode;
	}

	public void setLatexToUnicode(final boolean latexToUnicode) {
		this.latexToUnicode = latexToUnicode;
	}

	public boolean isAsciiToUnicode() {
		return asciiToUnicode;
	}

	public void setAsciiToUnicode(final boolean asciiToUnicode) {
		this.asciiToUnicode = asciiToUnicode;
	}

	public boolean isLatexOnly() {
		return latexOnly;
	}

	public void setLatexOnly(boolean latexOnly) {
		if (latexOnly && !this.keywords.isEmpty()) {
			throw new IllegalStateException("latex only and keyword contexts are exclusive");
		}
		this.latexOnly = latexOnly;
	}

	public List<KeywordContext> getKeywordContexts() {
		return Collections.unmodifiableList(this.keywords);
	}

	public void addKeywordContext(final KeywordContext keyword) {
		if (this.latexOnly) {
			throw new IllegalStateException("latex only and keyword contexts are exclusive");
		}
		this.keywords.add(keyword);
	}

	public void removeKeywordContext(final KeywordContext keyword) {
		this.keywords.remove(keyword);
	}

	public List<String> getCompletions() {
		return Collections.unmodifiableList(this.completions);
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(this.getIdentifier());
		pto.openList();
		if (this.isLatexToUnicode()) {
			pto.printAtom(LATEX_TO_UNICODE);
		}
		if (this.isAsciiToUnicode()) {
			pto.printAtom(ASCII_TO_UNICODE);
		}
		if (this.isIgnoreCase()) {
			pto.printAtom(IGNORE_CASE_ATOM);
		}
		if (this.isLatexOnly()) {
			pto.openTerm(KEYWORDS_ATOM);
			pto.printAtom(LATEX_ONLY_ATOM);
			pto.closeTerm();
		} else {
			for (KeywordContext keyword : this.keywords) {
				pto.openTerm(KEYWORDS_ATOM);
				pto.printAtom(keyword.getAtom());
				pto.closeTerm();
			}
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
