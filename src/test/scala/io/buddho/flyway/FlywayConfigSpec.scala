// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.buddho.flyway

import com.typesafe.config.ConfigFactory
import org.flywaydb.core.internal.util.Locations
import org.scalatest.{Matchers, FlatSpec}

import scala.collection.Seq


class FlywayConfigSpec extends FlatSpec with Matchers {


  "config" should "parse basic config" in {

    val c =
      """
        |db.default {
        |  url = "jdbc:postgresql://localhost:5432/buddho"
        |  user = sa
        |  password = sa
        |  driver = org.postgresql.Driver
        |}
        |
        |db.legacy {
        |  url = "jdbc:postgresql://localhost:5432/legacy"
        |  driver = org.postgresql.Driver
        |}
        |
        |flyway {
        |   enabled = true
        |   locations = []
        |   validateOnMigrate = true
        |   encoding = UTF-8
        |   outOfOrder = false
        |   schemas = []
        |   placeholders = {
        |     test = one
        |   }
        |   sqlMigrationPrefix = V
        |}
      """.stripMargin

    val config = FlywayConfig(ConfigFactory.parseString(c).withFallback(ConfigFactory.load()))

    val m1 = config.migrations.head

    m1.name should equal ('default)
    m1.enabled should be (true)
    m1.locations should be (Seq("db/migration/default"))
    m1.database should be (DatabaseConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/buddho",
      Some("sa"),
      Some("sa")))

    val m2 = config.migrations(1)

    m2.name should equal ('legacy)
    m2.locations should be (Seq("db/migration/legacy"))
    m2.database should be (DatabaseConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/legacy",
      None,
      None))
  }

  it should "parse environment config" in {

    val c =
      """
        |test.db.default {
        |  url = "jdbc:postgresql://localhost:5432/buddho"
        |  user = sa
        |  password = sa
        |  driver = org.postgresql.Driver
        |
        |  flyway.locations = [db/migration/test]
        |}
        |
        |flyway {
        |   enabled = true
        |   locations = []
        |   validateOnMigrate = true
        |   encoding = UTF-8
        |   outOfOrder = false
        |   schemas = []
        |   placeholders = {
        |     test = one
        |   }
        |   sqlMigrationPrefix = V
        |}
      """.stripMargin

    val config = FlywayConfig(ConfigFactory.parseString(c).withFallback(ConfigFactory.load()), Some("test"))

//    config.encoding should be ("UTF-8")
//    config.locations should be (List())
//    config.validateOnMigrate should be (true)
//    config.outOfOrder should be (false)
//    config.schemas should be (List())
//    config.placeholders should be (Map("test" -> "one"))
//    config.sqlMigrationPrefix should be ("V")

    val m = config.migrations.head

    m.name should equal ('default)
    m.enabled should be (true)
    m.locations should be (List("db/migration/test"))
    m.database should be (DatabaseConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/buddho",
      Some("sa"),
      Some("sa")))

  }

}
