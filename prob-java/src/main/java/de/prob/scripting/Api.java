package de.prob.scripting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import com.google.common.io.MoreFiles;
import com.google.inject.Inject;

import de.be4.classicalb.core.parser.node.Start;
import de.prob.exception.CliError;
import de.prob.exception.ProBError;
import de.prob.model.brules.RulesModelFactory;
import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.translate.EventBModelTranslator;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;
import de.prob.statespace.StateSpace;

public class Api {
	private final FactoryProvider modelFactoryProvider;

	/**
	 * Create an {@link Api} object. Normally this constructor should not be used directly, {@link Api} objects should be obtained using injection instead.
	 */
	@Inject
	Api(FactoryProvider modelFactoryProvider) {
		this.modelFactoryProvider = modelFactoryProvider;
	}

	@Override
	public String toString() {
		return "ProB Connector";
	}

	/**
	 * Returns a string representation of the currently available commands for the Api object. Intended to ease use in the Groovy console.
	 * 
	 * @return a string representation of the currently available commands
	 */
	public String help() {
		return "Api Commands:\n\n"
			+ " String help(): print out available commands\n"
			+ " StateSpace b_load(String filePath, [Map<String, String> prefs]): load a classical B machine from a .mch file\n"
			+ " StateSpace b_load(Start ast, [Map<String, String> prefs]): load aclassical B machine from an AST\n"
			+ " StateSpace eventb_load(String filePath, [Map<String, String> prefs]): load an EventB machine from a file\n"
			+ " void eventb_save(StateSpace stateSpace, String filePath): save an EventB state space to a file\n"
			+ " StateSpace tla_load(String filePath, [Map<String, String> prefs]): load a .tla file\n"
			+ " StateSpace brules_load(String filePath, [Map<String, String> prefs]): load a B rules machine from a .rmch file\n"
			+ " StateSpace csp_load(String filePath, [Map<String, String> prefs]): load a .csp file\n"
		;
	}

	/**
	 * Load a Classical B machine from the given file.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded machine
	 */
	public StateSpace b_load(final String file, final Map<String, String> prefs) throws IOException {
		final ClassicalBFactory bFactory = modelFactoryProvider.getClassicalBFactory();
		return bFactory.extract(file).load(prefs);
	}

	/**
	 * Load a Classical B machine from the given file.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded machine
	 */
	public StateSpace b_load(final String file) throws IOException {
		return b_load(file, Collections.emptyMap());
	}

	/**
	 * Load a Classical B machine from the given AST.
	 * 
	 * @param ast the AST to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded machine
	 */
	public StateSpace b_load(final Start ast, final Map<String, String> prefs) throws IOException {
		final ClassicalBFactory bFactory = modelFactoryProvider.getClassicalBFactory();
		return bFactory.create(ast).load(prefs);
	}

	/**
	 * Load a Classical B machine from the given AST.
	 *
	 * @param ast the AST to load
	 * @return the {@link StateSpace} for the loaded machine
	 */
	public StateSpace b_load(final Start ast) throws IOException {
		return b_load(ast, Collections.emptyMap());
	}

	/**
	 * Load an EventB machine from the given file.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded machine
	 */
	public StateSpace eventb_load(final String file, final Map<String, String> prefs) throws IOException {
		final ModelFactory<EventBModel> factory;
		if (EventBPackageFactory.EXTENSION.equals(MoreFiles.getFileExtension(Paths.get(file)))) {
			factory = modelFactoryProvider.getEventBPackageFactory();
		} else {
			factory = modelFactoryProvider.getEventBFactory();
		}
		return factory.extract(file).load(prefs);
	}

	/**
	 * Load an EventB machine from the given file.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded machine
	 */
	public StateSpace eventb_load(final String file) throws IOException {
		return eventb_load(file, Collections.emptyMap());
	}

	/**
	 * Save an EventB {@link StateSpace} to the given {@link IPrologTermOutput}.
	 * 
	 * @param s the {@link StateSpace} to save, whose model must be an EventB model
	 * @param pto where to output the saved Prolog term
	 */
	public void eventb_save(final StateSpace s, final IPrologTermOutput pto) {
		final EventBModelTranslator translator = new EventBModelTranslator((EventBModel)s.getModel());
		translator.printPrologFact(pto);
	}

	/**
	 * Save an EventB {@link StateSpace} to the given file.
	 * 
	 * @param s the {@link StateSpace} to save, whose model must be an EventB model
	 * @param path the path of the file to save to
	 */
	public void eventb_save(final StateSpace s, final String path) throws IOException {
		try (final FileOutputStream fos = new FileOutputStream(path)) {
			eventb_save(s, new PrologTermOutput(fos, false));
		}
	}

	/**
	 * Load a TLA model from the given file.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded model
	 */
	public StateSpace tla_load(final String file, final Map<String, String> prefs) throws IOException {
		final TLAFactory tlaFactory = modelFactoryProvider.getTLAFactory();
		return tlaFactory.extract(file).load(prefs);
	}

	/**
	 * Load a TLA model from the given file.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded model
	 */
	public StateSpace tla_load(final String file) throws IOException {
		return tla_load(file, Collections.emptyMap());
	}

	/**
	 * Load a B rules machine from the given file.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded machine
	 */
	public StateSpace brules_load(final String file, final Map<String, String> prefs) throws IOException {
		final RulesModelFactory bRulesFactory = modelFactoryProvider.getBRulesFactory();
		return bRulesFactory.extract(file).load(prefs);
	}

	/**
	 * Load a B rules machine from the given file.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded machine
	 */
	public StateSpace brules_load(final String file) throws IOException {
		return brules_load(file, Collections.emptyMap());
	}

	/**
	 * Load a CSP model from the given file. This requires the {@code cspm} parser to be installed.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded model
	 * @throws CliError if the {@code cspm} parser is not installed
	 */
	public StateSpace csp_load(final String file, final Map<String, String> prefs) throws IOException {
		final CSPFactory cspFactory = modelFactoryProvider.getCspFactory();
		try {
			return cspFactory.extract(file).load(prefs);
		} catch (ProBError error) {
			throw new CliError(
				"Could not find CSP Parser. Perform 'installCSPM' to install cspm in your ProB lib directory",
				error
			);
		}
	}

	/**
	 * Load a CSP model from the given file. This requires the {@code cspm} parser to be installed.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded model
	 * @throws CliError if the {@code cspm} parser is not installed
	 */
	public StateSpace csp_load(final String file) throws IOException {
		return csp_load(file, Collections.emptyMap());
	}

	/**
	 * Load an XTL model from the given file.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded model
	 */
	public StateSpace xtl_load(final String file, final Map<String, String> prefs) throws IOException {
		final XTLFactory xtlFactory = modelFactoryProvider.getXTLFactory();
		return xtlFactory.extract(file).load(prefs);
	}

	/**
	 * Load an XTL model from the given file.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded model
	 */
	public StateSpace xtl_load(final String file) throws IOException {
		return xtl_load(file, Collections.emptyMap());
	}

	/**
	 * Load a Z model from the given file.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded model
	 */
	public StateSpace z_load(final String file, final Map<String, String> prefs) throws IOException {
		final ZFactory zFactory = modelFactoryProvider.getZFactory();
		return zFactory.extract(file).load(prefs);
	}

	/**
	 * Load a Z model from the given file.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded model
	 */
	public StateSpace z_load(final String file) throws IOException {
		return z_load(file, Collections.emptyMap());
	}

	/**
	 * Load an Alloy model from the given file.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded model
	 */
	public StateSpace alloy_load(final String file, final Map<String, String> prefs) throws IOException {
		final AlloyFactory alloyFactory = modelFactoryProvider.getAlloyFactory();
		return alloyFactory.extract(file).load(prefs);
	}

	/**
	 * Load an Alloy model from the given file.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded model
	 */
	public StateSpace alloy_load(final String file) throws IOException {
		return alloy_load(file, Collections.emptyMap());
	}

	/**
	 * Load a model from the given file.
	 * The type/formalism of the model is determined automatically from the file extension.
	 *
	 * @param file the path of the file to load
	 * @param prefs the preferences to use
	 * @return the {@link StateSpace} for the loaded machine
	 * @throws IllegalArgumentException if the file extension is not recognized or supported
	 */
	public StateSpace load(String file, Map<String, String> prefs) throws IOException {
		String extension = MoreFiles.getFileExtension(Paths.get(file));
		return modelFactoryProvider.getFactoryForExtension(extension).extract(file).load(prefs);
	}

	/**
	 * Load a model from the given file.
	 * The type/formalism of the model is determined automatically from the file extension.
	 *
	 * @param file the path of the file to load
	 * @return the {@link StateSpace} for the loaded machine
	 * @throws IllegalArgumentException if the file extension is not recognized or supported
	 */
	public StateSpace load(String file) throws IOException {
		return load(file, Collections.emptyMap());
	}
}
