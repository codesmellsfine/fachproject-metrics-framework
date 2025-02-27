package org.tud.sse.metrics
package impl

import java.io.File
import java.net.URL

import org.opalj.br.analyses.Project
import org.tud.sse.metrics.analysis.{MetricValue, MetricsResult, MultiFileAnalysis}
import org.tud.sse.metrics.input.CliParser.OptionMap

import scala.collection.{Map, Set, SortedSet}
import scala.util.Try

/**
 * the ExternalStability metric
 * as defined in the paper "Identifying evolution patterns: a metrics-based approach for
 * external library reuse" by Eleni Constantinou und Ioannis Stamelos
 *
 * External_Stability = External_Removed * External_Remained
 * External_Removed = (number of classes contained in removed packages from Genreation i to (i+1))/(NUmber of all Classes
 * in generation i)
 * External_Remained = (number of all removed classes of remained packages from generation i to (i+1)/
 * (number of all Classes in generation i)
 *
 * @param jarDir directory containing the different jar-file versions of a software to analyze
 *               Optional CLI arguments:
 *  - --es_rem outputs values for partial metric es_removed: metric for classes of removed packages
 *  - --es_red outputs values for partial metric es_remained: metric for classes which have been removed from a package
 *  - --verbose log more information: file names, simple statistics, metric results
 *  - --ultra_verbose (like verbose) + list all package names and class (names) contained in each package
 *
 */
class ExternalStabilityAnalysis(jarDir: File) extends MultiFileAnalysis[(String, String, Double)](jarDir) {

  var previousPackages: Set[String] = Set[String]()
  var previousPackagesSize: Map[String, Int] = Map[String, Int]()
  var previousClassesInPackages: Map[String, Set[String]] = Map[String, Set[String]]()
  var previousNumberOfClasses: Int = 0
  var firstRound: Boolean = true
  var counter: Double = 0
  var verbose: Boolean = false
  var verbose_ultra: Boolean = false
  var es_rem: Boolean = false
  var es_red: Boolean = false
  var currentFile: String = ""
  var previousFile: String = ""

  private val sym_es_rem: Symbol = Symbol("es_rem")
  private val sym_es_red: Symbol = Symbol("es_red")
  private val sym_verbose: Symbol = Symbol("verbose")
  private val sym_verbose_ultra: Symbol = Symbol("verbose_ultra")


  /**
   * Calculate the ExternalStability metric
   * as defined in the paper "Identifying evolution patterns: a metrics-based approach for
   * external library reuse" by Eleni Constantinou und Ioannis Stamelos
   *
   * External_Stability = External_Removed * External_Remained
   * External_Removed = (number of classes contained in removed packages from Genreation i to (i+1))/(NUmber of all Classes
   * in generation i)
   * External_Remained = (number of all removed classes of remained packages from generation i to (i+1)/
   * (number of all Classes in generation i)
   *
   * @param project       Fully initialized OPAL project representing the JAR file under analysis
   * @param lastResult    Option that contains the intermediate result for the previous JAR file, if
   *                      available. This makes differential analyses easier to implement. This argument
   *                      may be None if either this is the first JAR file or the last calculation failed.
   * @param customOptions Custom analysis options taken from the CLI. Can be used to modify behavior
   *                      of the analysis via command-line
   * @return Try[T] object holding the intermediate result, if successful
   */
  override def produceAnalysisResultForJAR(project: Project[URL], file: File,
                                           lastResult: Option[(String, String, Double)],
                                           customOptions: OptionMap): Try[(String, String, Double)] = {
    currentFile = file.getName
    produceAnalysisResultForJAR(project, lastResult, customOptions)
  }

  /**
   * Calculcate the ExternalStability metric
   * as defined in the paper "Identifying evolution patterns: a metrics-based approach for
   * external library reuse" by Eleni Constantinou und Ioannis Stamelos
   *
   * @param project       Fully initialized OPAL project representing the JAR file under analysis
   * @param lastResult    Option that contains the intermediate result for the previous JAR file, if
   *                      available. This makes differential analyses easier to implement. This argument
   *                      may be None if either this is the first JAR file or the last calculation failed.
   * @param customOptions Custom analysis options taken from the CLI. Can be used to modify behavior
   *                      of the analysis via command-line
   * @return Try[T] object holding the intermediate result, if successful
   */
  override def produceAnalysisResultForJAR(project: Project[URL],
                                           lastResult: Option[(String, String, Double)],
                                           customOptions: OptionMap): Try[(String, String, Double)] = {
    //The ExternalStability metric consists of two parts ES_rem and ES_red
    //ES_rem is a metric that measures the relation between the number of classes which where contained
    //in removed packages and the total number of classes in a jar

    //Calculate ES_rem: first part of the ExternalStability metric

    es_rem = customOptions.contains(sym_es_rem)
    es_red = customOptions.contains(sym_es_red)
    verbose = customOptions.contains(sym_verbose)
    verbose_ultra = customOptions.contains(sym_verbose_ultra)


    val currentPackages: scala.collection.Set[String] = project.projectPackages
    val currentNumberOfClasses = project.projectClassFilesCount


    //Calculate the packages which have been removed in the newer version
    val packagesRemoved = previousPackages.diff(currentPackages)
    if (verbose_ultra) {
      log.info("removed Packages: " + packagesRemoved.size)
      for (rem_pack <- packagesRemoved) {
        log.info("removed Packages: " + rem_pack)
      }
    }

    //Sum the number of the classes in the removed packages
    var numberOfClassesInRemovedPackages = 0
    // foreach removed packages get the size and sum all the sizes up
    packagesRemoved.foreach(p => numberOfClassesInRemovedPackages += previousPackagesSize(p))


    //calculate current round number
    counter += 1

    if (verbose_ultra || verbose) {
      log.info("Round: " + counter)
      log.info("previous file: " + previousFile)
      log.info("current File: " + currentFile)
      log.info("Number of current Packages: " + currentPackages.size)
      log.info("Number of previous Packages: " + previousPackages.size)
      log.info("Number of Classes in Project: " + currentNumberOfClasses)
      log.info("Number of previousClasses in Project: " + previousNumberOfClasses)
      log.info("Number of Packages removed: " + packagesRemoved.size)
      log.info("Number of Classes in removed Packages: " + numberOfClassesInRemovedPackages)
    }

    //set a default value for ES_rem
    var ES_rem: Double = 1.0

    //Nothing to compare in the first round in a differential metric
    //Needs the data of two consequential jar-file versions
    //In the first round only data will be prepared for the differential analysis,starting from the second round
    if (!firstRound && previousNumberOfClasses > 0) {
      ES_rem = 1.0 - numberOfClassesInRemovedPackages.toDouble / previousNumberOfClasses.toDouble
    }

    //if only verbose: value of es_rem is logged immediately
    //verboseultra: output is delayed and positioned after all package names and class names have been logged
    //verboseultra: value of es_rem is logged to together with es and es_red at a later point in time
    if (verbose && !verbose_ultra) {
      log.info("ES_rem: " + ES_rem)
    }


    //Calculate the second partial metric ES_red
    // It is necessary to have access to the package-class relationship information in the following round
    // This information needs to be calculated and than needs to be stored in variable outside of this function
    var currentClassesInPackages: Map[String, Set[String]] = Map[String, Set[String]]()

    // For every package...
    for (curPack <- currentPackages) {
      var classes = Set[String]()
      // ...get every contained class
      for (curClass <- project.classesPerPackage(curPack)) {
        //store all the classes of a package in a Set[]
        //compare classes by their fully qualified name = fqn
        classes = classes + curClass.fqn
      }
      //..and store package-classes relationship information in a Map[]...
      currentClassesInPackages = currentClassesInPackages + (curPack -> classes)
    }

    if (verbose_ultra) {
      log.info("\n\nFile: " + currentFile)
      for (pack <- currentClassesInPackages) {
        log.info("\n\nPackage: " + pack._1 + " size: " + pack._2.size)
        currentClassesInPackages.get(pack._1).foreach(classes => {
          //Sort classes before outputting them, for easier insight,
          //only done when verboseultra is true
          val sortedClasses = SortedSet[String]() ++ classes
          for (cl <- sortedClasses) {
            log.info("Class : " + cl)
          }
        })
      }
    }


    var sum = 0
    //set a default value for ES_rem
    var ES_red: Double = 1.0

    //Nothing to compare in the first round in a differential metric
    //Needs the data of two consequential jar-file versions
    //In the first round only data will be prepared for the differential analysis,starting from the second round
    if (!firstRound && previousNumberOfClasses > 0) {
      //calculate packages, which exist in both jar-versions
      val packagesShared = previousPackages.intersect(currentPackages)
      // for every shared package calculate the number of classes removed from the subsequent package
      for (packShared <- packagesShared) {
        // calculate the classes, which where removed from a shared package
        val classesRemoved = previousClassesInPackages(packShared).diff(currentClassesInPackages(packShared))
        if (verbose_ultra) {
          for (cl <- classesRemoved) {
            log.info("Shared Package: " + packShared + " Removed Class: " + cl)
          }
        }
        sum += classesRemoved.size
      }
      if (verbose || verbose_ultra) {
        log.info("Classes removed from packages: " + sum)
      }
      ES_red = 1.0 - sum.toDouble / previousNumberOfClasses.toDouble
    }

    val external_stability_metric_value = ES_red * ES_rem


    //ultraverbose: delayed logging of es_rem
    //otherwise only the missing values of es_red and of es are logged
    if (verbose && !verbose_ultra) {
      log.info("ES_red: " + ES_red)
      log.info("ES: " + external_stability_metric_value)
    } else if (verbose_ultra) {
      log.info("ES_rem: " + ES_rem)
      log.info("ES_red: " + ES_red)
      log.info("ES: " + external_stability_metric_value)
    }

    var entity_ident: String = "Difference between: " + previousFile + " and " + currentFile
    previousPackages = currentPackages
    previousNumberOfClasses = currentNumberOfClasses
    firstRound = false
    previousPackagesSize = previousPackagesSize.empty
    currentPackages.foreach(p => previousPackagesSize = previousPackagesSize + (p -> project.classesPerPackage(p).size))
    //List all classes as organized in packages

    //Store the package-class relationship information for the processing in the next round
    previousClassesInPackages = currentClassesInPackages
    val prevFileTmp = previousFile
    previousFile = currentFile

    //The final StabilityMetric is calculated by ES_rem * ES_red
    //The result contains the StabilityMetric, the two partial metrics and round in which these metrics were calculated


    Try(prevFileTmp, currentFile,external_stability_metric_value)

  }

  override def produceMetricValues(): List[MetricsResult] = {

    //value == (external_stability_metric_value, ES_rem, ES_red, entity_ident)
    val stability_values = analysisResultsPerFile.values.map(_.get)
      .toList
      .map(value => MetricValue(value._1,value._2, analysisName, value._3))


    val MetricsResultBuffer = collection.mutable.ListBuffer[MetricsResult]()
    val MetricValueBuffer = collection.mutable.ListBuffer[MetricValue]()
    MetricValueBuffer.appendAll(stability_values)

    MetricsResultBuffer.append(MetricsResult(analysisName, jarDir, success = true, metricValues = MetricValueBuffer.toList))

    if (verbose_ultra || verbose) {
      MetricsResultBuffer.head.metricValues.foreach(value => log.info("Metrik Result: " + value))
    }

    MetricsResultBuffer.toList
  }

  override def analysisName: String = "ExtSta"


}
