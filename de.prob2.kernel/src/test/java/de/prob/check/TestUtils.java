package de.prob.check;

import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.exceptions.MappingFactoryInterface;
import de.prob.check.tracereplay.check.exceptions.ToManyOptionsIdentifierMapping;

public class TestUtils {

	public static class StubFactoryImplementation implements MappingFactoryInterface {

		@Override
		public ToManyOptionsIdentifierMapping produceMappingManager() {
			return (oldInfo, newInfo, name, section) -> TraceCheckerUtils.zipPreserveOrder(oldInfo, newInfo);
		}
	}
}
