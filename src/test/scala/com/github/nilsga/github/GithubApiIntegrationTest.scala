package com.github.nilsga.github

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent._
import scala.concurrent.duration._

class GithubApiIntegrationTest extends WordSpec with Matchers {

  val config = ConfigFactory.load()
  implicit val actorSystem = ActorSystem("GithubApiIntegrationTests", config)

  implicit val api = GithubApiClient(config.getString("github.token"))
  import Dsl._
  import actorSystem.dispatcher

  "Github API" should {

    "get user" in {
      val userFuture = api.user("nilsga")
      val user = Await.result(userFuture, 10 seconds)
      println(user)
    }

    "list user repos" in {

      //api.userWorking("nilsga")

      val reposFuture = api.user("nilsga").repos
      val repos = Await.result(reposFuture, 10 seconds)
      println(repos)
    }
  }

}
