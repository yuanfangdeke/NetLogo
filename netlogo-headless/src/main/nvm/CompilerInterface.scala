// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  api.{ ExtensionManager, Version },
  core.{ CompilationEnvironment, CompilerUtilitiesInterface, FrontEndInterface, Program }

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

trait CompilerInterface {
  import Procedure.ProceduresMap
  def frontEnd: FrontEndInterface
  def utilities: CompilerUtilitiesInterface
  def compileProgram(source: String, program: Program, extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment, flags: CompilerFlags = CompilerFlags()): CompilerResults
  def compileMoreCode(source: String, displayName: Option[String], program: Program,
    oldProcedures: ProceduresMap, extensionManager: ExtensionManager, compilationEnvironment: CompilationEnvironment,
    flags: CompilerFlags = CompilerFlags()): CompilerResults
  def makeLiteralReporter(value: AnyRef): Reporter
}

case class CompilerFlags(
  foldConstants: Boolean = true,
  useGenerator: Boolean = Version.useGenerator,
  useOptimizer: Boolean = Version.useOptimizer,
  optimizations: Seq[String] = Seq.empty[String])

case class CompilerResults(procedures: Seq[Procedure], program: Program) {
  import collection.immutable.ListMap
  def proceduresMap =
    ListMap(procedures.map(proc => (proc.name, proc)): _*)
  def head = procedures.head
}
