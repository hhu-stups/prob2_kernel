package de.prob.analysis.testcasegeneration;

public class TestCaseGeneratorMCDCSettings extends TestCaseGeneratorSettings {

	private final int level;

	public TestCaseGeneratorMCDCSettings(int maxDepth, int level) {
		super(maxDepth);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
}
