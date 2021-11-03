package de.prob.scripting;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.CachingDefinitionFileProvider;
import de.be4.classicalb.core.parser.IDefinitionFileProvider;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.PlainFileContentProvider;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
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
		// (The null check for probdir is only needed because this constructor is called manually from LoadBProjectCommandTest.testWriteCommand.
		// In all other cases, probdir is never null.)
		if (System.getProperty("prob.stdlib") == null && probdir != null) {
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
			ast = bparser.parseFile(f, false);
			rml = RecursiveMachineLoader.loadFromAst(bparser, ast, getDefaultParsingBehaviour(), bparser.getContentProvider());
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
		classicalBModel = classicalBModel.create(ast, rml, f, bparser);
		return new ExtractedModel<>(classicalBModel, classicalBModel.getMainMachine());
	}

	public ExtractedModel<ClassicalBModel> create(final String model) {
		return create("from_string", model);
	}

	public ExtractedModel<ClassicalBModel> create(final String name, final String model) {
		ClassicalBModel classicalBModel = modelCreator.get();
		BParser bparser = new BParser(name + ".mch");

		Start ast;
		final RecursiveMachineLoader rml;
		try {
			ast = bparser.parse(model, false, new PlainFileContentProvider());
			rml = RecursiveMachineLoader.loadFromAst(bparser, ast, getDefaultParsingBehaviour(), bparser.getContentProvider());
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
		classicalBModel = classicalBModel.create(ast, rml, new File(name + ".mch"), bparser);
		return new ExtractedModel<>(classicalBModel, classicalBModel.getMainMachine());
	}

	public ExtractedModel<ClassicalBModel> create(final Start model) {
		return create("from_string", model);
	}
	
	public ExtractedModel<ClassicalBModel> create(final String name, final Start model) {
		ClassicalBModel classicalBModel = modelCreator.get();
		BParser bparser = new BParser(name + ".mch");

		final RecursiveMachineLoader rml;
		try {
			rml = RecursiveMachineLoader.loadFromAst(bparser, model, getDefaultParsingBehaviour(), new CachingDefinitionFileProvider());
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
		classicalBModel = classicalBModel.create(model, rml, new File(name + ".mch"), bparser);
		return new ExtractedModel<>(classicalBModel, classicalBModel.getMainMachine());
	}

	/**
	 * Given an {@link Start} ast, {@link File} f, and {@link BParser} bparser,
	 * all machines are loaded.
	 *
	 * @param ast
	 *            {@link Start} representing the abstract syntax tree for the
	 *            machine
	 * @param directory
	 *            the directory relative to which machines should be loaded
	 * @param f
	 *            {@link File} containing machine
	 * @param contentProvider
	 *            the content provider to use
	 * @param bparser
	 *            {@link BParser} for parsing
	 * @return {@link RecursiveMachineLoader} rml with all loaded machines
	 * @throws ProBError
	 *             if the model could not be loaded
	 * @deprecated You're probably looking for {@link #extract(String)}.
	 *     If you need to manually load a machine,
	 *     use {@link RecursiveMachineLoader#loadFile(File, ParsingBehaviour, IDefinitionFileProvider)}
	 *     or {@link RecursiveMachineLoader#loadFromAst(BParser, Start, ParsingBehaviour, IDefinitionFileProvider)}.
	 */
	@Deprecated
	public RecursiveMachineLoader parseAllMachines(final Start ast, final String directory, final File f,
			final IDefinitionFileProvider contentProvider, final BParser bparser) {
		try {
			ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
			parsingBehaviour.setAddLineNumbers(true);
			final RecursiveMachineLoader rml = new RecursiveMachineLoader(directory, contentProvider, parsingBehaviour);

			rml.loadAllMachines(f, ast, bparser.getDefinitions());
			logger.trace("Done parsing '{}'", f.getAbsolutePath());
			return rml;
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
	}

	/**
	 * Parse a file into an AST {@link Start}.
	 * 
	 * @param model
	 *            {@link File} containing B machine
	 * @param bparser
	 *            {@link BParser} for parsing
	 * @return {@link Start} AST after parsing model with {@link BParser}
	 *         bparser
	 * @throws IOException
	 *             if an I/O error occurred
	 * @throws ProBError
	 *             if the file could not be parsed
	 * @deprecated This method only parses a single classical B file into an AST -
	 *     it does not parse referenced files and cannot be used to actually load the machine into ProB.
	 *     You're probably looking for {@link #extract(String)}.
	 *     If you really want to parse only a single file,
	 *     use {@link BParser#parseFile(File, boolean)} directly.
	 */
	@Deprecated
	public Start parseFile(final File model, final BParser bparser) throws IOException {
		try {
			logger.trace("Parsing main file '{}'", model.getAbsolutePath());
			return bparser.parseFile(model, false);
		} catch (BCompoundException e) {
			throw new ProBError(e);
		}
	}
}
