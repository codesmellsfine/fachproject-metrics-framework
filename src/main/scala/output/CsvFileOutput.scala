package org.tud.sse.metrics
package output

import StatisticsOutput.withCsvWriter

import scala.util.Try
import analysis.MetricsResult
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

/**
 * Trait providing functionality to export a list of metrics results to a CSV file.
 *
 * @author Johannes Düsing
 */
trait CsvFileOutput {
  protected val log: Logger = LoggerFactory.getLogger(this.getClass)


  def writeResultsToFile(outputFilePath: String, results: List[MetricsResult]): Try[Unit] = withCsvWriter(outputFilePath){ csvWriter =>

    val metricNames = results.flatMap(_.metricValues.map(_.metricName)).distinct
    val headings = (List("Path", "Entity") ++ metricNames).toArray
    csvWriter.writeNext(headings)

    val fileMetricsMap: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap()

    results.foreach{ res =>
      var metricResultCount: Double = 0
      var metricResultSum: Double = 0
      res.metricValues.foreach { value =>
        val entityIdent = res.jarFile.getPath + "$" + value.entityIdent

        if(!fileMetricsMap.contains(entityIdent)){
          fileMetricsMap.put(entityIdent, new mutable.HashMap())
        }

        val theMap = fileMetricsMap(entityIdent)

        if(!theMap.contains(value.metricName)) {
          theMap.put(value.metricName, value.metricValue)
          metricResultSum = metricResultSum + value.metricValue
          metricResultCount = metricResultCount+1
        }
      }
      log.info(s"File: ${res.jarFile}")
      log.info(s"MetricresultCount ${res.analysisName}: ${metricResultCount}")
      log.info(s"MetricResultSum ${res.analysisName}: ${metricResultSum}")
      log.info(s"Average metricresult ${res.analysisName}: ${metricResultSum/metricResultCount}")
    }


    fileMetricsMap.map{ tuple =>
      val splitIndex = tuple._1.indexOf("$")
      val fileName = tuple._1.substring(0, splitIndex)
      val entityName = tuple._1.substring(splitIndex + 1)
      (List(fileName, entityName) ++
        (2 until headings.length).map{ index =>
          tuple._2.get(headings(index)).map(_.toString).getOrElse("")
        }).toArray
    }
      .foreach(t => csvWriter.writeNext(t))
  }

}
