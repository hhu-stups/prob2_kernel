package de.prob.animator.command;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AstLeafWalksCommand extends AbstractCommand {

	private final IEvalElement evalElement;

	static final String PROLOG_COMMAND_NAME = "ast_leaf_walks";
	static final String OUTVAR = "Walks";

	static final String AST_UP_SYMB = "↑";
	static final String AST_DOWN_SYMB = "↓";

	private Set<List<String>> walks;


	public AstLeafWalksCommand(IEvalElement evalElement) {
		this.evalElement = evalElement;
	}

	@Override
	public void writeCommand(IPrologTermOutput pout) {
		pout.openTerm(PROLOG_COMMAND_NAME);
		evalElement.printProlog(pout);
		pout.printVariable(OUTVAR);
		pout.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		walks = new HashSet<>();

		ListPrologTerm leafPaths = (ListPrologTerm) bindings.get(OUTVAR);

		for (PrologTerm path : leafPaths) {
			List<String> lp = translateLeafPath((CompoundPrologTerm) path);
			walks.add(lp);
		}
	}

	public Set<List<String>> getWalks() {
		return walks;
	}

	static List<String> translateLeafPath(CompoundPrologTerm path) {
		// Paths are of the form a-b-...-c-root(r)-(d-e-...-f)
		// Which can be written as
		// -(-(-(-(a,b), c), root(r)), -(-(d, e), f))
		List<PrologTerm> translatedProlog = new ArrayList<>();
		translateLeafPath(path, translatedProlog);
		return leafPathWithDirections(translatedProlog);
	}


	private static void translateLeafPath(CompoundPrologTerm path,
			List<PrologTerm> translated) {
		PrologTerm lhs = path.getArgument(1);
		PrologTerm rhs = path.getArgument(2);
		if (lhs.isAtomic()) {
			// case: -(a, b) or -(a, root(r))
			translated.add(lhs);
			translated.add(rhs);
		} else if (rhs.isAtomic()) {
			// case: -(..., f)
			translateLeafPath((CompoundPrologTerm) lhs, translated);
			translated.add(rhs);
		} else {
			translateLeafPath((CompoundPrologTerm) lhs, translated);
			if (rhs.hasFunctor("root", 1)) {
				translated.add(rhs);
			} else {
				translateLeafPath((CompoundPrologTerm) rhs, translated);
			}
		}
	}

	private static List<String> leafPathWithDirections(List<PrologTerm> leafPaths) {
		boolean pastRoot = false;
		List<String> translated = new ArrayList<>();
		for (PrologTerm term : leafPaths) {
			// check for root element
			if (term.hasFunctor("root", 1)) {
				pastRoot = true;
				translated.add(term.getArgument(1).toString());
			} else {
				String atom = term.toString();
				if (pastRoot) {
					atom = AST_DOWN_SYMB + atom;
				} else {
					atom = atom + AST_UP_SYMB;
				}
				translated.add(atom);
			}
		}

		return translated;
	}
}

