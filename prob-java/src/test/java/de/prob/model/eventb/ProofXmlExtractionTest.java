package de.prob.model.eventb;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.unicode.UnicodeTranslator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProofXmlExtractionTest {

	@Test
	public void testSimpleTrafficLightPO() throws IOException {
		EventBModel model = EventBLoader.loadResource("trafficLight/mac.bum");
		EventBMachine machine = (EventBMachine) model.getMainComponent();
		for (ProofObligation po : machine.getProofs()) {
			if ("set_peds_go/inv3/INV".equals(po.getName())) {
				assertEquals(Arrays.asList("¬(cars_go=TRUE∧peds_go=TRUE)","cars_go∈BOOL","peds_go∈BOOL","cars_go=FALSE"),
						translateToRodinUnicode(po.getHypotheses()));
				assertEquals(Arrays.asList("cars_go=FALSE","¬(cars_go=TRUE∧peds_go=TRUE)"),
						translateToRodinUnicode(po.getSelectedHypotheses()));
				assertEquals("¬(cars_go=TRUE∧TRUE=TRUE)", translateToRodinUnicode(po.getGoal()));
				Map<String,IEvalElement> ids = po.getIdentifiers();
				assertEquals("BOOL", ids.get("cars_go").getCode());
				assertEquals("BOOL", ids.get("peds_go").getCode());
				assertEquals("BOOL", ids.get("peds_go'").getCode());
				assertEquals(3, ids.size());
			}
		}
	}

	private static List<String> translateToRodinUnicode(List<IEvalElement> elements) {
		return elements.stream().map(ProofXmlExtractionTest::translateToRodinUnicode).collect(Collectors.toList());
	}

	private static String translateToRodinUnicode(IEvalElement element) {
		return UnicodeTranslator.toRodinUnicode(element.getCode());
	}
}
