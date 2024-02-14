package de.prob.statespace

import java.nio.file.Paths

import de.prob.animator.domainobjects.AbstractEvalResult
import de.prob.animator.domainobjects.CSP
import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.IEvalElement
import de.prob.animator.domainobjects.IdentifierNotInitialised
import de.prob.cli.CliTestCommon
import de.prob.model.representation.CSPModel
import de.prob.scripting.ClassicalBFactory

import spock.lang.Specification

class StateSpaceEvaluationTest extends Specification {
	private static StateSpace s
	private static State root
	private static State firstState

	def setupSpec() {
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = CliTestCommon.injector.getInstance(ClassicalBFactory.class)
		s = factory.extract(path).load([:])
		root = s.root
		firstState = root.$initialise_machine()
	}

	def cleanupSpec() {
		s.kill()
	}

	private boolean isEmptySet(x) {
		return (x=="{}" || x=="\u2205") // u2205 is Unicode emptyset
	}

	def "it is possible to evaluate formulas in a state"() {
		final res = firstState.eval([
			new ClassicalB("waiting"),
			new ClassicalB("ready")
		]).collect { it.getValue() }
		expect:
		res == ["{}", "{}"] || res == ["\u2205", "\u2205"]
	}
	def "it is possible for someone to subscribe to a formula"() {
		when:
		def formula = new ClassicalB("waiting /\\ ready")
		boolean before = s.isSubscribed(formula)
		def subscriber = "I am a subscriber!"
		def success = s.subscribe(subscriber, formula)
		then:
		!before
		success
		s.isSubscribed(formula)
	}
	def "it is possible for multiple people to subscribe to the same formula"() {
		when:
		def formula = new ClassicalB("waiting \\/ ready")
		boolean before = s.isSubscribed(formula)
		def subscriber1 = "I am a subscriber!"
		def subscriber2 = "I am also a subscriber!"
		def success = s.subscribe(subscriber1, formula)
		def success2 = s.subscribe(subscriber2, formula)
		then:
		!before // it didn't have it before
		success
		success2
		s.isSubscribed(formula)
	}

	def "csp formulas cannot be subscribed"() {
		when:
		CSPModel m = CliTestCommon.injector.getInstance(CSPModel.class)
		m = m.create("some content", new File("somedir"+File.separator+"someotherdir"+File.separator+"myfile.csp"))
		CSP csp = new CSP("some formula", m)
		def subscriber1 = "subscriber1"
		def success = s.subscribe(subscriber1, csp)
		then:
		!success
		!s.isSubscribed(csp)
	}

	def "it is possible for someone to subscribe to multiple formulas"() {
		when:
		def formula = new ClassicalB("card(waiting)")
		def formula2 = new ClassicalB("card(ready)")
		boolean before = s.isSubscribed(formula)
		boolean before2 = s.isSubscribed(formula2)
		def subscriber = "I am a subscriber!"
		def success = s.subscribe(subscriber, [formula, formula2])
		then:
		!before
		!before2
		success
		s.isSubscribed(formula)
		s.isSubscribed(formula2)
	}
	def "it is possible for multiple people to subscribe to the same multiple formulas"() {
		when:
		def formula = new ClassicalB("card(ready)+card(waiting)")
		def formula2 = new ClassicalB("card(active)")
		boolean before = s.isSubscribed(formula)
		boolean before2 = s.isSubscribed(formula2)
		def subscriber1 = "I am a subscriber!"
		def subscriber2 = "I am also a subscriber!"
		def success = s.subscribe(subscriber1, [formula, formula2])
		def success2 = s.subscribe(subscriber2, [formula, formula2])
		then:
		!before
		!before2
		success
		success2
		s.isSubscribed(formula)
		s.isSubscribed(formula2)
	}

	def "multiple csp formulas cannot be subscribed"() {
		when:
		true
		CSPModel m = CliTestCommon.injector.getInstance(CSPModel.class)
		m = m.create("some content", new File("somedir"+File.separator+"someotherdir"+File.separator+"myfile.csp"))
		CSP csp = new CSP("some formula", m)
		CSP csp2 = new CSP("some formula2", m)
		def subscriber1 = "subscriber1"
		def success = s.subscribe(subscriber1, [csp, csp2])
		then:
		!success
		!s.isSubscribed(csp)
		!s.isSubscribed(csp2)
	}

	def "formulas should not be evaluated in the root state"() {
		expect: !root.initialised
	}

	def "after subscribing a formula, its values can be retrieved using valuesAt"() {
		when:
		def formula = new ClassicalB("card(waiting) + 1")
		s.subscribe("mmm",formula)
		firstState.explore()
		def values = firstState.values
		then:
		values.containsKey(formula)
		values[formula].getValue() == "1"
	}

	class DummyObject {
		def field = "I don't do much!"
	}

	def "the garbage collector should automatically remove subscribers if their references no longer exist"() {
		when:
		def subscriber = new DummyObject()
		def formula = new ClassicalB('card(ready) + 1')
		def success = s.subscribe(subscriber, formula)
		def before = s.isSubscribed(formula)
		subscriber = null
		System.gc()
		then:
		success
		before
		!s.isSubscribed(formula)
	}

	def "a formula that has not yet been subscribed should be recognized as subscribed"() {
		expect: !s.isSubscribed(new ClassicalB("card(waiting)+10"))
	}

	def "it is possible to unsubscribe a formula after subscribing it"() {
		when:
		def subscriber = "I'm a subscriber!"
		def formula = new ClassicalB("card(waiting) + 5")
		def success = s.subscribe(subscriber, formula)
		def before = s.isSubscribed(formula)
		def success2 = s.unsubscribe(subscriber, formula)
		then:
		success
		success2
		!s.isSubscribed(formula)
	}

	def "it is possible for someone to unsubscribe even if someone else is still subscribed"() {
		when:
		def subscriber = "hi!"
		def subscriber2 = "hi again!"
		def formula = new ClassicalB("card(ready) + card(active) + 7")
		def success = s.subscribe(subscriber, formula)
		def success2 = s.subscribe(subscriber2, formula)
		then:
		success
		success2
		s.unsubscribe(subscriber, formula)
	}

	def "it is not possible to unsubscribe a formula that is not subscribed (nothing will happen)"() {
		expect:
		!s.unsubscribe("I'm not a subscriber", new ClassicalB("1+24"))
	}

	def "getting subscribed formulas also removes formulas that aren't there any more"() {
		when:
		def subscriber = "hi!"
		def formula = new ClassicalB("card(active) + 9")
		def success = s.subscribe(subscriber, formula)
		def before = s.getSubscribedFormulas().contains(formula)
		s.unsubscribe(subscriber, formula)
		then:
		success
		before
		!s.getSubscribedFormulas().contains(formula)
	}

	def "it is possible to evaluate multiple formulas in multiple states"() {
		when:
		def waiting = new ClassicalB("waiting")
		def ready = new ClassicalB("ready")
		def active = new ClassicalB("active")
		def state2 = firstState.new("pp=PID1")
		def state3 = firstState.new("pp=PID2")
		def state4 = firstState.new("pp=PID3")
		def states = [
			root,
			firstState,
			state2,
			state3,
			state4
		]
		s.subscribe("I'm a subscriber!",[ready])
		Map<State, Map<IEvalElement, AbstractEvalResult>> result = s.evaluateForGivenStates(states, [waiting, ready, active])
		then:
		result[root].values().every {it instanceof IdentifierNotInitialised}
		def statesWOroot = states.findAll { it != root}
		statesWOroot.collect { result[it][ready].getValue() }.every {isEmptySet(it)}
		statesWOroot.collect { result[it][active].getValue() }.every {isEmptySet(it)}
		isEmptySet(result[firstState][waiting].getValue())
		result[state2][waiting].getValue() == "{PID1}"
		result[state3][waiting].getValue() == "{PID2}"
		result[state4][waiting].getValue() == "{PID3}"
	}
}
