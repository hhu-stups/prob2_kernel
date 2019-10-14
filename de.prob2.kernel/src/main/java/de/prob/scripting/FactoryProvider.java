package de.prob.scripting;

import com.google.inject.Inject;

import de.prob.model.brules.RulesModelFactory;

/**
 * Provides the user with access to the {@link ClassicalBFactory},
 * {@link CSPFactory}, and {@link EventBFactory} objects that are injected into
 * the FactoryProvider
 * 
 * @author joy
 * 
 */
public class FactoryProvider {

	private final ClassicalBFactory classicalBFactory;
	private final CSPFactory cspFactory;
	private final EventBFactory eventBFactory;
	private final EventBPackageFactory eventBPackageFactory;
	private final TLAFactory tlaFactory;
	private final RulesModelFactory bRulesFactory;
	private final XTLFactory xtlFactory;
	private final ZFactory zFactory;
	private final AlloyFactory alloyFactory;

	@Inject
	public FactoryProvider(
		final ClassicalBFactory bfactory,
		final CSPFactory cspFactory,
		final EventBFactory eventBFactory,
		final EventBPackageFactory eventBPackageFactory,
		final TLAFactory tlaFactory,
		final RulesModelFactory bRulesFactory,
		final XTLFactory xtlFactory,
		final ZFactory zFactory,
		final AlloyFactory alloyFactory
	) {
		this.classicalBFactory = bfactory;
		this.cspFactory = cspFactory;
		this.eventBFactory = eventBFactory;
		this.eventBPackageFactory = eventBPackageFactory;
		this.tlaFactory = tlaFactory;
		this.bRulesFactory = bRulesFactory;
		this.xtlFactory = xtlFactory;
		this.zFactory = zFactory;
		this.alloyFactory = alloyFactory;
	}

	public ClassicalBFactory getClassicalBFactory() {
		return classicalBFactory;
	}

	public EventBFactory getEventBFactory() {
		return eventBFactory;
	}

	public EventBPackageFactory getEventBPackageFactory() {
		return eventBPackageFactory;
	}

	public CSPFactory getCspFactory() {
		return cspFactory;
	}

	public TLAFactory getTLAFactory() {
		return tlaFactory;
	}

	public RulesModelFactory getBRulesFactory() {
		return bRulesFactory;
	}

	public XTLFactory getXTLFactory() {
		return this.xtlFactory;
	}

	public ZFactory getZFactory() {
		return this.zFactory;
	}
	
	public AlloyFactory getAlloyFactory() {
		return this.alloyFactory;
	}
}
