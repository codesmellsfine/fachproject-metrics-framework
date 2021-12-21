package impl.group2

import java.net.URL

import org.opalj.br.Method
import org.opalj.br.analyses.Project
import org.tud.sse.metrics.analysis.{MetricValue, SingleFileAnalysis}
import org.tud.sse.metrics.input.CliParser.OptionMap

import scala.util.Try

class LackOfCohesionInMethodsAnalysis extends SingleFileAnalysis{

  override def analyzeProject(project: Project[URL], customOptions: OptionMap): Try[Iterable[MetricValue]] = Try {
    val list: List[MetricValue] = List()
    val classes = project.allProjectClassFiles

    classes.foreach(singleClass => {
      /*println("newclass")
      singleClass.fields.foreach(f => println(s"$f"))*/

      val methods = singleClass.methods

      /*methods.foreach(m => {
        println("newmethod")
        if(m.body.isDefined){
          m.body.get.localVariableTable match{
            case Some(t) => t.foreach(v => println(s"$v"))
            case None =>
          }
        }
      })*/

      var metric = 0 // Wert der LCOM-Metrik

      // es wird fuer alle Methodenpaare geprueft, ob die Attributmengen disjunkt sind
      // wenn ja, wird metric um 1 erhöht, sonst um 1 verringert
      for (i <- methods; j <- methods
           if (!i.equals(j))) {
        if (disjunctAttributeSets(i, j)) {
          metric = metric + 1
        } else {
          metric = metric - 1
        }
      }

      // in der Definition ist LCOM nie negativ, das Minimum ist 0
      if (metric < 0) {
        metric = 0
      }

      // disjunctAttributeSets prueft, ob die Methoden m1 und m2 ausschliesslich auf unterschiedliche Attribute zugreifen
      def disjunctAttributeSets(m1: Method, m2: Method): Boolean = {
        var bool = true
        for (i <- m1.attributes; j <- m2.attributes) {
          if (i.equals(j)) {
            bool = false
          }
        }
        bool
      }

      list.::(MetricValue("file", "LCOM", metric))
    })
    list
  }

  override def analysisName: String = "LCOM"

}
