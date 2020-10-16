package de.prob.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UtilMethodTest {

	JsonManager<PersistentTrace> jsonManager = null;

	@Before
	public void createJsonManager(){
		if(jsonManager==null) {
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			JsonManager<PersistentTrace> jsonManager = injector.getInstance(JsonManagerStub.class).jsonManager;
			jsonManager.initContext(new JsonManager.Context<>(new Gson(), PersistentTrace.class, "Trace", 1));
			this.jsonManager = jsonManager;
		}
	}


	@Test
	public void findClosestMatch_no_Element(){
		ArrayList<String> noElement = new ArrayList<>();

		String result = jsonManager.findClosestMatch("House", noElement);

		Assert.assertEquals("", result);
	}

	@Test
	public void findClosestMatch_one_Element(){
		String expected = "Mouse";
		List<String> oneElement = Collections.singletonList(expected);

		String result = jsonManager.findClosestMatch("House", oneElement);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void findClosestMatch_two_Elements(){
		String expected = "Mouse";
		String notExpected = "Zouse";
		List<String> oneElement = Arrays.asList(expected, notExpected);

		String result = jsonManager.findClosestMatch("House", oneElement);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void findClosestMatch_multiple_Elements(){

		List<String> oneElement = Arrays.asList("destState", "name", "$jacocoData", "params", "results", "preds", "destStateNotChanged");

		String result = jsonManager.findClosestMatch("result", oneElement);

		Assert.assertEquals("results", result);
	}

	@Test
	public void checkClassFieldsMatchJsonObject_PersistentTransition_fullMatch(){

		String testObject = "{\n" +
				"      \"name\": \"inc\",\n" +
				"      \"params\": {\n" +
				"        \"x\": \"2\"\n" +
				"      },\n" +
				"      \"results\": {},\n" +
				"      \"destState\": {\n" +
				"        \"floor\": \"1\"\n" +
				"      },\n" +
				"      \"destStateNotChanged\": [],\n" +
				"      \"preds\": null\n" +
				"    }";


		JsonObject jsonObject = jsonManager.getContext().gson.fromJson(testObject, JsonObject.class);

		jsonManager.checkClassFieldsMatchJsonObject(PersistentTransition.class, jsonObject);
	}


	@Test(expected = JsonParseException.class)
	public void checkClassFieldsMatchJsonObject_PersistentTransition_wrongFieldName(){

		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("name", "inc");
		JsonObject params = new JsonObject();
		params.addProperty("x", 2);
		JsonObject result = new JsonObject();
		JsonObject destState = new JsonObject();
		destState.addProperty("floor", 1);
		JsonArray destStateNotChanged = new JsonArray();


		jsonObject.add("params", params);
		jsonObject.add("result", result); //wrong needs to be "results"
		jsonObject.add("destState", destState);
		jsonObject.add("destStateNotChanged", destStateNotChanged);
		jsonObject.addProperty("preds", "null");

		jsonManager.checkClassFieldsMatchJsonObject(PersistentTransition.class, jsonObject);
	}


	@Test(expected = JsonParseException.class)
	public void checkClassFieldsMatchJsonObject_PersistentTransition_toManyFields(){

		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("name", "inc");
		jsonObject.addProperty("hallo", 123);

		JsonObject params = new JsonObject();
		params.addProperty("x", 2);
		JsonObject result = new JsonObject();
		JsonObject destState = new JsonObject();
		destState.addProperty("floor", 1);
		JsonArray destStateNotChanged = new JsonArray();


		jsonObject.add("params", params);
		jsonObject.add("results", result);
		jsonObject.add("destState", destState);
		jsonObject.add("destStateNotChanged", destStateNotChanged);
		jsonObject.addProperty("preds", "null");


		jsonManager.checkClassFieldsMatchJsonObject(PersistentTransition.class, jsonObject);
	}


	@Test(expected = JsonParseException.class)
	public void checkClassFieldsMatchJsonObject_PersistentTransition_missingField(){

		JsonObject jsonObject = new JsonObject();

		//"name" is missing

		JsonObject params = new JsonObject();
		params.addProperty("x", 2);
		JsonObject result = new JsonObject();
		JsonObject destState = new JsonObject();
		destState.addProperty("floor", 1);
		JsonArray destStateNotChanged = new JsonArray();


		jsonObject.add("params", params);
		jsonObject.add("results", result);
		jsonObject.add("destState", destState);
		jsonObject.add("destStateNotChanged", destStateNotChanged);
		jsonObject.addProperty("preds", "null");


		jsonManager.checkClassFieldsMatchJsonObject(PersistentTransition.class, jsonObject);
	}
	

	
}
