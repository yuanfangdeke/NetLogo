// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected in
// StructureParser. - ST 2/21/08, 1/21/09

import org.nlogo.api.{ ExtensionManager, Version }
import org.nlogo.compile.api.{ Backifier => BackifierInterface, CommandMunger, DefaultAstVisitor,
  FrontMiddleBridgeInterface, MiddleEndInterface, Optimizations, ProcedureDefinition, ReporterMunger }
import org.nlogo.core.{ Dialect, Program }
import org.nlogo.nvm.{ CompilerFlags, GeneratorInterface, ProcedureInterface }
import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, FrontEndInterface, FrontEndProcedure, Femto }
import scala.collection.immutable.ListMap
import scala.collection.JavaConversions._

private object CompilerMain {

  val bridge = Femto.scalaSingleton[FrontMiddleBridgeInterface](
    "org.nlogo.compile.middle.FrontMiddleBridge")
  val middleEnd = Femto.scalaSingleton[MiddleEndInterface](
    "org.nlogo.compile.middle.MiddleEnd")

  def backifier(program: Program, extensionManager: ExtensionManager): BackifierInterface =
    new Backifier(program, extensionManager)

  private val frontEnd =
    Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")

  def compile(
    sources:          Map[String, String],
    displayName:      Option[String],
    program:          Program,
    subprogram:       Boolean,
    oldProcedures:    ListMap[String, ProcedureInterface],
    extensionManager: ExtensionManager,
    compilationEnv:   CompilationEnvironment): (ListMap[String, ProcedureInterface], Program) = {

    val oldProceduresListMap = ListMap[String, ProcedureInterface](oldProcedures.toSeq: _*)
    val (topLevelDefs, feStructureResults) =
      frontEnd.frontEnd(CompilationOperand(sources, extensionManager, compilationEnv, program, oldProceduresListMap, subprogram, displayName))

    val bridged = bridge(feStructureResults, oldProcedures, topLevelDefs, backifier(feStructureResults.program, extensionManager))

    val allSources = sources ++
      feStructureResults.includedSources.map(i =>
          (i -> compilationEnv.getSource(compilationEnv.resolvePath(i))))

    val allDefs = middleEnd.middleEnd(bridged, sources, compilationEnv, getOptimizations(CompilerFlags(), feStructureResults.program.dialect))

    val newProcedures =
      allDefs
        .map(assembleProcedure(_, feStructureResults.program, compilationEnv))
        .filterNot(_.isLambda)
        .map(p => p.name -> p)

    val returnedProcedures = ListMap(newProcedures: _*) ++ oldProcedures
    // only return top level procedures.
    // anonymous procedures can be reached via the children field on Procedure.
    (returnedProcedures, feStructureResults.program)
  }

  // These phases optimize and tweak the ProcedureDefinitions given by frontEnd/CompilerBridge. - RG 10/29/15
  // SimpleOfVisitor performs an optimization, but also sets up for SetVisitor - ST 2/21/08
  def assembleProcedure(procdef: ProcedureDefinition, program: Program, compilationEnv: CompilationEnvironment): ProcedureInterface = {
    val optimizers: Seq[DefaultAstVisitor] = Seq(
      new ConstantFolder, // en.wikipedia.org/wiki/Constant_folding
      new ArgumentStuffer // fill args arrays in Commands & Reporters
    )
    for (optimizer <- optimizers) {
      procdef.accept(optimizer)
    }
    new Assembler().assemble(procdef) // flatten tree to command array
    if(Version.useGenerator) // generate byte code
      procdef.procedure.code =
        Femto.get[GeneratorInterface]("org.nlogo.generate.Generator", procdef.procedure,
          Boolean.box(compilationEnv.profilingEnabled)).generate()

    procdef.procedure
  }

  private def getOptimizations(flags: CompilerFlags, dialect: Dialect): Optimizations =
    if (flags.useOptimizer) {
      val commandOpts =
        Seq("Fd1", "FdLessThan1", "HatchFast", "CroFast", "CrtFast", "SproutFast")
          .map(opt => s"org.nlogo.compile.middle.optimize.$opt")
          .map(className => Femto.scalaSingleton[CommandMunger](className))
      val reporterOpts =
        Seq("PatchAt", "With", "OneOfWith", "Nsum", "Nsum4", "CountWith", "OtherWith",
          "WithOther", "AnyOther", "AnyOtherWith", "CountOther", "CountOtherWith", "AnyWith1",
          "AnyWith2", "AnyWith3", "AnyWith4", "AnyWith5", "RandomConst")
          .map(opt => s"org.nlogo.compile.middle.optimize.$opt")
          .map(className => Femto.scalaSingleton[ReporterMunger](className))
      val netLogoSpecificOptimizations =
        Seq("DialectPatchVariableDouble", "DialectTurtleVariableDouble")
          .map(opt => s"org.nlogo.compile.optimize.$opt")
          .map(className => Femto.get[ReporterMunger](className, dialect))
      Optimizations(commandOpts, reporterOpts ++ netLogoSpecificOptimizations)
    } else
      Optimizations.none
}
