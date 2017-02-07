// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.ProcedureInterface;

// Note that _call is "CustomGenerated".  That means that the bytecode
// generator generates custom bytecode for _call, instead of using the
// perform() method below.  The body of the perform() method below
// needs to be maintained in tandem with CustomGenerator.generateCall
// (as well as _callreport.report and
// CustomGenerator.generateCallReport). - ST 5/18/10

public final strictfp class _call
    extends Command
    implements org.nlogo.nvm.CustomGenerated {
  public final ProcedureInterface procedure;

  public _call(ProcedureInterface procedure) {
    this.procedure = procedure;
  }

  @Override
  public int returnType() {
    return Syntax.VoidType();
  }

  @Override
  public String toString() {
    return super.toString() + ":" + procedure.name();
  }

  @Override
  public void perform(Context context) throws LogoException {
    Object[] callArgs = new Object[procedure.size()];
    for (int i = 0; i < (procedure.args().size() - procedure.localsCount()); i++) {
      callArgs[i] = args[i].report(context);
    }
    context.activation = new Activation(procedure, context.activation, callArgs, next);
    context.ip = 0;
  }
}
