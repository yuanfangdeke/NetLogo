// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.Program
import scala.collection.immutable.ListMap

case class CompilerResults(procedures: Seq[ProcedureInterface], program: Program) {
  def this(proceduresMap: ListMap[String, ProcedureInterface], program: Program) =
    this(proceduresMap.values.toSeq, program)

  def proceduresMap: ListMap[String, ProcedureInterface] =
    ListMap(procedures.map(proc => (proc.name, proc)): _*)
  def head = procedures.head
}
