package org.tud.sse.metrics
package impl

import java.io.File
import java.net.URL

import org.opalj.br.analyses.Project
import org.tud.sse.metrics.analysis.{MetricValue, MetricsResult, MultiFileAnalysis}
import org.tud.sse.metrics.input.CliParser.OptionMap

import scala.util.Try

class ExtEvolutionAnalysis(jarDir: File) extends MultiFileAnalysis[(String, String, Double)](jarDir:File) {

  var previousFile: String = ""
  var currentFile: String = ""
  var previousPackages: scala.collection.Set[String] = Set[String]()
  var initialRound: Boolean = true
  var roundCounter: Integer = 0

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
  override protected def produceAnalysisResultForJAR(project: Project[URL], file: File, lastResult: Option[(String, String, Double)], customOptions: OptionMap): Try[(String, String, Double)] = {

    currentFile = file.getName
    produceAnalysisResultForJAR(project, lastResult, customOptions)
  }

  override protected def produceAnalysisResultForJAR(project: Project[URL], lastResult: Option[(String, String, Double)], customOptions: OptionMap): Try[(String, String, Double)] = {
    var externalEvolution: Double = 0
    var entityIdent: String = ""

    val currentPackages: scala.collection.Set[String] = project.projectPackages
    val currentNumberOfClasses: Double = project.projectClassFilesCount

    if (!initialRound) {
      //Calculate the new packages that doesn't exist in the previous version
      val newPackages = currentPackages.diff(previousPackages)
      var numberOfClassesInNewPackages: Double = 0

      newPackages.foreach(p => numberOfClassesInNewPackages += project.classesPerPackage(p).size)
//      log.info(s"Classes in new Packages Count: $numberOfClassesInNewPackages")

      if (currentNumberOfClasses != 0) {
//        log.info(s"Number of Classes: $currentNumberOfClasses")
        externalEvolution = numberOfClassesInNewPackages / currentNumberOfClasses
      }
//      log.info(s"externalEvolution: $externalEvolution")
    }

      entityIdent = s"ExtEvo:$previousFile:$currentFile"
      val prevFileTmp = previousFile
      previousFile = currentFile
      previousPackages = currentPackages
      initialRound = false

      Try(prevFileTmp,currentFile, externalEvolution)
    }

    /**
     * This method is called after all individual intermediate results have been calculated. It may
     * consume those intermediate results and produce a list of JAR file metrics, which can either
     * concern each JAR file individually, or the batch of analyzed files as a whole.
     *
     * @return List of JarFileMetricsResults
     */
    override def produceMetricValues(): List[MetricsResult] = {
      val extEvo = analysisResultsPerFile.values.map(_.get).
        toList.map(value => MetricValue(value._1,value._2, analysisName, value._3))

      val metricResultBuffer = collection.mutable.ListBuffer[MetricsResult]()
      val metricValueBuffer = collection.mutable.ListBuffer[MetricValue]()

      metricValueBuffer.appendAll(extEvo)


      metricResultBuffer.append(MetricsResult(analysisName, jarDir, success = true, metricValues = metricValueBuffer.toList))

      metricResultBuffer.toList
    }

    /**
     * The name for this analysis implementation. Will be used to include and exclude analyses via CLI.
     */
    override def analysisName: String = "ExtEvo"
}

