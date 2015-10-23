package com.github.nilsga.github

import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.unmarshalling.Unmarshaller
import com.github.nilsga.github.GithubModel.{Repo, User}

import scala.concurrent.{ExecutionContext, Future}

object Dsl {

  implicit def toFuture[T](dsl: BaseDsl[T])(implicit api: GithubApiClient, ec: ExecutionContext): Future[T] = dsl.execute

  trait BaseDsl[T] {

    implicit def um: Unmarshaller[ResponseEntity, T]
    def path: String

    def execute(implicit ec: ExecutionContext, api: GithubApiClient) : Future[T] = api.request[T](path)

  }

  class UserDsl(val id: String)(implicit val um: Unmarshaller[ResponseEntity, User], val umSeqRepo: Unmarshaller[ResponseEntity, Seq[Repo]], api: GithubApiClient) extends BaseDsl[User]{

    override def path = s"/users/$id"

    def repos = new UserReposDsl(id)

  }

  class UserReposDsl(val id: String)(implicit val um: Unmarshaller[ResponseEntity, Seq[Repo]]) extends BaseDsl[Seq[Repo]] {

    override def path: String = s"/users/$id/repos"
  }

}