package de.prob.statespace

import java.nio.file.Paths

import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.ComputationNotCompletedResult
import de.prob.animator.domainobjects.EnumerationWarning
import de.prob.animator.domainobjects.EvalResult
import de.prob.animator.domainobjects.EventB
import de.prob.animator.domainobjects.FormulaExpand
import de.prob.animator.domainobjects.IdentifierNotInitialised
import de.prob.animator.domainobjects.WDError
import de.prob.cli.CliTestCommon
import de.prob.scripting.ClassicalBFactory
import de.prob.scripting.EventBFactory

import spock.lang.Specification

class TraceEvaluationTest extends Specification {
	private static StateSpace s
	private Trace t

	def setupSpec() {
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = CliTestCommon.injector.getInstance(ClassicalBFactory.class)
		s = factory.extract(path).load([:])
	}

	def cleanupSpec() {
		s.kill()
	}

	def setup() {
		t = new Trace(s)
	}

	def "when not initialised the result is IdentifierNotInitialised"() {
		when:
		final x = t.evalCurrent("waiting", FormulaExpand.EXPAND)

		then:
		x instanceof IdentifierNotInitialised
	}

	def "evaluating infinite sets results in enumeration warning"() {
		when:
		final x = t.evalCurrent("card({x|x : NATURAL & x mod 2 = 0})", FormulaExpand.EXPAND)

		then:
		x instanceof EnumerationWarning
	}

	def "evaluating formulas with well-definedness problems results in WDError"() {
		when:
		final x = t.evalCurrent("1 / 0", FormulaExpand.EXPAND)

		then:
		x instanceof WDError
	}

	def "evaluating formulas with type issues results in ComputationNotCompleted"() {
		when:
		final x = t.evalCurrent("1 + {}", FormulaExpand.EXPAND)

		then:
		x instanceof ComputationNotCompletedResult
	}

	private boolean isEmptySet(x) {
		return (x=="{}" || x=="\u2205") // u2205 is Unicode emptyset
	}

	def "It is possible to evaluate (correct) formulas in the current state (if initialised)"() {
		given:
		final t = t.$initialise_machine()

		when:
		final x = t.evalCurrent("x = waiting & y = card(x)", FormulaExpand.EXPAND)

		then:
		x instanceof EvalResult
		x.value == "TRUE"
		isEmptySet(x.solutions["x"])
		x.solutions["y"] == "0"
	}

	def "It is possible to evaluate a formula over the course of a Trace"() {
		given:
		final t = t.$initialise_machine().new("pp = PID1").new("pp = PID2")

		when:
		final x = t.evalAll("waiting")

		then:
		x.size() == 4
		x.keySet() as List == t.elements

		final values = x.values() as List
		values[0] instanceof IdentifierNotInitialised
		values[1].value == "{}" || values[1].value == "\u2205"
		values[2].value == "{PID1}"
		values[3].value == "{PID1,PID2}"
	}

	def "It is possible to evaluate a parsed formula over the course of a Trace"() {
		given:
		final t = t.$initialise_machine().new("pp = PID1").new("pp = PID2")

		when:
		final x = t.evalAll(new ClassicalB("waiting"))

		then:
		x.size() == 4
		x.keySet() as List == t.elements

		final values = x.values() as List
		values[0] instanceof IdentifierNotInitialised
		values[1].value == "{}" || values[1].value == "\u2205"
		values[2].value == "{PID1}"
		values[3].value == "{PID1,PID2}"
	}

	def "Evaluating a formula over a trace also works for EventB"() {
		given:
		final path = Paths.get("groovyTests", "Lift", "lift0.bcm").toString()
		final factory = CliTestCommon.injector.getInstance(EventBFactory.class)
		final s = factory.extract(path).load([:])
		t = new Trace(s)
		final t2 = t.$setup_constants().$initialise_machine().up()

		when:
		final x = t2.evalAll(new EventB("level"))

		then:
		x.size() == 4
		x.keySet() as List == t2.elements

		final values = x.values() as List
		values[0] instanceof IdentifierNotInitialised
		values[1] instanceof IdentifierNotInitialised
		values[2].value == "L0"
		values[3].value == "L1"

		cleanup:
		if (s != null) {
			s.kill()
		}
	}
}
