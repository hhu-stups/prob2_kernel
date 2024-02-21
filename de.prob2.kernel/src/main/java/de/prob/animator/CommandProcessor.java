package de.prob.animator;

import java.util.Map;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.IRawCommand;
import de.prob.cli.ProBInstance;
import de.prob.core.sablecc.node.ACallBackResult;
import de.prob.core.sablecc.node.AExceptionResult;
import de.prob.core.sablecc.node.AInterruptedResult;
import de.prob.core.sablecc.node.ANoResult;
import de.prob.core.sablecc.node.AProgressResult;
import de.prob.core.sablecc.node.AYesResult;
import de.prob.core.sablecc.node.PResult;
import de.prob.exception.ProBError;
import de.prob.exception.PrologException;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ProBResultParser;
import de.prob.parser.PrologTermGenerator;
import de.prob.parser.SimplifiedROMap;
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.prolog.term.PrologTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CommandProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandProcessor.class);

	private final ProBInstance cli;

	CommandProcessor(ProBInstance cli) {
		this.cli = cli;
	}

	private static String shorten(final String s) {
		final String shortened = s.length() <= 200 ? s : (s.substring(0, 200) + "...");
		return shortened.endsWith("\n") ? shortened.substring(0, shortened.length()-1) : shortened;
	}

	public IPrologResult sendCommand(final AbstractCommand command) {

		String query;
		if (command instanceof IRawCommand) {
			query = ((IRawCommand) command).getCommand();
			if (!query.endsWith(".")) {
				query += ".";
			}
		} else {
			PrologTermStringOutput pto = new PrologTermStringOutput();
			command.writeCommand(pto);
			pto.printAtom("true");
			query = pto.fullstop().toString();
		}
		String result = cli.send(query); // send the query and get Prolog's response

		PResult topnode = parseResult(result);
		while (topnode instanceof AProgressResult || topnode instanceof ACallBackResult) {
			if (topnode instanceof AProgressResult ) {
				// enable the command to respond to the progress information (e.g., by updating progress bar)
				command.processProgressResult(PrologTermGenerator.toPrologTerm(topnode));
				result = cli.receive(); // receive next term by Prolog
			} else {
				final PrologTermStringOutput pout = new PrologTermStringOutput();
				command.processCallBack(PrologTermGenerator.toPrologTerm(topnode), pout);
				result = cli.send(pout.fullstop().toString());
			}
			topnode = parseResult(result);
		}
		// command is finished, we can extract the result:
		IPrologResult extractResult = extractResult(topnode);
		if (LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled()) {
			String resultString = extractResult.toString();
			LOGGER.debug("Result: {}", shorten(resultString));
			LOGGER.trace("Full result: {}", resultString);
		}
		return extractResult;
	}

	private IPrologResult extractResult(PResult topnode) {
		if (topnode instanceof ANoResult) {
			return new NoResult();
		} else if (topnode instanceof AInterruptedResult) {
			return new InterruptedResult();
		} else if (topnode instanceof AYesResult) {
			Map<String, PrologTerm> binding = BindingGenerator.createBinding(PrologTermGenerator.toPrologTerm(topnode));
			return new YesResult(new SimplifiedROMap<>(binding));
		} else if (topnode instanceof AExceptionResult) {
			AExceptionResult r = (AExceptionResult) topnode;
			String message = r.getString().getText();
			throw new PrologException(message);
		} else {
			throw new ProBError("Unhandled Prolog result: " + topnode.getClass());
		}
	}

	private PResult parseResult(final String input) {
		return ProBResultParser.parse(input).getPResult();
	}
}
