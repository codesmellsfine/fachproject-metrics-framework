package org.tud.sse.metrics
package impl

import java.io.File
import java.net.URL

import org.opalj.br.analyses.Project
import org.tud.sse.metrics.analysis.{MetricValue, MetricsResult, MultiFileAnalysis}
import org.tud.sse.metrics.input.CliParser.OptionMap

import scala.util.Try

class DITMFMAnalysis(jarDir: File) extends MultiFileAnalysis[(Double,String)](jarDir:File){

  var previousFile: String = ""
  var currentFile: String = ""
  var initialRound: Boolean = true
  var roundCounter: Integer = 0

  var preVersionAverageDIT: Double = 0
  var difDITBetweenVersions: Double = 0
  /**
   * This method is called to execute the analysis for each JAR file individually.
   * It calculates the intermediate results of type Try[T], which will be stored in the
   * analysisResultsPerFile map automatically by the enclosing analyzeNext call.
   *
   * @param project       Fully initialized OPAL project representing the JAR file under analysis
   * @param lastResult    Option that contains the intermediate result for the previous JAR file, if
   *                      available. This makes differential analyses easier to implement. This argument
   *                      may be None if either this is the first JAR file or the last calculation failed.
   * @param customOptions Custom analysis options taken from the CLI. Can be used to modify behavior
   *                      of the analysis via command-line
   * @return Try[T] object holding the intermediate result, if successful
   */
  override protected def produceAnalysisResultForJAR(project: Project[URL],file: File, lastResult: Option[(Double, String)], customOptions: OptionMap): Try[(Double, String)] = {
    currentFile = file.getName
    produceAnalysisResultForJAR(project,lastResult,customOptions)
  }

  override def produceAnalysisResultForJAR(project: Project[URL], lastResult: Option[(Double, String)], customOptions: OptionMap): Try[(Double, String)] = {

    var resultList = List[MetricValue]()

    project.allProjectClassFiles.foreach(c => {
      val supertypes = project.classHierarchy.allSupertypes(c.thisType).size
      val className = c.thisType.fqn
      resultList =MetricValue(className,this.analysisName,supertypes) :: resultList
    })

    var classCount:Double = 0
    var dITSum:Double = 0
    var averageDIT:Double = 0.0

    resultList.foreach(l =>{
      dITSum += l.metricValue
      classCount += 1
    })

    if(classCount>0){
      averageDIT = dITSum/classCount
      if(!initialRound){
        log.info(s"averageDIT: $averageDIT, preDIT: $preVersionAverageDIT")
        difDITBetweenVersions = (averageDIT - preVersionAverageDIT)/preVersionAverageDIT
        log.info(s"difCoupling: $difDITBetweenVersions")
      }
    }
    initialRound = false
    preVersionAverageDIT = averageDIT
    val entityIdent: String = s"DIT:$previousFile:$currentFile"
    previousFile = currentFile
    roundCounter = roundCounter +1

    Try(difDITBetweenVersions,entityIdent)

  }

  /**
   * This method is called after all individual intermediate results have been calculated. It may
   * consume those intermediate results and produce a list of JAR file metrics, which can either
   * concern each JAR file individually, or the batch of analyzed files as a whole.
   *
   * @return List of JarFileMetricsResults
   */
  override def produceMetricValues(): List[MetricsResult] = {
    val difCBO = analysisResultsPerFile.values.map(_.get).
      toList.map(value => MetricValue(value._2,"DIT-difference",value._1))

    val metricResultBuffer = collection.mutable.ListBuffer[MetricsResult]()
    val metricValueBuffer = collection.mutable.ListBuffer[MetricValue]()

    metricValueBuffer.appendAll(difCBO)


    metricResultBuffer.append(MetricsResult(analysisName,jarDir,success = true,metricValues = metricValueBuffer.toList))

    metricResultBuffer.toList

  }

  /**
   * The name for this analysis implementation. Will be used to include and exclude analyses via CLI.
   */
  override def analysisName: String = "DITDifMultiFile"
}
