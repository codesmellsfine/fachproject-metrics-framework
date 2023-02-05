package org.tud.sse.metrics
package impl

import org.tud.sse.metrics.impl.Version.Version

class MFMResultsUtility(var metricName: String, var prevVersion: String, var currentVersion: String, var metricResult: String) {

//  protected val log: Logger = LoggerFactory.getLogger(this.getClass)
//  log.info(s"metricName: $metricName, prevVersion: $prevVersion, curVersion: $currentVersion, metricResult: $metricResult")
  var versionType:Version = determineVersionType(extractVersionNumber(prevVersion),extractVersionNumber(currentVersion))


   def extractVersionNumber(fileName: String): String = {
     if(fileName!= ""){
       val indexVersionStart = (fileName.lastIndexOf("-")+1)
       val indexVersionEnd = (fileName.indexOf(".jar"))
       fileName.substring(indexVersionStart,indexVersionEnd)
     } else{
       ""
     }
   }

  def determineVersionType(prevVersionNumber: String, curVersionNumber: String): Version = {

    val prevVersionNumbers:Array[String] = prevVersionNumber.split("\\.")
    val curVersionNumbers:Array[String] = curVersionNumber.split("\\.")
    if(prevVersionNumbers.length> 0 && curVersionNumbers.length > 0) {
      if (prevVersionNumbers(0).compareTo(curVersionNumbers(0)) != 0) {
//        log.info(s"First VersionNumberPrev: ${prevVersionNumbers(0)}, First VersionNumberCur: ${curVersionNumbers(0)}")
        Version.MAJORVERSION
      } else if (prevVersionNumbers(1).compareTo(curVersionNumbers(1)) != 0) {
        Version.MINORVERSION
      } else {
        Version.HOTFIXVERSION
      }
    } else{
//      log.info(s"prevVersionNumbersLength: ${prevVersionNumbers.length}, curVersionNUmbersLength: ${curVersionNumbers.length}")
      Version.NOCHANGE
    }

  }

}
