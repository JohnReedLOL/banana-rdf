package org.w3.banana

/**
 * A Module that gathers the types needed to define an RDF implementation
 * Closely based on
 *   http://dvcs.w3.org/hg/rdf/raw-file/default/rdf-concepts/index.html
 * But with the  notable exceptions:
 *   - we allow literals in subject position - for help later in reasoning.
 */
trait RDFOperations[Rdf <: RDF] {

  trait GraphCompanionObject {
    def empty: Rdf#Graph
    def apply(elems: Rdf#Triple*): Rdf#Graph
    def apply(it: Iterable[Rdf#Triple]): Rdf#Graph
    def toIterable(graph: Rdf#Graph): Iterable[Rdf#Triple]
  }
  val Graph: GraphCompanionObject

  trait TripleCompanionObject extends Function3[Rdf#Node, Rdf#URI, Rdf#Node, Rdf#Triple] {
    def unapply(t: Rdf#Triple): Option[(Rdf#Node, Rdf#URI, Rdf#Node)]
  }
  
  val Triple: TripleCompanionObject

  trait NodeCompanionObject {
    def fold[T](node: Rdf#Node)(funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T): T
  }
  
  val Node: NodeCompanionObject
  
  trait URICompanionObject extends Function1[String, Rdf#URI] {
    def unapply(i: Rdf#URI): Option[String]
  }
  
  val URI : URICompanionObject

  trait BNodeCompanionObject extends Function1[String, Rdf#BNode] with Function0[Rdf#BNode] {
    def unapply(bn: Rdf#BNode): Option[String]
  }
  
  val BNode: BNodeCompanionObject

  trait LiteralCompanionObject {
    def fold[T](literal: Rdf#Literal)(funTL: Rdf#TypedLiteral => T, funLL: Rdf#LangLiteral => T): T
  }
  
  val Literal: LiteralCompanionObject
  
  trait TypedLiteralCompanionObject extends Function2[String, Rdf#URI, Rdf#TypedLiteral] with Function1[String, Rdf#TypedLiteral] {
    def unapply(tl: Rdf#TypedLiteral): Option[(String, Rdf#URI)]
    def apply(lexicalForm: String): Rdf#TypedLiteral = TypedLiteral(lexicalForm, URI("http://www.w3.org/2001/XMLSchema#string"))
  }
  
  val TypedLiteral: TypedLiteralCompanionObject
  
  trait LangLiteralCompanionObject extends Function2[String, Rdf#Lang, Rdf#LangLiteral] {
    def unapply(ll: Rdf#LangLiteral): Option[(String, Rdf#Lang)]
  }
  
  val LangLiteral: LangLiteralCompanionObject
  
  trait LangCompanionObject extends Function1[String, Rdf#Lang] {
    def unapply(l: Rdf#Lang): Option[String]
  }
  
  val Lang: LangCompanionObject

  // pimps
  
  class URIW(iri: Rdf#URI) {
    def asString: String = {
      val URI(stringURI) = iri
      stringURI
    }
  }

  implicit def wrapURI(iri: Rdf#URI): URIW = new URIW(iri)

  class GraphW(graph: Rdf#Graph) {
    def toIterable: Iterable[Rdf#Triple] = Graph.toIterable(graph)
  }
  
  implicit def wrapGraph(graph: Rdf#Graph): GraphW = new GraphW(graph)
  implicit def graphAsIterable(graph: Rdf#Graph): Iterable[Rdf#Triple] = Graph.toIterable(graph)
  
  implicit def tupleToTriple(tuple: (Rdf#Node, Rdf#URI, Rdf#Node)): Rdf#Triple = Triple(tuple._1, tuple._2, tuple._3)

  class TripleW(triple: Rdf#Triple) {
    val Triple(subject, predicate, objectt) = triple
  }
  implicit def wrapTriple(triple: Rdf#Triple): TripleW = new TripleW(triple)

  class NodeW(node: Rdf#Node) {
    def fold[T](funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T): T =
      Node.fold(node)(funURI, funBNode, funLiteral)
  }
  
  implicit def wrapNode(node: Rdf#Node): NodeW = new NodeW(node)
  
  class LiteralW(literal: Rdf#Literal) {
    def lexicalForm = Literal.fold(literal) (
      { case TypedLiteral(s, _) => s },
      { case LangLiteral(s, _) => s }
    )
    def fold[T](funTL: Rdf#TypedLiteral => T, funLL: Rdf#LangLiteral => T): T = Literal.fold(literal)(funTL, funLL)
  }
  
  private val _xsd = XSDPrefix(this)

  implicit def wrapLiteral(literal: Rdf#Literal): LiteralW = new LiteralW(literal)
  
  implicit def wrapIntAsLiteral(i: Int): Rdf#TypedLiteral = TypedLiteral(i.toString, _xsd.integer)
  
  implicit def wrapStringAsLiteral(s: String): Rdf#TypedLiteral = TypedLiteral(s, _xsd.string)
  
  implicit def wrapFloatAsLiteral(f: Double): Rdf#TypedLiteral = TypedLiteral(f.toString, _xsd.double)
  
  class LiteralBuilder(lexicalForm: String) {
    def datatype(datatype: Rdf#URI): Rdf#TypedLiteral = TypedLiteral(lexicalForm, datatype)
    def lang(tag: String): Rdf#LangLiteral = LangLiteral(lexicalForm, Lang(tag))
    def § = TypedLiteral(lexicalForm, _xsd.string)
  }
  
  implicit def wrapStringInLiteralBuilder(lexicalForm: String): LiteralBuilder = new LiteralBuilder(lexicalForm)

  
}
