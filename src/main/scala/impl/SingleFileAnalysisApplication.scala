package org.tud.sse.metrics
package impl

import org.tud.sse.metrics.analysis.SingleFileAnalysis
import org.tud.sse.metrics.application.SingleFileAnalysisApplication


object SingleFileAnalysisApplication extends SingleFileAnalysisApplication {

  override protected val registeredAnalyses: Seq[SingleFileAnalysis] = Seq(

  )
}
