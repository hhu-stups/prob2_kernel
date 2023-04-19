package de.prob.check.tracereplay.check.ui;

/**
 * Can be used to represent some sort of progress
 * The idea is that the thing that has progress is divided in steps, each step represents a part of the main work
 * further every step consists of tasks
 * While the number of steps necessary to fulfill the requirements can be known at the beginning the number of exact
 * tasks might be unknown to the programmer. Therefore with this interface it is possible to add additional task when entering
 * the step. Of course the progress needs to be decided in intervals, representing the steps and those intervals again
 * can be split to the size of the task...
 */
@Deprecated
public interface ProgressMemoryInterface {

	/**
	 * Adds a single task
	 */
	void addTask();

	/**
	 * Adds multiple Tasks
	 * @param count the number of tasks
	 */
	void addTasks(int count);

	/**
	 * Fulfills one task
	 */
	void fulfillTask();

	/**
	 * Fulfills a number of tasks
	 * @param count the number of tasks
	 */
	void fulfillTasks(int count);

	/**
	 * Increases the steps already done
	 */
	void nextStep();
}
