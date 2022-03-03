package de.prob.cli

import spock.lang.Specification

class ConsoleListenerTest extends Specification {
	def "multiple lines are read and logged"() {
		given:
		final ProBInstance proBInstance = Mock()
		final reader = new BufferedReader(new StringReader("foo\nbar"))
		def lines = []
		final listener = new ConsoleListener(proBInstance, reader, {lines << it})

		when:
		final line1 = listener.readAndLog()
		final line2 = listener.readAndLog()

		then:
		line1 == "foo"
		line2 == "bar"
		lines == [line1, line2]

		cleanup:
		reader.close()
	}

	def "no more lines are logged once the ProBInstance is shutting down"() {
		given:
		final ProBInstance proBInstance = Mock() {
			isShuttingDown() >>> [false, false, true]
		}
		final reader = new BufferedReader(new StringReader("foo\nbar\ngoo"))
		def lines = []
		final listener = new ConsoleListener(proBInstance, reader, {lines << it})

		when:
		listener.logLines()

		then:
		lines == ["foo", "bar"]

		cleanup:
		reader.close()
	}

	def "a single line is read and logged"() {
		given:
		final ProBInstance proBInstance = Mock()
		final reader = new BufferedReader(new StringReader("foo"))
		def lines = []
		final listener = new ConsoleListener(proBInstance, reader, {lines << it})

		when:
		final line = listener.readAndLog()

		then:
		line == "foo"
		lines == [line]

		cleanup:
		reader.close()
	}

	def "nothing is logged if the ProBInstance is shutting down"() {
		given:
		final ProBInstance proBInstance = Mock() {
			isShuttingDown() >> true
		}
		final reader = new BufferedReader(new StringReader("foo"))
		def lines = []
		final listener = new ConsoleListener(proBInstance, reader, {lines << it})

		when:
		listener.logLines()

		then:
		lines == []

		cleanup:
		reader.close()
	}

	def "reading when the ProBInstance is shutting down returns null and logs nothing"() {
		given:
		final ProBInstance proBInstance = Mock() {
			isShuttingDown() >> true
		}
		final reader = new BufferedReader(new StringReader(""))
		def lines = []
		final listener = new ConsoleListener(proBInstance, reader, {lines << it})

		when:
		final line = listener.readAndLog()

		then:
		line == null
		lines == []

		cleanup:
		reader.close()
	}
}
