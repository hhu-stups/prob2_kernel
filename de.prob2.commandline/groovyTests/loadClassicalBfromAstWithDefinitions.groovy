import de.be4.classicalb.core.parser.BParser

final modelString = """MACHINE TestMch
    DEFINITIONS
        TestDef == TRUE
    PROPERTIES
        TestDef = TRUE
END"""

api.b_load(BParser.parse(modelString))

"load constructed B machine from AST with definitions"
