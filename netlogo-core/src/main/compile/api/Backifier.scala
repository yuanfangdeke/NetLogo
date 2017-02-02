// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import org.nlogo.{ core, nvm }

import scala.collection.immutable.ListMap

trait Backifier {
  def apply(procedures: ListMap[String, nvm.ProcedureInterface], c: core.Command): nvm.Command
  def apply(procedures: ListMap[String, nvm.ProcedureInterface], r: core.Reporter): nvm.Reporter
}
