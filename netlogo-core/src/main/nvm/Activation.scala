// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ Activation => ApiActivation }

object Activation {
  private val NoArgs = Array[AnyRef]()
  def args(size: Int): Array[AnyRef] = {
    if (size > 0)
      new Array[AnyRef](size)
    else
      NoArgs
  }


  def forRunOrRunresult(procedure: ProcedureInterface, parent: Activation, returnAddress: Int): Activation = {
    // When the procedure is compiled from a string, it will have 0 arguments, which
    // means references to the parent procedures arguments will error.
    // We have to copy so we don't mess with the parent activation's locals. - RG 1/13/17
    val args = new Array[AnyRef](procedure.size max parent.args.length)
    System.arraycopy(parent.args, 0, args, 0, parent.procedure.args.size)
    new Activation(procedure, parent, args, returnAddress)
  }
}

final class Activation(
  val procedure: ProcedureInterface,
  _parent: Activation,
  private[nlogo] val args: Array[AnyRef],
  val returnAddress: Int,
  var binding: Binding) extends ApiActivation {

  def this(procedure: ProcedureInterface, parent: Activation, args: Array[AnyRef], returnAddress: Int) = {
    this(procedure, parent, args, returnAddress, new Binding())
  }

  def this(procedure: ProcedureInterface, parent: Activation, returnAddress: Int) = {
    this(procedure, parent, Activation.args(procedure.size), returnAddress, new Binding())
  }

  def this(procedure: ProcedureInterface, parentOpt: Option[Activation], args: Array[AnyRef], returnAddress: Int, binding: Binding) = {
    this(procedure, parentOpt.orNull, args, returnAddress, binding)
  }

  def parent: Option[Activation] =
    Option(_parent)

  private[nvm] def parentOrNull: Activation =
    _parent

  def setUpArgsForRunOrRunresult() {
    // if there's a reason we copy instead of using the original, I don't remember it - ST 2/6/11
    System.arraycopy(_parent.args, 0, args, 0, _parent.procedure.args.size)
  }

  override def toString =
    super.toString + ":" + procedure.name + "(" + args.size + " args" +
      ", return address = " + returnAddress + ")\n" +
      args.zipWithIndex
        .map{case (a, i) => "  arg " + i + " = " + a}
        .mkString("\n")

  def nonLambdaActivation: Activation = {
    if (procedure.isLambda)
      parent.map(_.nonLambdaActivation).getOrElse(this)
    else
      this
  }
}
