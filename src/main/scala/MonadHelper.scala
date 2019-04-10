import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Either, Failure, Left, Right, Success, Try}


object MonadHelper {
  def asFuture[X](xy: Try[X]): Future[X] = xy match {
    case Success(s) => Future.successful(s)
    case Failure(e) => Future.failed(e)
  }

  def sequence[X](xy: Try[X]): Either[Throwable, X] =
    xy match {
      case Success(s) => Right(s)
      case Failure(e) => Left(e)
    }

  def sequence[X](xf: Future[X])(implicit executor: ExecutionContext): Future[Either[Throwable, X]] =
    xf transform( { s => Right(s) }, { f => f }) recoverWith[Either[Throwable, X]] { case f => Future(Left(f)) }

  // Hint: write as a for-comprehension, using the method sequence (above).
  // 6 points.
  def sequence[X](xfs: Seq[Future[X]])(implicit executor: ExecutionContext): Seq[Future[Either[Throwable, X]]] = for (x <- xfs) yield sequence(x) // TO BE IMPLEMENTED

  def sequence[X](xys: Seq[Try[X]]): Try[Seq[X]] = (Try(Seq[X]()) /: xys) {
    (xsy, xy) => for (xs <- xsy; x <- xy) yield xs :+ x
  }

  def sequence[X](xys: Stream[Try[X]]): Try[Stream[X]] = (Try(Stream[X]()) /: xys) {
    (xsy, xy) => for (xs <- xsy; x <- xy) yield xs :+ x
  }

  def sequence[X](xos: Seq[Option[X]]): Option[Seq[X]] = (Option(Seq[X]()) /: xos) {
    (xso, xo) => for (xs <- xso; x <- xo) yield xs :+ x
  }

  // Hint: this one is a little more tricky. Remember what I mentioned about Either not being a pure monad -- it needs projecting
  // 7 points.
  def sequence[X](xe: Either[Throwable, X]): Option[X] = xe match {
    case Left(e) => throw e
    case Right(x) => Option(x)
  }
}

