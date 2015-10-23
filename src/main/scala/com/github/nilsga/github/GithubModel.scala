package com.github.nilsga.github

object GithubModel {
  case class User(id: String)
  case class Repo(id: String, name: String, full_name: String, description: String)
}
