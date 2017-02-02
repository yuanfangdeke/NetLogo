// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  core.{ FrontEndProcedure, Let, StructureDeclarations, Token },
  api.SourceOwner

import scala.collection.immutable.ListMap

class Procedure(
  val isReporter:           Boolean,
  val name:                 String,
  val nameToken:            Token,
  val argTokens:            Seq[Token],
  val procedureDeclaration: StructureDeclarations.Procedure,
  baseDisplayName:          Option[String] = None) extends DefaultProcedureInterface {

    args = argTokens.map(_.text).toVector

    def this(p: FrontEndProcedure) =
      this(
        p.isReporter, p.name, p.nameToken, p.argTokens, p.procedureDeclaration,
        if (p.displayName == "") None else Some(p.displayName))

    val filename = nameToken.filename
    val isLambda = false

    lazy val displayName = buildDisplayName(baseDisplayName)

    protected def buildDisplayName(displayName: Option[String]): String = {
      val nameAndFile =
        Option(filename)
          .filter(_.nonEmpty)
          .map(name + " (" + _ + ")")
          .getOrElse(name)

      displayName.getOrElse("procedure " + nameAndFile)
    }

    def dump: String = {
      val buf = new StringBuilder
      if (isReporter)
        buf ++= "reporter "
      buf ++= displayName
      buf ++= ":"
      buf ++= args.mkString("[", " ", "]")
      buf ++= "{" + agentClassString + "}:\n"
      for (i <- code.indices) {
        val command = code(i)
        buf ++= "[" + i + "]"
        buf ++= command.dump(3)
        buf ++= "\n"
      }
      for (p <- children) {
        buf ++= "\n"
        buf ++= p.dump
      }
      buf.toString
    }
}
