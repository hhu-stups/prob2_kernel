package de.prob.scripting;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.CachingDefinitionFileProvider;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.annotations.Home;
import de.prob.exception.ProBError;
import de.prob.model.classicalb.ClassicalBModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates new {@link ClassicalBModel} objects.
 *
 * @author joy
 *
 */
public class ClassicalBFactory implements ModelFactory<ClassicalBModel> {

	Logger logger = LoggerFactory.getLogger(ClassicalBFactory.class);
	private final Provider<ClassicalBModel> modelCreator;
	public static final String CLASSICAL_B_MACHINE_EXTENSION = "mch";
	public static final String CLASSICAL_B_REFINEMENT_EXTENSION = "ref";
	public static final String CLASSICAL_B_IMPLEMENTATION_EXTENSION = "imp";


	@Inject
	public ClassicalBFactory(final Provider<ClassicalBModel> modelCreator, final @Home String probdir) {
		this.modelCreator = modelCreator;

		// Provide location of ProB stdlib directory to the parser,
		// if it hasn't been set already.
		if (System.getProperty("prob.stdlib") == null) {
			System.setProperty("prob.stdlib", probdir + File.separator + "stdlib");
		}
	}

	private static ParsingBehaviour getDefaultParsingBehaviour() {
		final ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setAddLineNumbers(true);
		return parsingBehaviour;
	}

	@Override
	public ExtractedModel<ClassicalBModel> extract(final String modelPath) throws IOException {
		ClassicalBModel classicalBModel = modelCreator.get();

		File f = new File(modelPath);
		BParser bparser = new BParser(modelPath);

		logger.trace("Parsing main file '{}'", f.getAbsolutePath());
		Start ast;
		final RecursiveMachineLoader rml;
		try {
			ast = bparser.parseFile(f);
			rml = RecursiveMachineLoader.loadFromAst(bparser, ast, getDefaultParsingBehaviour(), bparser.getContentProvider());
		} catch (BCompoundException e) {
			List<BException> exceptions = e.getBExceptions();
			if (exceptions.size() == 1 && exceptions.get(0).getCause() instanceof IOException) {
				throw (IOException)exceptions.get(0).getCause();
			} else {
				throw new ProBError(e);
			}
		}
		classicalBModel = classicalBModel.create(ast, rml, f, bparser);
		return new ExtractedModel<>(classicalBModel, classicalBModel.getMainMachine());
	}

	public ExtractedModel<ClassicalBModel> create(final String model) {
		return create("from_string", model);
	}

	public ExtractedModel<ClassicalBModel> create(final String name, final String model) {
		ClassicalBModel classicalBModel = modelCreator.get();
		BParser bparser = new BParser(name + "." + CLASSICAL_B_MACHINE_EXTENSION);

		Start ast;
		final RecursiveMachineLoader rml;
		try {
			ast = bparser.parseMachine(model);
			rml = RecursiveMachineLoader.loadFromAst(bparser, ast, getDefaultParsingBehaviour(), bparser.getContentProvider());
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
		classicalBModel = classicalBModel.create(ast, rml, new File(name + "." + CLASSICAL_B_MACHINE_EXTENSION), bparser);
		return new ExtractedModel<>(classicalBModel, classicalBModel.getMainMachine());
	}

	public ExtractedModel<ClassicalBModel> create(final Start model) {
		return create("from_string", model);
	}
	
	public ExtractedModel<ClassicalBModel> create(final String name, final Start model) {
		ClassicalBModel classicalBModel = modelCreator.get();
		BParser bparser = new BParser(name + "." + CLASSICAL_B_MACHINE_EXTENSION);

		final RecursiveMachineLoader rml;
		try {
			rml = RecursiveMachineLoader.loadFromAst(bparser, model, getDefaultParsingBehaviour(), new CachingDefinitionFileProvider());
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
		classicalBModel = classicalBModel.create(model, rml, new File(name + "." + CLASSICAL_B_MACHINE_EXTENSION), bparser);
		return new ExtractedModel<>(classicalBModel, classicalBModel.getMainMachine());
	}
}
