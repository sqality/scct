package com.sqality.scct.report

import collection.immutable.{ SortedMap, TreeMap }
import com.sqality.scct.{ CoveredBlock, Name }
import java.math.{ MathContext, RoundingMode }

class CoverageData(val blocks: List[CoveredBlock]) {

  final val RATE_MATH_CONTEXT = new MathContext(2, RoundingMode.DOWN)

  private def forSourceFile(sourceFile: String) =
    new CoverageData(blocks.filter(_.name.sourceFile == sourceFile).sortWith(_.offset < _.offset))

  def forSourceFiles: Map[String, CoverageData] = {
    val names = blocks.map(_.name.sourceFile).distinct
    names.foldLeft(stringMap) { (map, n) => map + (n -> forSourceFile(n)) }
  }

  private def forClass(name: Name) =
    new CoverageData(blocks.filter(_.name == name))

  def forClasses: Map[Name, CoverageData] = {
    val names = blocks.map(_.name).distinct
    names.foldLeft(nameMap) { (map, n) => map + (n -> forClass(n)) }
  }

  private def forPackage(packageName: String) =
    new CoverageData(blocks.filter(_.name.packageName == packageName))

  def forPackages: Map[String, CoverageData] = {
    val names = blocks.map(_.name.packageName).distinct
    names.foldLeft(stringMap) { (map, n) => map + (n -> forPackage(n)) }
  }

  private def forProject(projectName: String) =
    new CoverageData(blocks.filter(_.name.projectName == projectName))

  def forProjects: Map[String, CoverageData] = {
    val names = blocks.map(_.name.projectName).distinct
    names.foldLeft(stringMap) { (map, n) => map + (n -> forProject(n)) }
  }

  private def nameMap: SortedMap[Name, CoverageData] = new TreeMap[Name, CoverageData]()
  private def stringMap: SortedMap[String, CoverageData] = new TreeMap[String, CoverageData]()

  lazy val percentage: Option[Int] = rate map { it => (it * 100).toInt }

  lazy val nonPlaceHolderBlocks: List[CoveredBlock] = blocks.filter(!_.placeHolder)

  private lazy val nonPlaceHolderBlocksSize = nonPlaceHolderBlocks.length

  private lazy val coveredBlocksSize = nonPlaceHolderBlocks.foldLeft(0) { (sum, b) => if (b.count > 0) sum + 1 else sum }

  lazy val ratioDetail = (coveredBlocksSize, nonPlaceHolderBlocksSize)

  lazy val rate: Option[BigDecimal] = {
    nonPlaceHolderBlocks match {
      case List() => None
      case list => {
        val sum = BigDecimal(coveredBlocksSize, RATE_MATH_CONTEXT)
        Some(sum / list.size)
      }
    }
  }
}
