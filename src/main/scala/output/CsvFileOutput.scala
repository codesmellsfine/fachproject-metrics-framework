package org.tud.sse.metrics
package output


import org.slf4j.{Logger, LoggerFactory}
import org.tud.sse.metrics.analysis.MetricsResult
import org.tud.sse.metrics.impl.{MFMResultsUtility, SQLConnector}
import org.tud.sse.metrics.output.StatisticsOutput.withCsvWriter

import scala.collection.mutable
import scala.util.Try

/**
 * Trait providing functionality to export a list of metrics results to a CSV file.
 *
 * @author Johannes Düsing
 */
trait CsvFileOutput {
  protected val log: Logger = LoggerFactory.getLogger(this.getClass)


  def writeResultsToFile(outputFilePath: String, results: List[MetricsResult]): Try[Unit] = withCsvWriter(outputFilePath){ csvWriter =>
    val headings = Array("MetricName","PreVersionFile","CurrentversionFile","PreVersion","CurrentVersion","MetricValue","VersionChangeType")
    csvWriter.writeNext(headings)


    val fileMetricsMap: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap()
    // val fileMetricsList: List
    var mFMResults:List[MFMResultsUtility] = List()
    val sQLConnector: SQLConnector = new SQLConnector {}
    sQLConnector.createMFMResultTable()


    results.foreach(result =>{
      result.metricValues.foreach(value =>{
        val mFMResult = new MFMResultsUtility(value.metricName, value.previousVersion, value.currentVersion, value.metricValue)
        mFMResults = mFMResult::mFMResults
        val arrayToWrite = Array(value.metricName,value.previousVersion, value.currentVersion, mFMResult.extractVersionNumber(value.previousVersion),mFMResult.extractVersionNumber(value.currentVersion),value.metricValue.toString, mFMResult.versionType.toString)
        csvWriter.writeNext(arrayToWrite)
      })
    })
    sQLConnector.fillMetricResultTable {
      mFMResults
    }



    //    results.foreach{ res =>
//      res.metricValues.foreach { value =>
//        val entityIdent = value.previousVersion + "$" + value.currentVersion + "$" + value.metricName
//
//        if(!fileMetricsMap.contains(entityIdent)){
//          fileMetricsMap.put(entityIdent, new mutable.HashMap())
//        }
//
//        val theMap = fileMetricsMap(entityIdent)
//
//        if(!theMap.contains(value.metricName)) {
//          theMap.put(value.metricName, value.metricValue)
//        }
//      }
//    }


//    fileMetricsMap.map{ tuple =>
//      var splitIndex = tuple._1.indexOf("$")
//      val prevVersion = tuple._1.substring(0, splitIndex)
//      splitIndex = tuple._1.indexOf("$",splitIndex)
//      val curVersion = tuple._1.substring(0,splitIndex)
//      val entityName = tuple._1.substring(splitIndex+1)
//      (List(prevVersion,curVersion,entityName) ++
//        (2 until headings.length).map{ index =>
//          tuple._2.get(headings(index)).map(_.toString).getOrElse("")
//        }).toArray
//    }
//      .foreach(t => csvWriter.writeNext(t))
  }

}
