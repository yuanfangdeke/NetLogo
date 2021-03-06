// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _turtleorlinkvariable(_varName: String) extends Reporter {

  override def toString =
    super.toString + ":" + varName

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep varName and _varName separate - ST 9/22/12
  def varName = _varName

  override def report(context: Context): AnyRef =
    report_1(context)

  def report_1(context: Context): AnyRef =
    try context.agent.getTurtleOrLinkVariable(_varName)
    catch { case ex: AgentException =>
      throw new RuntimePrimitiveException(context, this, ex.getMessage) }

}
