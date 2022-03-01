package de.prob.scripting;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.prob.statespace.AnimationSelector;

@Singleton
public class ScriptEngineProvider implements Provider<ScriptEngine> {
	private final Api api;
	private final AnimationSelector animations;
	private final Injector injector;
	private final ScriptEngineManager manager;

	@Inject
	public ScriptEngineProvider(final Api api, final AnimationSelector animations, final Injector injector) {
		this.api = api;
		this.animations = animations;
		this.injector = injector;
		manager = new ScriptEngineManager(this.getClass().getClassLoader());
	}

	@Override
	public ScriptEngine get() {
		final ScriptEngine engine = manager.getEngineByName("Groovy");
		final Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
		bindings.put("api", api);
		bindings.put("animations", animations);
		bindings.put("engine", engine);
		bindings.put("injector", injector);
		return new GroovySE(engine);
	}
}
