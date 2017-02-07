// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Procedure;

public final strictfp class _stop
    extends Command {


  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    perform_1(context);
  }

  public void perform_1(final org.nlogo.nvm.Context context) throws LogoException {
    // check: are we in an ask?
    if (!context.atTopActivation()) {
      // if so, then "stop" means that this agent prematurely
      // finishes its participation in the ask.
      context.finished = true;
    } else if (context.activation.nonLambdaActivation().procedure().isReporter()) {
      // if we're not in an ask, then "stop" means to exit this procedure
      // immediately.  first we must check that it is (or is being called by)
      // a command procedure and not a reporter procedure.
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", displayName()));
    }
    context.stop();
  }

  // identical to perform_1() above... BUT with a profiling hook added
  public void profiling_perform_1(final org.nlogo.nvm.Context context) throws LogoException {
    if (!context.atTopActivation()) {
      context.finished = true;
    } else {
      if (context.activation.nonLambdaActivation().procedure().isReporter())
        throw new RuntimePrimitiveException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", displayName()));
    }
    workspace.profilingTracer().closeCallRecord(context, context.activation);
    context.stop();
  }
}
