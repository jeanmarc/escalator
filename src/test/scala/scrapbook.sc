import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._

val input = List(Some(10), Some(20), None, Some(40))

input.flatMap(x => x)

input.map(x => x)

implicit val ec = ExecutionContext.global

val f = Future[Int] {
  Thread.sleep(10)
  42
}

val x = Await.result(f, 1.seconds)


val p1 = Promise[Option[List[Int]]]
val p2 = Promise[Option[List[Int]]]
val p3 = Promise[Option[List[Int]]]

val f1 = p1.future
val f2 = p2.future
val f3 = p3.future

val fa = Seq(f1, f2, f3)

// now we want to create a Future[Option[List[Int]]] from this sequence of futures
// that holds the joined result of all Some(List[Int])

val fb = Future.sequence(fa)

val res = fb.flatMap(s => Future(s.flatMap(x => x).flatMap(x => x)))

p1.success(Some(List(1, 2, 3)))
p2.success(None)
p3.success(Some(List(3, 42)))

val answer = Await.result(res, 1.seconds)


