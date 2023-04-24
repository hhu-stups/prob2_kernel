package de.prob.animator.command;

import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.MockedDefinitions;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.PrologExceptionPrinter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.CommandInterruptedException;
import de.prob.animator.IPrologResult;
import de.prob.animator.InterruptedResult;
import de.prob.animator.NoResult;
import de.prob.animator.YesResult;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.exception.ProBError;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.parser.ResultParserException;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

/**
 * The {@link AbstractCommand} class is used to implement composable
 * interactions with the ProB core. It defines two callback methods that are
 * being called by the Animator when the command is being performed. It also
 * provides a {@link #getSubcommands()} method to break down the command into
 * separate commands to improve debugging.
 * 
 * @author joy
 * 
 */
public abstract class AbstractCommand {
	/**
	 * Creates the prolog term that is sent to the core. It gets the term output
	 * object from the animator. The animator will automatically take care of
	 * name clashes when Prolog variables are used.
	 * 
	 * @param pto
	 *            {@link de.prob.prolog.output.IPrologTermOutput} that must be
	 *            used to write the query term.
	 */
	public abstract void writeCommand(IPrologTermOutput pto);

	/**
	 * <p>
	 * After performing the query this method receives a Map of bindings from
	 * variable names used in the query to Prolog terms representing the answer.
	 * </p>
	 * 
	 * <p>
	 * A number of helper tools can be used when processing the results (see
	 * {@link de.prob.parser.BindingGenerator})
	 * </p>
	 * 
	 * <p>
	 * This will be called if the Prolog query was successful and no error
	 * messages were logged during the execution of the query. If the query was
	 * not successful, or if there were errors
	 * {@link AbstractCommand#processErrorResult(IPrologResult, List)} will
	 * be called.
	 * </p>
	 * 
	 * <p>
	 * Note: This method is allowed to throw {@link ResultParserException} if
	 * the answer from Prolog does not match the expectation. The exception is a
	 * subclass of RuntimeException and it should always indicate a bug (or
	 * version inconsistency)
	 * </p>
	 * 
	 * @param bindings
	 *            {@link ISimplifiedROMap} of String variable names to their
	 *            calculated answers represented as {@link PrologTerm}s
	 */
	public abstract void processResult(
			ISimplifiedROMap<String, PrologTerm> bindings);

	/**
	 * Returns the list of sub-commands contained in a given
	 * {@link AbstractCommand}. This allow the animator to debug the code. If
	 * developers want individual commands to be executed separately in debug
	 * mode when an {@link AbstractCommand} is executed, then this method MUST
	 * be overridden. By default, {@link Collections#emptyList()} is returned.
	 * 
	 * @return {@code List} of {@link AbstractCommand} subcommands
	 */
	public List<AbstractCommand> getSubcommands() {
		return Collections.emptyList();
	}

	/**
	 * This code is called in three cases:
	 * <ol>
	 * <li>The Prolog query was unsuccessful (answered no) and there were no
	 * errors logged.</li>
	 * <li>The Prolog query was unsuccessful (answered no) and errors were found
	 * </li>
	 * <li>The Prolog query was successful (and bindings have been generated),
	 * but errors were also found</li>
	 * </ol>
	 * 
	 * Default behavior for error handling is implemented in
	 * {@link AbstractCommand}, but if a developer wants to implement special
	 * behavior, he/she needs to overwrite this method.
	 * 
	 * @param result the result returned from Prolog
	 * @param errors the error messages and locations that were reported
	 */
	public void processErrorResult(final IPrologResult result, final List<ErrorItem> errors) {
		if (result instanceof NoResult) {
			throw new ProBError("Prolog said no.", errors);
		} else if (result instanceof InterruptedResult) {
			throw new CommandInterruptedException("ProB was interrupted", errors);
		} else if (result instanceof YesResult) {
			processResult(((YesResult) result).getBindings());
			throw new ProBError("ProB reported Errors", errors);
		} else {
			throw new ProBError("Errors were", errors);
		}
	}
	
	/**
	 * This code is called when the Prolog process sends a progress term
	 * but hasn't finished computation yet
	 */
	public void processProgressResult(final PrologTerm progressInfo) {
		// TODO
	}
	
	
	/**
	 * This code is called when the Prolog process sends a call_back term
	 * but hasn't finished computation yet
	 */
	public void processCallBack(final PrologTerm callBack, final IPrologTermOutput pout) {
		if (callBack.hasFunctor("interrupt_requested",0)) {
			if (Thread.interrupted()) {
				pout.printAtom("interrupt_is_requested");
			} else {
				pout.printAtom("not_requested");
			}
		} else if (callBack.hasFunctor("parse_classical_b",3)) {
			// parse_classical_b(Kind,DefList,Formula)
			// we could use: (new ClassicalB(formulaToEval, FormulaExpand.EXPAND), but
			// it will also try parsing as substitution and creates an uncessary UUID
			// TO DO : support multiple formulas, support parse_event_b call_back as well
			// parse_classical_b(Kind,toParse)
			String Kind = callBack.getArgument(1).atomToString();
			// Kind is formula, expression, predicate, substitution
			// TO DO: process argument 2 as list ; it contains def(Name,Type,Arity) Terms
			ListPrologTerm definitions = (ListPrologTerm) callBack.getArgument(2);
			String toParse = callBack.getArgument(3).atomToString();
			try {
				Start ast;
				BParser parser = new BParser();
				MockedDefinitions context = new MockedDefinitions();
				for (PrologTerm definition : definitions) {
					BindingGenerator.getCompoundTerm(definition, "def", 3);
					String name = definition.getArgument(1).atomToString();
					String type = definition.getArgument(2).toString();
					String parameterCount = definition.getArgument(3).toString();
					context.addMockedDefinition(name, type, parameterCount);
				}
				parser.setDefinitions(context);
				switch (Kind) {
					case "formula":
						ast = parser.parseFormula(toParse); break;
					case "expression":
						ast = parser.parseExpression(toParse); break;
					case "predicate":
						ast = parser.parsePredicate(toParse); break;
					case "substitution":
						ast = parser.parseSubstitution(toParse); break;
					case "transition":
						ast = parser.parseTransition(toParse); break;
					default:
						throw new IllegalArgumentException("Invalid kind for parse_classical_b: " + Kind);
				}
				ASTProlog.printFormula(ast, pout);
			} catch (BCompoundException e) {
				pout.openTerm("parse_error");
				PrologExceptionPrinter.printException(pout, e);
				pout.closeTerm();
			}
		} else {
			pout.printAtom("call_back_not_supported");
		}
	}
}
