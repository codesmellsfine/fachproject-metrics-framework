package org.tud.sse.metrics
package impl

import org.tud.sse.metrics.analysis.SingleFileAnalysis
import org.tud.sse.metrics.application.SingleFileAnalysisApplication
import org.tud.sse.metrics.impl.group1.DepthOfInheritanceTreeAnalysis


object SingleFileAnalysisApplication extends SingleFileAnalysisApplication {

  override protected val registeredAnalyses: Seq[SingleFileAnalysis] = Seq(
    // ck-suite wmc, dit, noc, cbo, RFC (Response for a Class), LCOM (NOT INCLUDED!!)
    new DepthOfInheritanceTreeAnalysis()
//    new NumberOfChildrenAnalysis(),
//    new LackOfCohesionInMethodsAnalysis(),
    // new CBOAnalysis()
//    new WeightedMethodsPerClassAnalysis(),
//    // Number of functions, Lines of Code logical physical
//    new NumberOfFunctionsAnalysis(),
     // new LOCproAnalysis()


//    //Gruppe 1
//    new AverageMaximumNestingAnalysis(),
//    // Depth of Inheritance Tree ck-suite
//    new DepthOfInheritanceTreeAnalysis(),
//    new MaximumNestingAnalysis(),
//    // Number of Children ck-suite
//    new NumberOfChildrenAnalysis(),
//
//    //Gruppe 2
//    new AVGFanInAnalysis(),
//    new AVGFanOutAnalysis(),
//    new ClassesReferencedAnalysis(),
//    // Lack of Cohesion in Methods ck-suite
//    new LackOfCohesionInMethodsAnalysis(),
//    // Number of functions relevant
//    new NumberOfFunctionsAnalysis(),
//
//    //Gruppe 3
//    // Coupling between objects ck-suite
//    new CBOAnalysis(),
//    new LCCAnalysis(),
//    // Lines of Code pro
//    new LOCproAnalysis(),
//    new TCCAnalysis(),
//
//    // Gruppe 4
//    // Lines of Code physical
//    new LOCphyAnalysis()
//    // MCCabe Cyclomatic Complexity
//    new MCCCAnalysis(),
//    new DACAnalysis(),
//    // Response for a class ck-suite
//    new RFCAnalysis(),
//    // Logical Lines Of Code
//    new LLOCAnalysis(),
//
//    //Gruppe 5
//    new NumberOfLoopsAnalysis(),
//    new NumberOfVariablesDeclaredAnalysis(),
//    // Weighted Methods per class ck-suite
//    new WeightedMethodsPerClassAnalysis(),
//    new vrefAnalysis()
  )
}
