import java.nio.file.Paths

import de.prob.animator.domainobjects.BVisual2Formula
import de.prob.animator.domainobjects.BVisual2Value
import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.ExpandedFormula
import de.prob.statespace.Trace

import static de.prob.unicode.UnicodeTranslator.toUnicode

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())
def t = new Trace(s)
t = t.$initialise_machine()

final f = "(ready /\\ waiting) = {} & card(active) <= 1" as ClassicalB
final inserted = BVisual2Formula.fromFormula(s, f)
assert inserted != null

final formula = inserted.expand(t.currentState)
assert formula instanceof ExpandedFormula
assert formula.label == "ready \u2229 waiting = \u2205 \u2227 card(active) \u2264 1"
assert formula.value == BVisual2Value.PredicateValue.TRUE
assert formula.children.size() == 2

final f1 = formula.children[0]
assert f1.label == "ready \u2229 waiting = \u2205"
assert f1.value == BVisual2Value.PredicateValue.TRUE
assert f1.children.size() == 1

final f11 = f1.children[0]
assert f11.label == 'ready \u2229 waiting'
assert (f11.value as BVisual2Value.ExpressionValue).value == toUnicode("{}")
assert f11.children.size() == 2

final f111 = f11.children[0]
assert f111.label == "ready"
assert (f111.value as BVisual2Value.ExpressionValue).value == toUnicode("{}")
assert f111.children.empty

final f112 = f11.children[1]
assert f112.label == "waiting"
assert (f112.value as BVisual2Value.ExpressionValue).value == toUnicode("{}")
assert f112.children.empty

final f2 = formula.children[1]
assert f2.label == 'card(active) \u2264 1'
assert f2.value == BVisual2Value.PredicateValue.TRUE
assert f2.children.size() == 1

final f21 = f2.children[0]
assert f21.label == 'card(active)'
assert (f21.value as BVisual2Value.ExpressionValue).value == '0'
assert f21.children.size() == 1

final f211 = f21.children[0]
assert f211.label == 'active'
assert (f211.value as BVisual2Value.ExpressionValue).value == toUnicode('{}')
assert f211.children.empty

"expanding a B formula works"
