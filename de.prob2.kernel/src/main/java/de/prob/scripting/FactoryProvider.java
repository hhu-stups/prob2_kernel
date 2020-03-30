package de.prob.scripting;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	public static final Map<Class<? extends ModelFactory<?>>, List<String>> FACTORY_TO_EXTENSIONS_MAP;
	static {
		final Map<Class<? extends ModelFactory<?>>, List<String>> map = new LinkedHashMap<>();
		map.put(ClassicalBFactory.class, Arrays.asList("mch", "ref", "imp", "sys"));
		map.put(EventBFactory.class, Arrays.asList("bum", "buc"));
		map.put(EventBPackageFactory.class, Collections.singletonList("eventb"));
		map.put(CSPFactory.class, Arrays.asList("csp", "cspm"));
		map.put(TLAFactory.class, Collections.singletonList("tla"));
		map.put(RulesModelFactory.class, Collections.singletonList("rmch"));
		map.put(XTLFactory.class, Arrays.asList("P", "pl"));
		map.put(ZFactory.class, Arrays.asList("zed", "tex"));
		map.put(AlloyFactory.class, Collections.singletonList("als"));
		FACTORY_TO_EXTENSIONS_MAP = Collections.unmodifiableMap(map);
	}

	public static final Map<String, Class<? extends ModelFactory<?>>> EXTENSION_TO_FACTORY_MAP;
	static {
		final Map<String, Class<? extends ModelFactory<?>>> map = new LinkedHashMap<>();
		FACTORY_TO_EXTENSIONS_MAP.forEach((factory, extensions) ->
			extensions.forEach(extension -> map.put(extension, factory))
		);
		EXTENSION_TO_FACTORY_MAP = Collections.unmodifiableMap(map);
	}

	/**
	 * @deprecated Use the values of {@link #FACTORY_TO_EXTENSIONS_MAP} instead
	 */
	@Deprecated
	public static final List<String> EXTENSION_PATTERNS_ORDERED = Arrays.asList("*.mch", "*.ref", "*.imp", "*.sys", "*.bum", "*.buc", "*.eventb", "*.csp", "*.cspm", "*.tla", "*.rmch", "*.P", "*.pl", "*.zed", "*.tex", "*.als");

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
	
	public static boolean isExtensionKnown(final String ext) {
		return EXTENSION_TO_FACTORY_MAP.containsKey(ext);
	}
	
	public static Class<? extends ModelFactory<?>> factoryClassFromExtension(final String ext) {
		final Class<? extends ModelFactory<?>> factory = EXTENSION_TO_FACTORY_MAP.get(ext);
		if (factory == null) {
			throw new IllegalArgumentException(String.format("Could not determine machine type for extension %s", ext));
		}
		return factory;
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
