package de.prob.model.representation;

import java.io.File;
import java.util.Map;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.google.inject.Inject;

import de.prob.animator.command.LoadZCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;
import de.prob.statespace.StateSpace;

public class ZModel extends AbstractModel {
	@Inject
	public ZModel(final StateSpaceProvider ssProvider) {
		this(ssProvider, null);
	}

	public ZModel(final StateSpaceProvider ssProvider, File modelFile) {
		super(ssProvider, PersistentHashMap.emptyMap(), new DependencyGraph(), modelFile);
	}

	public ZModel create(final File modelFile) {
		return new ZModel(getStateSpaceProvider(), modelFile);
	}
	
	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new ClassicalB(formula, expand);
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.Z;
	}

	@Override
	public boolean checkSyntax(final String formula) {
		try {
			this.parseFormula(formula, FormulaExpand.TRUNCATE);
			return true;
		} catch (EvaluationException ignored) {
			return false;
		}
	}

	@Override
	public StateSpace load(final AbstractElement mainComponent, final Map<String, String> preferences) {
		return getStateSpaceProvider().loadFromCommand(this, mainComponent, preferences, new LoadZCommand(this.getModelFile().getAbsolutePath()));
	}

	@Override
	public AbstractElement getComponent(String name) {
		return null;
	}
}
