// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  core.{ FrontEndProcedure, Let, StructureDeclarations, Syntax, Token },
  api.SourceOwner

import scala.collection.immutable.ListMap

object ProcedureInterface {
  type ProceduresMap = ListMap[String, ProcedureInterface]
  val NoProcedures = ListMap[String, ProcedureInterface]()
}

trait ProcedureInterface extends FrontEndProcedure {
  def isLambda: Boolean
  def pos: Int
  def pos_=(i: Int): Unit
  def end: Int
  def end_=(i: Int): Unit
  def size: Int
  def size_=(i: Int): Unit
  def localsCount: Int
  def localsCount_=(i: Int): Unit
  def code: Array[Command]
  def code_=(a: Array[Command]): Unit
  def addChild(p: ProcedureInterface): Unit
  def init(workspace: Workspace): Unit
  // these methods may not be necessary to include
  def owner: SourceOwner
  def owner_=(owner: SourceOwner): Unit
}

private[nvm] trait DefaultProcedureInterface extends ProcedureInterface {
  var pos: Int = 0
  var end: Int = 0
  var localsCount = 0
  var size = 0
  var code = Array[Command]()
  var owner: SourceOwner = null

  protected var children = collection.mutable.Buffer[ProcedureInterface]()

  def addChild(p: org.nlogo.nvm.ProcedureInterface): Unit = {
    children += p
  }

  override def syntax: Syntax = {
    val right = List.fill(args.size - localsCount)(Syntax.WildcardType)
    if (isReporter)
      Syntax.reporterSyntax(right = right, ret = Syntax.WildcardType)
    else
      Syntax.commandSyntax(right = right)
  }

  override def toString =
    super.toString + "[" + name + ":" + args.mkString("[", " ", "]") +
      ":" + agentClassString + "]"

  def init(workspace: org.nlogo.nvm.Workspace): Unit = {
    size = args.size
    code.foreach(_.init(workspace))
    children.foreach(_.init(workspace))
  }
}

trait LiftedLambdaInterface extends ProcedureInterface {
  def lambdaFormals: Seq[Let]
  def lambdaFormalsArray: Array[Let]
  def closedLets: Set[Let]
  def getLambdaFormal(name: String): Option[Let]
}
