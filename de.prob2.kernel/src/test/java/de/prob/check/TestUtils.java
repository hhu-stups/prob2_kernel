package de.prob.check;

import de.prob.check.tracereplay.check.ui.ProgressMemoryInterface;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.ui.MappingFactoryInterface;
import de.prob.check.tracereplay.check.ui.ToManyOptionsIdentifierMapping;

public class TestUtils {

	public static class StubFactoryImplementation implements MappingFactoryInterface {

		@Override
		public ToManyOptionsIdentifierMapping produceMappingManager() {
			return (oldInfo, newInfo, name, section) -> TraceCheckerUtils.zipPreserveOrder(oldInfo, newInfo);
		}
	}

	public static class ProgressStubFactory implements ProgressMemoryInterface{


		@Override
		public void addTask() {

		}

		@Override
		public void addTasks(int count) {

		}

		@Override
		public void fulfillTask() {

		}

		@Override
		public void fulfillTasks(int count) {

		}

		@Override
		public void nextStep() {

		}


	}
}
