import scala.collection.mutable



var in = mutable.HashMap[String, List[String]]()
in += ("foo" -> List("foo", "foofoo"))
in


in.map {case (k, v) => (k, v.reverse)}


val two = List(1, 2, 3, 4, 5)

two.zipWithIndex

