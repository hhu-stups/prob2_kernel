package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

public final class CompleteIdentifierCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "get_possible_completions";
	private static final String LATEX_TO_UNICODE = "latex_to_unicode";
	private static final String ASCII_TO_UNICODE = "ascii_to_unicode";
	private static final String IGNORE_CASE_ATOM = "lower_case";
	private static final String DOT_ONLY_ATOM = "dot_only";
	private static final String LATEX_ONLY_ATOM = "latex_only";
	// ignore keyword "special_categories_only": it provides prefixes for the 'only'-categories;
	// the category is set directly in this class
	private static final String SVG_ONLY_ATOM = "svg_only";
	private static final String SVG_COLORS_ONLY_ATOM = "svg_colors_only";
	private static final String STRINGS_ONLY_ATOM = "strings_only";
	private static final String KEYWORDS_ATOM = "keywords";

	private static final String COMPLETIONS_VAR = "Completions";

	private final String identifier;
	private boolean ignoreCase;
	private boolean latexToUnicode;
	private boolean asciiToUnicode;
	private boolean dotOnly;
	private boolean latexOnly;
	private boolean svgOnly;
	private boolean svgColorsOnly;
	private boolean stringsOnly;
	private final List<KeywordContext> keywords;
	private List<Completion> completions;

	public CompleteIdentifierCommand(String identifier) {
		super();
		this.identifier = identifier;
		this.ignoreCase = false;
		this.keywords = new ArrayList<>();
		this.completions = null;
	}

	private void checkMutuallyExclusiveOptions() {
		if (!this.keywords.isEmpty() && (this.isDotOnly() || this.isLatexOnly() || this.isSvgOnly() || this.isSvgColorsOnly() || this.isStringsOnly())) {
			throw new IllegalStateException("'only'-arguments and keyword contexts are mutually exclusive");
		}
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

	public boolean isDotOnly() {
		return this.dotOnly;
	}

	public void setDotOnly(boolean dotOnly) {
		this.dotOnly = dotOnly;
		this.checkMutuallyExclusiveOptions();
	}

	public boolean isLatexOnly() {
		return this.latexOnly;
	}

	public void setLatexOnly(boolean latexOnly) {
		this.latexOnly = latexOnly;
		this.checkMutuallyExclusiveOptions();
	}

	public boolean isSvgOnly() {
		return this.svgOnly;
	}

	public void setSvgOnly(boolean svgOnly) {
		this.svgOnly = svgOnly;
		this.checkMutuallyExclusiveOptions();
	}

	public boolean isSvgColorsOnly() {
		return this.svgColorsOnly;
	}

	public void setSvgColorsOnly(boolean svgColorsOnly) {
		this.svgColorsOnly = svgColorsOnly;
		this.checkMutuallyExclusiveOptions();
	}

	public boolean isStringsOnly() {
		return this.stringsOnly;
	}

	public void setStringsOnly(boolean stringsOnly) {
		this.stringsOnly = stringsOnly;
		this.checkMutuallyExclusiveOptions();
	}

	public List<KeywordContext> getKeywordContexts() {
		return Collections.unmodifiableList(this.keywords);
	}

	public void addKeywordContext(KeywordContext keyword) {
		this.keywords.add(keyword);
		this.checkMutuallyExclusiveOptions();
	}

	public void removeKeywordContext(KeywordContext keyword) {
		this.keywords.remove(keyword);
	}

	public List<Completion> getCompletions() {
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
		if (this.isDotOnly()) {
			pto.openTerm(KEYWORDS_ATOM);
			pto.printAtom(DOT_ONLY_ATOM);
			pto.closeTerm();
		} else if (this.isLatexOnly()) {
			pto.openTerm(KEYWORDS_ATOM);
			pto.printAtom(LATEX_ONLY_ATOM);
			pto.closeTerm();
		} else if (this.isSvgOnly()) {
			pto.openTerm(KEYWORDS_ATOM);
			pto.printAtom(SVG_ONLY_ATOM);
			pto.closeTerm();
		} else if (this.isSvgColorsOnly()) {
			pto.openTerm(KEYWORDS_ATOM);
			pto.printAtom(SVG_COLORS_ONLY_ATOM);
			pto.closeTerm();
		} else if (this.isStringsOnly()) {
			pto.openTerm(KEYWORDS_ATOM);
			pto.printAtom(STRINGS_ONLY_ATOM);
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
		ListPrologTerm completionsWithType = BindingGenerator.getList(bindings, COMPLETIONS_VAR);
		this.completions = completionsWithType.stream()
			.map(Completion::fromPrologTerm)
			.collect(Collectors.toList());
	}

	public enum KeywordContext {

		LATEX("latex"),
		EXPR("expr"),
		ALL("all"),
		SVG("svg"),
		STRING("string");

		private final String atom;

		KeywordContext(String atom) {
			this.atom = atom;
		}

		public String getAtom() {
			return this.atom;
		}
	}

	public static class Completion {
		private final String completion, type;

		public Completion(final String completion, final String type) {
			this.completion = completion;
			this.type = type;
		}

		public static Completion fromPrologTerm(PrologTerm term) {
			CompoundPrologTerm compound = BindingGenerator.getCompoundTerm(term, "completion", 2);
			return new Completion(compound.getArgument(1).atomToString(), compound.getArgument(2).atomToString());
		}

		public String getCompletion() {
			return this.completion;
		}

		public String getType() {
			return this.type;
		}
	}
}
