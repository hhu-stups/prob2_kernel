import java.nio.file.Paths

import de.prob.animator.command.EvaluateRegisteredFormulasCommand
import de.prob.animator.command.RegisterFormulasCommand
import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.EvaluationErrorResult
import de.prob.statespace.Trace

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString())

def t = s as Trace
t = t.$initialise_machine()
t = t.new("pp=PID1")
final f = "1+1" as ClassicalB
final f2 = "active" as ClassicalB
final cmd1 = new RegisterFormulasCommand([f, f2])
s.execute(cmd1)
final cmd2 = new EvaluateRegisteredFormulasCommand("3", [f, f2])
s.execute(cmd2)
assert cmd2.results[f].value == "2"
res = cmd2.results[f2].value
assert res == "{}" || res == "\u2205"
final cmd3 = new EvaluateRegisteredFormulasCommand("root", [f, f2])
s.execute(cmd3)
assert cmd3.results[f].value == "2"
assert cmd3.results[f2] instanceof EvaluationErrorResult

"It is possible to register formulas and asynchronously evaluate them later"
