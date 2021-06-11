package de.prob.check.tracereplay.check.eventB.abz;

import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.*;
import de.prob.check.tracereplay.check.exploration.PersistenceDelta;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.check.tracereplay.check.renamig.DeltaCalculationException;
import de.prob.check.tracereplay.check.renamig.RenamingDelta;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.model.eventb.Event;
import de.prob.model.eventb.EventBModel;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.Machine;
import de.prob.model.representation.ModelElementList;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class SimpleEventBMachine {


}
