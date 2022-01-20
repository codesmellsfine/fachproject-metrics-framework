package impl.group1

import java.io.File

import org.scalatest.{FlatSpec, Matchers}
import org.tud.sse.metrics.ApplicationConfiguration
import org.tud.sse.metrics.analysis.{MetricValue, MetricsResult}
import org.tud.sse.metrics.testutils.AnalysisTestUtils


class EvolutionAnalysisTest extends FlatSpec with Matchers{

  val dirWithTestFiles = new File(getClass.getResource("/group1/commons-collections").getPath)
  val evolutionAnalysisToTest = new EvolutionAnalysis(dirWithTestFiles)

  val appConfig = new ApplicationConfiguration(inputFilePath = "", treatFilesAsLibrary = true, outFileOption = None,
    opalLoggingEnabled = false, batchModeEnabled = Some(false), excludedAnalysesNames = List(), includedAnalysesNames = List(),
    excludeJreClasses = true, additionalClassesDir = None, loadAdditionalClassesAsInterface = false)

  val optionsMap : Map[Symbol,Any]  = Map(Symbol("ext_evo") -> "ext_evo", Symbol("int_evo")-> "int_evo")

  val results:List[MetricsResult]  = AnalysisTestUtils.runMultiFileAnalysis(_ =>
    evolutionAnalysisToTest,dirWithTestFiles,appConfig,optionsMap)


  assert(results.size == 1)

  val evolutionResult:MetricsResult = results.head
  assert(evolutionResult.success)
  assert(evolutionResult.metricValues.size == 36)



  // so kann man gezielt einzelne Values testen.
  for(metricValueToTest <- results.head.metricValues){
    if(metricValueToTest.metricName == "Evolution"){
      if(metricValueToTest.entityIdent.contains("00-commons-collections-1.0.jar") &&
        metricValueToTest.entityIdent.contains("01-commons-collections-2.0.jar")){
        assert(metricValueToTest.metricValue == 0.0)
      } else if(metricValueToTest.entityIdent.contains("02-commons-collections-2.0.20020914.015953.jar") &&
        metricValueToTest.entityIdent.contains("03-commons-collections-2.0.20020914.020746")){
        assert(metricValueToTest.metricValue == 0.200709219858156)
      }
    }else if(metricValueToTest.metricName == "Internal Evolution"){
      if(metricValueToTest.entityIdent.contains("02-commons-collections-2.0.20020914.015953.jar") &&
        metricValueToTest.entityIdent.contains("03-commons-collections-2.0.20020914.020746")){
        assert(metricValueToTest.metricValue == 0.3333333333333333)
      }
    } else{
      if(metricValueToTest.entityIdent.contains("02-commons-collections-2.0.20020914.015953.jar") &&
        metricValueToTest.entityIdent.contains("03-commons-collections-2.0.20020914.020746")){
        assert(metricValueToTest.metricValue == 0.06808510638297872)
      }
    }
  }

  // results hat size 1 daher wird die forschleife nur einmal durchlaufen.
  // der head enthält nur das dif vom letzten Test. Obere for schleife für richtige Tests.
  /*
  for(metricResult <- results){

    if(diffOf0001(metricResult))
    {
      assert(metricResult == 0.0)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    } else if(diffOf0102(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.033783783783783786)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.06756756756756757)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    } else if(diffOf0203(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.200709219858156)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.06808510638297872)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.3333333333333333)
    } else if(diffOf0304(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    } else if(diffOf0405(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    } else if(diffOf0506(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    } else if(diffOf0607(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 1.9603729603729605)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.5874125874125874)
      assert(metricResult.metricValues.toSeq(2).metricValue == 3.3333333333333335)
    } else if(diffOf0708(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    } else if(diffOf0809(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    } else if(diffOf0910(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    } else if(diffOf1011(metricResult)){
      assert(metricResult.metricValues.toSeq.head.metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(1).metricValue == 0.0)
      assert(metricResult.metricValues.toSeq(2).metricValue == 0.0)
    }
  }
*/
  results.foreach(item => println(item.metricValues))

  def diffOf0001B(result: MetricValue): Boolean = {
    result.entityIdent.contains("00-commons-collections-1.0.jar") &&
      result.entityIdent.contains("01-commons-collections-2.0.jar")
  }
  def diffOf0001(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("00-commons-collections-1.0.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("01-commons-collections-2.0.jar")
  }

  def diffOf0102(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("01-commons-collections-2.0.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("02-commons-collections-2.0.20020914.015953.jar")
  }

  def diffOf0203(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("02-commons-collections-2.0.20020914.015953.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("03-commons-collections-2.0.20020914.020746")
  }

  def diffOf0304(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("03-commons-collections-2.0.20020914.020746.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("04-commons-collections-2.0.20020914.020858.jar")
  }

  def diffOf0405(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("04-commons-collections-2.0.20020914.020858.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("05-commons-collections-2.1.jar")
  }

  def diffOf0506(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("05-commons-collections-2.1.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("06-commons-collections-2.1.1.jar")
  }

  def diffOf0607(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("06-commons-collections-2.1.1.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("07-commons-collections-2.0.jar")
  }

  def diffOf0708(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("07-commons-collections-3.0.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("08-commons-collections-3.1.jar")
  }

  def diffOf0809(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("08-commons-collections-3.1.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("09-commons-collections-3.2.jar")
  }

  def diffOf0910(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("09-commons-collections-3.2.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("10-commons-collections-3.2.1.jar")
  }

  def diffOf1011(result: MetricsResult): Boolean = {
    result.metricValues.toSeq.head.entityIdent.contains("10-commons-collections-3.2.1.jar") &&
      result.metricValues.toSeq.head.entityIdent.contains("11-commons-collections-3.2.2.jar")
  }

}
