package de.prob.check.tracereplay.check;

public interface ProgressMemoryInterface {

	void addTask();

	void addTasks(int count);

	void fulfillTask();

	void fulfillTasks(int count);

	void nextStep();
}
