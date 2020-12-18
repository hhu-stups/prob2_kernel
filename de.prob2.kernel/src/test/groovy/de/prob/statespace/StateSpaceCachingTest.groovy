package de.prob.statespace

import java.nio.file.Paths

import com.google.common.util.concurrent.UncheckedExecutionException
import com.google.inject.Guice

import de.prob.MainModule
import de.prob.cli.CliTestCommon
import de.prob.scripting.ClassicalBFactory

import spock.lang.Specification

class StateSpaceCachingTest extends Specification {

	static StateSpace s

	def setupSpec() {
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = CliTestCommon.injector.getInstance(ClassicalBFactory.class)
		s = factory.extract(path).load([:])
	}

	def cleanupSpec() {
		s.kill()
	}

	def setup() {
		s.states.invalidateAll()
	}

	def "at the beginning, root is not in the state space"() {
		expect:
		s.states.getIfPresent("root") == null
	}

	def "after accessing root, it is present in cache"() {
		expect:
		s.root != null
		s.states.getIfPresent("root") != null
	}

	def "states that are discovered during exploration are automatically stored cache"() {
		when:
		s.root.explore()

		then:
		s.states.getIfPresent("0") != null
		s.getState("0") != null
	}

	def "but if the state is gone from cache for some reason, it can be reretrieved from prolog"() {
		when:
		s.root.explore()
		s.states.invalidate("0")

		then:
		s.states.getIfPresent("0") == null
		s.getState("0") != null
		s.states.getIfPresent("0") != null
	}

	def "if a state does not exist in the state space (on the prolog side) and illegal argument exception is thrown"() {
		when:
		s.getState("bum!")

		then:
		thrown(IllegalArgumentException)
	}

	def "states can also be accessed via integer values whereby root is -1"() {
		expect:
		s.getState(-1) == s.root
	}

	def "states can also be accessed via integer values if the states exist"() {
		when:
		s.root.explore()

		then:
		s.getState(0) == s.getState("0")
	}

	def "states cannot be accessed via negative integer except -1 (root)"() {
		when:
		s.getState(-100)

		then:
		thrown(IllegalArgumentException)
	}

	def "states can be accessed via integer via the getAt method"() {
		when:
		s.root.explore()

		then:
		s[0].id == "0"
		s[0] == s[0]
	}

	def "states that do not exist in the prolog kernel cannot be accessed via integer"() {
		when:
		s.getState(500) // we have not reached this during the exploration we have done so far in the tests

		then:
		thrown(IllegalArgumentException)
	}

	def "the cache is emptied when it gets too full"() {
		given:
		final mainModule = new MainModule()
		mainModule.maxCacheSize = 5
		final injector = Guice.createInjector(mainModule)
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = injector.getInstance(ClassicalBFactory.class)

		when:
		final s = factory.extract(path).load([:])
		Trace t = new Trace(s)
		final sizes = []
		for (i in 1..10) {
			t = t.anyEvent()
			sizes << s.states.size()
		}

		then:
		sizes.every {it <= 5}

		cleanup:
		if (s != null) {
			s.kill()
		}
	}

	def "trying to get a key from the LoadingCache that doesn't exist results in an exception"() {
		when:
		s.states.get("b")

		then:
		thrown(UncheckedExecutionException)
	}
}
