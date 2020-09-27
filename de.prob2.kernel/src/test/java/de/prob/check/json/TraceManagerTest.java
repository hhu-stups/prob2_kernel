package de.prob.check.json;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.check.tracereplay.json.TraceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TraceManagerTest {

	TraceManager traceManager = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null) {
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
		}
		
	}


	@Test
	public void deserialize_correct_file_test(){

	}


	@Test
	public void serialize_correct_data_structure_test(){
		
	}
}
