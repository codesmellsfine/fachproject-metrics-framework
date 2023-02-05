package org.tud.sse.metrics
package impl

import java.io.File
import java.net.URL

import org.opalj.br.analyses.Project
import org.tud.sse.metrics.analysis.{MetricValue, MetricsResult, MultiFileAnalysis}
import org.tud.sse.metrics.input.CliParser.OptionMap

import scala.collection.mutable.ListBuffer
import scala.util.Try

class WMCMFMAnalysis(jarDir: File) extends MultiFileAnalysis[(String, String, Double)](jarDir){

  var previousFile: String = ""
  var currentFile: String = ""
  var initialRound: Boolean = true
  var roundCounter: Integer = 0
  var preVersionAverageWMC: Double = 0
  var difWMCBetweenVersions: Double = 0

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
  override protected def produceAnalysisResultForJAR(project: Project[URL],file:File, lastResult: Option[(String, String, Double)], customOptions: OptionMap): Try[(String, String, Double)] = {
    currentFile = file.getName
    produceAnalysisResultForJAR(project,lastResult,customOptions)
  }

  override def produceAnalysisResultForJAR(project: Project[URL], lastResult: Option[(String, String, Double)], customOptions: OptionMap): Try[(String, String, Double)] = {

    val classes = project.allProjectClassFiles
    var WMCProjectSum = 0.0
    val classesCount = classes.size
    val rlist = new ListBuffer[MetricValue]()
    var WMCMax = 0.0
    var WMCMaxName = ""

    //Iterating over all classes in project
    for ( classFile <-  classes) {
      val methods = classFile.methodsWithBody
      var WMC = 0.0
      //iterating over all methods in class
      while (methods.hasNext) {
        val method = methods.next()

          val body = method.body
          var e = 0
          var n = 0
          body.get.instructions.foreach { instruction =>
            Try {
              if (instruction.isCompoundConditionalBranchInstruction) e = e + instruction.asCompoundConditionalBranchInstruction.jumpOffsets.size
              else if (instruction.isSimpleConditionalBranchInstruction) e = e + 2
              else if (!instruction.isReturnInstruction) e = e + 1
              n = n + 1
            }
          }
          //Complexity for current method
          val complexity = e - n + 2
          WMC = WMC + complexity
        }
      //Checking if new max value found
      if(WMC > WMCMax) {
        WMCMax = WMC
        WMCMaxName = classFile.fqn
      }
      //Adding to sum and saving WMC of current class
      WMCProjectSum = WMCProjectSum + WMC
    }
    val averageWMC = WMCProjectSum/classesCount

    if(!initialRound){
//      log.info(s"averageCoupling: $averageWMC, preCoupling: $preVersionAverageWMC")
      difWMCBetweenVersions = (averageWMC - preVersionAverageWMC)/preVersionAverageWMC
//      log.info(s"difCoupling: $difWMCBetweenVersions")
    }

    initialRound = false
    preVersionAverageWMC = averageWMC
    val entityIdent: String = s"WMC:$previousFile:$currentFile"
    val prevFileTmp = previousFile
    previousFile = currentFile
    roundCounter = roundCounter +1

    Try(prevFileTmp,currentFile,difWMCBetweenVersions)
  }


  /**
   * This method is called after all individual intermediate results have been calculated. It may
   * consume those intermediate results and produce a list of JAR file metrics, which can either
   * concern each JAR file individually, or the batch of analyzed files as a whole.
   *
   * @return List of JarFileMetricsResults
   */
  override def produceMetricValues(): List[MetricsResult] = {
    val difWMC = analysisResultsPerFile.values.map(_.get).
      toList.map(value => MetricValue(value._1,value._2, analysisName,value._3))

    val metricResultBuffer = collection.mutable.ListBuffer[MetricsResult]()
    val metricValueBuffer = collection.mutable.ListBuffer[MetricValue]()

    metricValueBuffer.appendAll(difWMC)


    metricResultBuffer.append(MetricsResult(analysisName,jarDir,success = true,metricValues = metricValueBuffer.toList))

    metricResultBuffer.toList
  }

  /**
   * The name for this analysis implementation. Will be used to include and exclude analyses via CLI.
   */
  override def analysisName: String = "WMC"
}
