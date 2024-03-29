package de.prob.animator.command;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoadBProjectCommandTest {

	@Test
	public void testWriteCommand() throws BCompoundException, URISyntaxException, IOException {
		URL resource = this.getClass().getResource("/de/prob/testmachines/b/scheduler.mch");
		assertNotNull(resource);
		File f =  new File(resource.toURI());
		StructuredPrologOutput prologTermOutput = new StructuredPrologOutput();
		final ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setAddLineNumbers(true);
		RecursiveMachineLoader rml = RecursiveMachineLoader.loadFile(f, parsingBehaviour);

		LoadBProjectCommand command = new LoadBProjectCommand(rml, f);
		command.writeCommand(prologTermOutput);
		prologTermOutput.fullstop().flush();
		Collection<PrologTerm> sentences = prologTermOutput.getSentences();
		PrologTerm next = sentences.iterator().next();
		assertNotNull(next);
		assertTrue(next instanceof CompoundPrologTerm);
		CompoundPrologTerm t = (CompoundPrologTerm) next;
		assertEquals("load_classical_b_from_list_of_facts", t.getFunctor());
		assertEquals(2, t.getArity());

		PrologTerm argument = t.getArgument(2);
		assertTrue(argument.isList());

	}

}
