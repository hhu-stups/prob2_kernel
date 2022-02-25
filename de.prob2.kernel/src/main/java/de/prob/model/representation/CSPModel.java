package de.prob.model.representation;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadCSPCommand;
import de.prob.animator.domainobjects.CSP;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.cli.OsSpecificInfo;
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;

public class CSPModel extends AbstractModel {
	private final OsSpecificInfo osInfo;
	private final String content;
	private final CSPElement mainComponent;
	
	@Inject
	public CSPModel(final StateSpaceProvider ssProvider, final OsSpecificInfo osInfo) {
		this(ssProvider, osInfo, null, null, null);
	}

	public CSPModel(final StateSpaceProvider ssProvider, final OsSpecificInfo osInfo, String content, File modelFile, CSPElement mainComponent) {
		super(ssProvider, Collections.emptyMap(), new DependencyGraph(), modelFile);
		this.osInfo = osInfo;
		this.content = content;
		this.mainComponent = mainComponent;
	}

	public CSPModel create(final String content, final File modelFile) {
		return new CSPModel(getStateSpaceProvider(), this.osInfo, content, modelFile, new CSPElement(modelFile.getName()));
	}

	/**
	 * DO NOT CALL THIS METHOD - for internal use only by {@link CSP}.
	 * 
	 * @return OS-specific information for the current system
	 */
	public OsSpecificInfo getOsInfo() {
		return this.osInfo;
	}

	public String getContent() {
		return content;
	}

	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new CSP(formula, this);
	}

	@Override
	public IEvalElement formulaFromIdentifier(final List<String> identifier, final FormulaExpand expansion) {
		// TODO This only handles syntactically valid identifiers and not arbitrary strings
		return this.parseFormula(String.join(".", identifier), expansion);
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.CSP;
	}

	@Override
	public boolean checkSyntax(final String formula) {
		try {
			CSP element = (CSP) parseFormula(formula, FormulaExpand.TRUNCATE);
			element.printProlog(new PrologTermStringOutput());

			return true;
		} catch (EvaluationException e) {
			return false;
		}

	}

	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadCSPCommand(getModelFile().getAbsolutePath());
	}

	@Override
	public AbstractElement getComponent(String name) {
		if (mainComponent != null && name != null && name.equals(mainComponent.getName())) {
			return mainComponent;
		}

		return null;
	}

	@Override
	public Object getProperty(String name) {
		AbstractElement component = getComponent(name);
		return component != null ? component : super.getProperty(name);
	}

	public Object getAt(String name) {
		return getComponent(name);
	}
}
