// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ AgentKind, CompilationEnvironment, CompilerException }
import org.nlogo.api.{ Agent => ApiAgent, AggregateManagerInterface, CommandRunnable,
  CompilerServices, HubNetInterface, ImporterUser, JobOwner, LogoException, OutputDestination,
  PreviewCommands, RandomServices, ReporterRunnable, Workspace => ApiWorkspace }

import org.nlogo.agent.{ Agent, AgentSet, World }

import collection.mutable.WeakHashMap

trait Workspace extends ApiWorkspace with JobManagerOwner {

  def world: World

  def compiler: CompilerInterface

  def procedures: ProcedureInterface.ProceduresMap
  def procedures_=(procedures: ProcedureInterface.ProceduresMap)

  def tick(c: Context, originalInstruction: Instruction)
  def resetTicks(c: Context)

  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String

  def behaviorSpaceExperimentName: String
  def behaviorSpaceExperimentName(name: String): Unit

  def getComponent[A <: AnyRef](componentClass: Class[A]): Option[A]

  /** lastRunTimes is used by `every` to track how long ago a job ran */
  def lastRunTimes: WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]
  /** completedActivations is used by `__thunk-did-finish` */
  def completedActivations: WeakHashMap[Activation, Boolean]

  /* compiler controls */
  @throws(classOf[CompilerException])
  def compileCommands(source: String): ProcedureInterface
  @throws(classOf[CompilerException])
  def compileCommands(source: String, agentKind: AgentKind): ProcedureInterface
  @throws(classOf[CompilerException])
  def compileForRun(source: String, context: Context, reporter: Boolean): ProcedureInterface
  @throws(classOf[CompilerException])
  def compileReporter(source: String): ProcedureInterface

  /* evaluation */
  def runCompiledCommands(owner: JobOwner, procedure: ProcedureInterface): Boolean
  def runCompiledReporter(owner: JobOwner, procedure: ProcedureInterface): AnyRef

  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, agent: Agent, waitForCompletion: Boolean): Unit
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, agents: AgentSet, waitForCompletion: Boolean): Unit

  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agent: Agent): AnyRef
  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet): AnyRef

  /* components */
  def fileManager: FileManager
  def profilingTracer: Tracer

  /* plots */
  def setupPlots(c: Context): Unit
  def updatePlots(c: Context): Unit

  /* job controls */
  def addJobFromJobThread(job: Job)
  def joinForeverButtons(agent: Agent)

  /* controls for things outside of nvm */
  def breathe(context: Context): Unit
  def requestDisplayUpdate(force: Boolean)
  def inspectAgent(agent: ApiAgent, radius: Double)
  def inspectAgent(agentKind: AgentKind, agent: Agent, radius: Double): Unit
  def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit
  def stopInspectingDeadAgents(): Unit
}

trait EditorWorkspace {
  def magicOpen(name: String): Unit

  @throws(classOf[java.io.IOException])
  def convertToNormal(): String
}

trait LoggingWorkspace {
  def startLogging(properties: String): Unit
  def zipLogFiles(filename: String): Unit
  def deleteLogFiles(): Unit
}
