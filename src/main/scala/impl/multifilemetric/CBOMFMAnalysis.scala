package org.tud.sse.metrics
package impl

import java.io.File
import java.net.URL

import org.opalj.br.analyses.Project
import org.tud.sse.metrics.analysis.{MetricValue, MetricsResult, MultiFileAnalysis}
import org.tud.sse.metrics.input.CliParser.OptionMap

import scala.util.Try

class CBOMFMAnalysis(jarDir: File) extends MultiFileAnalysis[(String, String, Double)](jarDir) {

  var previousFile: String = ""
  var currentFile: String = ""
  var initialRound: Boolean = true
  var roundCounter: Integer = 0

  var preVersionAverageCBO: Double = 0
  var difCBOBetweenVersions: Double = 0




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
  override protected def produceAnalysisResultForJAR(project: Project[URL],file: File, lastResult: Option[(String, String, Double)], customOptions: OptionMap): Try[(String, String, Double)] = {
    currentFile = file.getName
    produceAnalysisResultForJAR(project,lastResult,customOptions)
  }

  override def produceAnalysisResultForJAR(project: Project[URL], lastResult: Option[(String, String, Double)], customOptions: OptionMap): Try[(String, String, Double)] = {
    var resultList = List[MetricValue]()
    //Creating a set of all classes in the project
    var allProjectClasses = Set[String]()
    project.allProjectClassFiles.foreach(c=>
      allProjectClasses += c.fqn.replaceAll("/", ".")
    )
    //Iterating over all the methods of each class and find the classes which they access to. CBO is defined as the number of those classes which are being accessed from this class.
    project.allProjectClassFiles.foreach(c =>{
      // log.info("Calculating CBO of class: " + c.fqn)
      var coupledWith = Set[String]()
      c.methods.foreach( m=>{
        //         log.info("method name: " + m.name)
        if(m.body.isDefined) {
          //           m.body.get.instructions.foreach(i => if (i != null) println("-- " + i))
          allProjectClasses.foreach(classInProject =>
            if(classInProject!= c.fqn.replaceAll("/", ".") && m.body.get.instructions.mkString.contains(classInProject))
              coupledWith += classInProject
          )
        }
      }
      )
      // log.info("coupled with: " + coupledWith)
      val className = c.thisType.simpleName
      resultList = MetricValue(className, this.analysisName,"", coupledWith.size)::resultList
    }
    )
    var classCount:Double = 0
    var couplingSum:Double = 0
    var averageCoupling:Double = 0.0
    resultList.foreach(c =>{
      couplingSum += c.metricValue
      classCount += 1
    })
    if(classCount>0) {
    averageCoupling = couplingSum/classCount
    if(!initialRound){
      log.info(s"averageCoupling: $averageCoupling, preCoupling: $preVersionAverageCBO")
      difCBOBetweenVersions = (averageCoupling - preVersionAverageCBO)/preVersionAverageCBO
      log.info(s"difCoupling: $difCBOBetweenVersions")
    }
    }

    initialRound = false
    preVersionAverageCBO = averageCoupling
    val entityIdent: String = s"CBO:$previousFile:$currentFile"
    val prevFileTmp = previousFile
    previousFile = currentFile
    roundCounter = roundCounter +1

    Try(prevFileTmp,currentFile,difCBOBetweenVersions)
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
      toList.map(value => MetricValue(value._1,value._2,analysisName,value._3))

    val metricResultBuffer = collection.mutable.ListBuffer[MetricsResult]()
    val metricValueBuffer = collection.mutable.ListBuffer[MetricValue]()

    metricValueBuffer.appendAll(difCBO)


    metricResultBuffer.append(MetricsResult(analysisName,jarDir,success = true,metricValues = metricValueBuffer.toList))

    metricResultBuffer.toList

  }

  /**
   * The name for this analysis implementation. Will be used to include and exclude analyses via CLI.
   */
  override def analysisName: String = "CBO"
}
