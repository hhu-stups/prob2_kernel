package de.prob.scripting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.CachingDefinitionFileProvider;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.checking.DefinitionCollector;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.annotations.Home;
import de.prob.exception.ProBError;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.statespace.StateSpace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates new {@link ClassicalBModel} objects.
 *
 * @author joy
 *
 */
public class ClassicalBFactory implements ModelFactory<ClassicalBModel> {
	public static final String CLASSICAL_B_MACHINE_EXTENSION = "mch";
	public static final String CLASSICAL_B_REFINEMENT_EXTENSION = "ref";
	public static final String CLASSICAL_B_IMPLEMENTATION_EXTENSION = "imp";

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassicalBFactory.class);

	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<ClassicalBModel> modelCreator;

	@Inject
	ClassicalBFactory(Provider<StateSpace> stateSpaceProvider, Provider<ClassicalBModel> modelCreator, @Home Path proBDirectory) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;

		// Provide location of ProB stdlib directory to the parser,
		// if it hasn't been set already.
		if (System.getProperty("prob.stdlib") == null) {
			System.setProperty("prob.stdlib", proBDirectory.resolve("stdlib").toString());
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

		File modelFile = new File(modelPath);
		BParser bparser = new BParser(modelPath);

		LOGGER.trace("Parsing main file '{}'", modelFile.getAbsolutePath());
		Start ast;
		final RecursiveMachineLoader rml;
		try {
			ast = bparser.parseFile(modelFile);
			rml = RecursiveMachineLoader.loadFromAst(bparser, ast, getDefaultParsingBehaviour(), bparser.getContentProvider());
		} catch (BCompoundException e) {
			List<BException> exceptions = e.getBExceptions();
			if (exceptions.size() == 1 && exceptions.get(0).getCause() instanceof IOException && exceptions.get(0).getLocations().isEmpty()) {
				throw (IOException)exceptions.get(0).getCause();
			} else {
				throw new ProBError(e);
			}
		}
		classicalBModel = classicalBModel.create(ast, rml, modelFile, bparser);
		return new ExtractedModel<>(stateSpaceProvider, classicalBModel);
	}

	public ExtractedModel<ClassicalBModel> create(final String model) {
		return create("from_string", model);
	}

	public ExtractedModel<ClassicalBModel> create(final String name, final String model) {
		ClassicalBModel classicalBModel = modelCreator.get();
		File modelFile = new File(name + "." + CLASSICAL_B_MACHINE_EXTENSION);
		BParser bparser = new BParser(modelFile.getPath());

		Start ast;
		final RecursiveMachineLoader rml;
		try {
			ast = bparser.parseMachine(model);
			rml = RecursiveMachineLoader.loadFromAst(bparser, ast, getDefaultParsingBehaviour(), bparser.getContentProvider());
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
		classicalBModel = classicalBModel.create(ast, rml, modelFile, bparser);
		return new ExtractedModel<>(stateSpaceProvider, classicalBModel);
	}

	/**
	 * Create a classical B model from the given AST.
	 * <p>
	 * ATTENTION: file definitions do not work and some other syntax constructs are unsupported.
	 * <p>
	 * If you have any problems, try to pretty-print the AST and use {@link ClassicalBFactory#create(String)} or {@link ClassicalBFactory#extract(String)} instead.
	 *
	 * @param model machine ast
	 * @return extracted model
	 */
	public ExtractedModel<ClassicalBModel> create(final Start model) {
		return create("from_string", model);
	}

	/**
	 * Create a classical B model from the given AST.
	 * <p>
	 * ATTENTION: file definitions do not work and some other syntax constructs are unsupported.
	 * <p>
	 * If you have any problems, try to pretty-print the AST and use {@link ClassicalBFactory#create(String, String)} or {@link ClassicalBFactory#extract(String)} instead.
	 *
	 * @param name machine name
	 * @param model machine ast
	 * @return extracted model
	 */
	public ExtractedModel<ClassicalBModel> create(final String name, final Start model) {
		ClassicalBModel classicalBModel = modelCreator.get();
		File modelFile = new File(name + "." + CLASSICAL_B_MACHINE_EXTENSION);
		BParser bparser = new BParser(modelFile.getPath());
		new DefinitionCollector(bparser.getDefinitions()).collectDefinitions(model);
		// otherwise definitions are unknown, maybe this part can be moved to the RML?
		// TODO: make this work with file definitions, see https://github.com/hhu-stups/prob-issues/issues/387

		final RecursiveMachineLoader rml;
		try {
			rml = RecursiveMachineLoader.loadFromAst(bparser, model, getDefaultParsingBehaviour(), new CachingDefinitionFileProvider());
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
		classicalBModel = classicalBModel.create(model, rml, modelFile, bparser);
		return new ExtractedModel<>(stateSpaceProvider, classicalBModel);
	}
}
