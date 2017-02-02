package org.nlogo.prim;

import org.nlogo.core.{Syntax, Token}
import org.nlogo.nvm.{Context, Reporter}
import org.nlogo.core.Pure
import scala.collection.JavaConverters._

class _constcodeblock(value: List[Token]) extends Reporter with Pure {
  def this(value: Seq[Token]) = this(value.toList)

  override def toString =
    s"${super.toString}: [${value.map((t: Token) => t.text).mkString(" ")}]"

  override def report(ctx: Context) = value
}
