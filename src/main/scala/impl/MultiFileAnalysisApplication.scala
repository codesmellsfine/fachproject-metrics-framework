package org.tud.sse.metrics
package impl

import java.io.File

import org.tud.sse.metrics.analysis.MultiFileAnalysis
import org.tud.sse.metrics.application.MultiFileAnalysisApplication

object MultiFileAnalysisApplication extends MultiFileAnalysisApplication {
  override protected def buildAnalyses(jarDirectory: File): Seq[MultiFileAnalysis[_]] = Seq(
    //Gruppe 1
    // new EvolutionAnalysis(jarDirectory)
    // new ExternalStabilityAnalysis(jarDirectory)
    // Singlefile metrics Difference between Versions
//    new LOCProMFMAnalysis(jarDirectory),
//    new CBOMFMAnalysis(jarDirectory),
//    new DITMFMAnalysis(jarDirectory),
//    new NFCMFMAnalysis(jarDirectory),
//    new NOCMFMAnalysis(jarDirectory),
//    new WMCMFMAnalysis(jarDirectory),
    new ExtEvolutionAnalysis(jarDirectory),
    new IntEvolutionAnalysis(jarDirectory)
    //Gruppe 5
    // new InternalStabilityAnalysis(jarDirectory)
  )
}
