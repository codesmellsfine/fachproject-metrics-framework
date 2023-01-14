package impl.multifilemetric

import java.io.File
import java.net.URL

import org.opalj.br.analyses.Project
import org.tud.sse.metrics.analysis.{MetricsResult, MultiFileAnalysis}
import org.tud.sse.metrics.input.CliParser.OptionMap

import scala.util.Try

class LOCProMFMAnalysis(jarDir: File) extends MultiFileAnalysis[(Double,String)](jarDir){

  var previousFile: String = ""
  var currentFile: String = ""
  var initialRound: Boolean = true
  var roundCounter: Integer = 0
  var preLinesOfCodeCounter: Integer = 0

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
    currentFile = file.toString
    produceAnalysisResultForJAR(project,lastResult,customOptions)
  }

  override def produceAnalysisResultForJAR(project: Project[URL], lastResult: Option[(Double, String)], customOptions: OptionMap): Try[(Double, String)] = {
    var lineCounter = 0

    project.allProjectClassFiles.foreach(
      c => {
        c.methodsWithBody.foreach(
          m => if(m.body.exists(_.lineNumberTable.nonEmpty) || m.body.isDefined){
            if(m.returnType!=null){
              lineCounter = lineCounter + m.body.get.lineNumberTable.get.lineNumbers.size +1
            } else{
              lineCounter = lineCounter + m.body.get.lineNumberTable.get.lineNumbers.size
            }
          } else{
            lineCounter = lineCounter +0
          }
        )
      }
    )
    if(!initialRound){

    }

    initialRound = false
    previousFile = currentFile
    preLinesOfCodeCounter = lineCounter

    Try(0.0,"")
  }

  /**
   * This method is called after all individual intermediate results have been calculated. It may
   * consume those intermediate results and produce a list of JAR file metrics, which can either
   * concern each JAR file individually, or the batch of analyzed files as a whole.
   *
   * @return List of JarFileMetricsResults
   */
  override def produceMetricValues(): List[MetricsResult] = ???

  /**
   * The name for this analysis implementation. Will be used to include and exclude analyses via CLI.
   */
  override def analysisName: String = ???
}
