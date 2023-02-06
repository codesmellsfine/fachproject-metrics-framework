package org.tud.sse.metrics
package impl

import java.sql.{Connection, DriverManager, PreparedStatement, Statement}

abstract class SQLConnector {
  var connection: Connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Matjap\\Documents\\Studium\\BachelorArbeit\\IndexerCrawler\\BA-MavenData.db");
  var statement:Statement = connection.createStatement()

  def createMFMResultTable() = {
    statement.executeUpdate("drop table if exists MFMResultData")
    statement.executeUpdate("create table MFMResultData (MetricName string, PreviousVersionName string, CurrentVersionName string, PreviousVersion string, CurrentVersion string, MetricValue double, VersionChangeType string)")
  }

  def fillMetricResultTable(mFMResults: List[MFMResultsUtility]) ={
    this.connection.setAutoCommit(false)

    var insertTableSQL:String = "INSERT INTO MFMResultData" + "(MetricName, PreviousVersionName, CurrentVersionName, PreviousVersion,CurrentVersion,MetricValue,VersionChangeType) VALUES" + "(?,?,?,?,?,?,?)"

    var pstmt: PreparedStatement = connection.prepareStatement(insertTableSQL)

    mFMResults.foreach(result =>{
      pstmt.setString(1,result.metricName)
      pstmt.setString(2,result.prevFileName)
      pstmt.setString(3,result.curFileName)
      pstmt.setString(4,result.prevVersion)
      pstmt.setString(5,result.curVersion)
      pstmt.setDouble(6,result.metricResult)
      pstmt.setString(7,result.versionType.toString)
      pstmt.addBatch()
    })
    pstmt.executeBatch()
    connection.commit()
    pstmt.close()
    connection.close()

  }



}
