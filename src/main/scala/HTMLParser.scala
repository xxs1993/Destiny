
import javax.xml.transform.Source
import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

import scala.util.Try
import scala.xml.parsing.XhtmlParser

/**
  * @author scalaprof
  */
object HTMLParser {

  import java.io.ByteArrayInputStream

  import org.xml.sax.InputSource

  import scala.xml.Node
  import scala.xml.parsing.NoBindingFactoryAdapter

  lazy val adapter = new NoBindingFactoryAdapter()
  private lazy val parser = (new SAXFactoryImpl).newSAXParser

  def parse(html: String, encoding: String = "UTF-8"): Try[Node] = this.parse(html.getBytes(encoding))

  def parse(html: Array[Byte]): Try[Node] = Try{
    val stream = new ByteArrayInputStream(html)
    val source = new InputSource(stream)
//  val p = new XhtmlParser(new Source(stream))
    adapter.loadXML(source, parser)
  }
}
