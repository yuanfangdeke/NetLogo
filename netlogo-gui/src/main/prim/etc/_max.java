// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _max extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    LogoList list = argEvalList(context, 0);
    double winner = 0;
    Double boxedWinner = null;
    for (Object elt : list.javaIterable()) {
      if (elt instanceof Double) {
        Double boxedValue = (Double) elt;
        double value = boxedValue.doubleValue();
        if (boxedWinner == null || value > winner) {
          winner = value;
          boxedWinner = boxedValue;
        }
      }
    }
    if (boxedWinner == null) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim._max.cantFindMaxOfListWithNoNumbers", Dump.logoObject(list)));
    }
    return boxedWinner;
  }
}
