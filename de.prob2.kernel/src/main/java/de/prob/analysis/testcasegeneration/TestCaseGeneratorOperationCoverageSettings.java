package de.prob.analysis.testcasegeneration;

import java.util.List;

public class TestCaseGeneratorOperationCoverageSettings extends TestCaseGeneratorSettings {

    private List<String> operations;

    public TestCaseGeneratorOperationCoverageSettings(int maxDepth, List<String> operations) {
        super(maxDepth);
        this.operations = operations;
    }

    public List<String> getOperations() {
        return operations;
    }
}
