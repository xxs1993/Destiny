import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Either, Failure, Left, Right, Success, Try}


object MonadHelper {
  def asFuture[X](xy: Try[X]): Future[X] = xy match {
    case Success(s) => Future.successful(s)
    case Failure(e) => Future.failed(e)
  }

}

