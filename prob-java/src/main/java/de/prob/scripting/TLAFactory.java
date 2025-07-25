package de.prob.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.exception.ProBError;
import de.prob.model.representation.TLAModel;
import de.prob.statespace.StateSpace;
import de.tla2b.exceptions.TLA2BException;
import de.tla2bAst.Translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TLAFactory implements ModelFactory<TLAModel> {

	private static final Logger logger = LoggerFactory.getLogger(TLAFactory.class);

	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<TLAModel> modelCreator;

	@Inject
	TLAFactory(Provider<StateSpace> stateSpaceProvider, Provider<TLAModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<TLAModel> extract(final String fileName) throws IOException {
		TLAModel tlaModel = modelCreator.get();
		File f = new File(fileName);
		if (!f.exists()) {
			throw new FileNotFoundException("The TLA Model" + fileName + " was not found.");
		}

		Translator translator;
		Start ast;
		try {
			translator = new Translator(f.getAbsolutePath());
			ast = translator.translate();
		} catch (TLA2BException e) {
			throw new ProBError(e);
		}

		BParser bparser = new BParser(fileName);
		try {
			bparser.getDefinitions().addDefinitions(translator.getBDefinitions());
		} catch (PreParseException e) {
			throw new ProBError(e);
		}
		final RecursiveMachineLoader rml;
		try {
			rml = translator.parseAllMachines(ast, f, bparser);
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
		logger.trace("Done parsing '{}'", f.getAbsolutePath());
		tlaModel = tlaModel.create(ast, rml, f, bparser, translator);
		return new ExtractedModel<>(stateSpaceProvider, tlaModel);
	}
}
