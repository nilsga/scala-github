package com.github.nilsga.github

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, ResponseEntity, Uri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import com.github.nilsga.github.Dsl.UserDsl
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.{DefaultFormats, jackson}

import scala.concurrent.{ExecutionContext, Future}

object GithubApiClient {

  def apply(token: String)(implicit actorSystem: ActorSystem = ActorSystem("GithubApi")) = new GithubApiClient(token)(actorSystem, ActorMaterializer())
}

class GithubApiClient(val token: String)(implicit val actorSystem: ActorSystem, val materializer: ActorMaterializer) {

  val apiEndpoint = "https://api.github.com"
  val defaultUri = Uri(apiEndpoint)
  val authHeader = RawHeader("Authorization", s"token $token")
  implicit val api = this
  implicit val jacksonSerialization = jackson.Serialization
  implicit val formats = DefaultFormats

  def user(userId: String)(implicit ec: ExecutionContext): UserDsl = {
    new UserDsl(userId)
  }

  def request[T](path: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext, um: Unmarshaller[ResponseEntity, T]) : Future[T] = {

    Http().singleRequest(HttpRequest(uri = uri(path, params)).withHeaders(authHeader)).flatMap(resp => {
      resp.status match {
        case OK => Unmarshal(resp.entity).to[T]
        case _ => Unmarshal(resp.entity).to[String].flatMap(entity => Future.failed(new RuntimeException(entity)))
      }
    })
  }

  private def uri(path: String, params: Map[String, String]) = defaultUri
    .withPath(Uri.Path(path))
    .withQuery(params)

}
