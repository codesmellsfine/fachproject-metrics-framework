package org.tud.sse.metrics
package impl

import java.io.File
import java.net.URL

import org.opalj.br.analyses.Project
import org.tud.sse.metrics.analysis.{MetricValue, MetricsResult, MultiFileAnalysis}
import org.tud.sse.metrics.input.CliParser.OptionMap

import scala.util.Try
import scala.util.control.Breaks.{break, breakable}

class IntEvolutionAnalysis(jarDir: File) extends MultiFileAnalysis[(String, String, Double)](jarDir:File) {

  var previousFile: String = ""
  var currentFile: String = ""
  var previousPackages: scala.collection.Set[String] = Set[String]()
  var initialRound: Boolean = true
  var roundCounter: Integer = 0
  val maintainedPackages: scala.collection.Set[String] = Set[String]()

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

    currentFile = file.toString
    produceAnalysisResultForJAR(project, lastResult, customOptions)
  }


  override protected def produceAnalysisResultForJAR(project: Project[URL], lastResult: Option[(String, String, Double)], customOptions: OptionMap): Try[(String, String, Double)] = {
    var internalEvolution: Double = 0
    var entityIdent: String = ""

    val currentPackages: scala.collection.Set[String] = project.projectPackages

    if (!initialRound) {
      //Calculate the new packages that doesn't exist in the previous version
      val newPackages = currentPackages.diff(previousPackages)
      var maintainedPackages = currentPackages.intersect(previousPackages)
      var interactionsWithNewPackages:Double = 0
      maintainedPackages = currentPackages.intersect(previousPackages)
      val maintainedPackagesSize:Double = maintainedPackages.size


      for(maintainedPackage <- maintainedPackages){
        breakable{
          for(newPackage <- newPackages){
            val classesFromMaintainedPackage = project.classesPerPackage(maintainedPackage)
            val classesFromNewPackage = project.classesPerPackage(newPackage)
            for(maintainedClass <- classesFromMaintainedPackage){
              for(newClass <- classesFromNewPackage){
                maintainedClass.methods.foreach(m =>
                  if(m.body.isDefined){
                    // interaction between two classes
                    if(m.body.get.instructions.mkString.contains(newClass.thisType.simpleName)){
                      interactionsWithNewPackages = interactionsWithNewPackages +1
                      // if there is an interaction between the maintained and new Package it will be counted once and the rest of the iterations
                      // for the package can be skipped, interaction between the packages is only counted once.
                      break
                    }
                  }
                )
              }
            }
          }
        }
      }

      log.info(s"Maintained Packages interactions with new Packages: $interactionsWithNewPackages")

      if(maintainedPackagesSize!=0){
        internalEvolution = interactionsWithNewPackages/maintainedPackages.size
        log.info(s"internalEvolution: $internalEvolution")
      }
    }

      entityIdent = s"IntEvo:$previousFile:$currentFile"
      val prevFileTmp = previousFile
      previousFile = currentFile
      previousPackages = currentPackages
      initialRound = false

      Try(prevFileTmp,currentFile,internalEvolution)
    }

    /**
     * This method is called after all individual intermediate results have been calculated. It may
     * consume those intermediate results and produce a list of JAR file metrics, which can either
     * concern each JAR file individually, or the batch of analyzed files as a whole.
     *
     * @return List of JarFileMetricsResults
     */
    override def produceMetricValues(): List[MetricsResult] = {
      val intEvo = analysisResultsPerFile.values.map(_.get).
        toList.map(value => MetricValue(value._1,value._2, analysisName, value._3))

      val metricResultBuffer = collection.mutable.ListBuffer[MetricsResult]()
      val metricValueBuffer = collection.mutable.ListBuffer[MetricValue]()

      metricValueBuffer.appendAll(intEvo)


      metricResultBuffer.append(MetricsResult(analysisName, jarDir, success = true, metricValues = metricValueBuffer.toList))

      metricResultBuffer.toList

    }

    /**
     * The name for this analysis implementation. Will be used to include and exclude analyses via CLI.
     */
    override def analysisName: String = "IntEvo"
}

