package de.prob.analysis.testcasegeneration;

public abstract class TestCaseGeneratorSettings {

    protected int maxDepth;

    public TestCaseGeneratorSettings(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
