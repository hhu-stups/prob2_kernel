package de.prob.json;

import com.google.inject.Inject;
import de.prob.check.tracereplay.PersistentTrace;


/**
 * A wrapper class to avoid injecting a parametrized JsonManager
 */
public class JsonManagerStub {

	public JsonManager<PersistentTrace> jsonManager;

	@Inject
	public JsonManagerStub(JsonManager<PersistentTrace> jsonManager){
		this.jsonManager = jsonManager;
	}
}
