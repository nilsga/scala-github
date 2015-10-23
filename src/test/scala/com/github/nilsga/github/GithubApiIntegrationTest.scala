package com.github.nilsga.github

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent._
import scala.concurrent.duration._

class GithubApiIntegrationTest extends WordSpec with Matchers with Json4sSupport {

  val config = ConfigFactory.load()
  implicit val actorSystem = ActorSystem("GithubApiIntegrationTests", config)

  implicit val api = GithubApiClient(config.getString("github.token"))
  import Dsl._
  import actorSystem.dispatcher

  "Github API" should {

    "list repos" in {
      //val repos = Await.result(api.repos, 10 seconds)
      //println(repos)
    }

    "get user" in {

      //api.userWorking("nilsga")

      val repos = api.user("nilsga").repos
      val userStr = Await.result(repos, 10 seconds)
      println(userStr)
    }
  }

}
