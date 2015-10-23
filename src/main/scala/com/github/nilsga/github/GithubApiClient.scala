package com.github.nilsga.github

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.github.nilsga.github.Dsl.UserDsl
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}

import scala.concurrent.{ExecutionContext, Future}

object GithubApiClient {

  def apply(token: String)(implicit actorSystem: ActorSystem = ActorSystem("GithubApi")) = new GithubApiClient(token)(actorSystem, ActorMaterializer())
}

class GithubApiClient(val token: String)(implicit val actorSystem: ActorSystem, val materializer: ActorMaterializer) {

  val apiEndpoint = "https://api.github.com"
  val defaultUri = Uri(apiEndpoint)
  val authHeader = RawHeader("Authorization", s"token $token")

  def user(userId: String)(implicit ec: ExecutionContext): UserDsl = {
    new UserDsl(userId)
  }

  def request[T](path: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext, manifest: Manifest[T]) : Future[T] = {

    implicit val serializer = jackson.Serialization
    implicit val formats = DefaultFormats

    import Json4sSupport._

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
