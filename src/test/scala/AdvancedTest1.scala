import io.gatling.core.Predef._
import io.gatling.core.feeder.SourceFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._
import scala.util.Random

class AdvancedTest1 extends Simulation{

  val httpConf:HttpProtocolBuilder = http
    .check(status in 200)
    .disableFollowRedirect
    .disableCaching
    .baseUrl("http://localhost:5000")

  val adds: SourceFeederBuilder[String] = csv("pools/adds.csv").random
  val olds: SourceFeederBuilder[String] = csv("pools/olds.csv").circular

  val GetByName: HttpRequestBuilder = http("GetByName")
    .get("/get/name")
    .queryParam("name", "${name}")

  val GetByEmail: HttpRequestBuilder = http("GetByEmail")
    .get("/get/email")
    .queryParam("email", "${email}")
    .check(jsonPath("$.name") saveAs "name")
    .check(jsonPath("$.email") is "${email}")

  val Add: HttpRequestBuilder = http("Add")
    .post("/add")
    .body(ElFileBody("templates/add.json")).asJson


  val AdvancedScenario1: ScenarioBuilder = scenario("AdvancedScenario1")
    .group("Open")(
          feed(olds)
          .randomSwitch(
            50.0 -> exec(GetByEmail)
              .doIf(session => session("name").as[String] == "Ivan")(feed(adds).exec(Add)),
            50.0 -> exec(GetByName)
          )
    )


  val AdvancedScenario2: ScenarioBuilder = scenario("AdvancedScenario2")
    .group("Closed")(
      forever(
        pace(2)
          .feed(olds)
          .exec(GetByEmail)
          .exec(GetByName)
          .exec(session => {
            val name = session("name").as[String]
            val surname = session("surname").as[String]
            val new_surname = Random.shuffle((name + surname).toList).mkString("")
            session.set("surname", new_surname)
          })
          .exec(session => {
            val surname = session("surname").as[String]
            session.set("email", surname + "@email.ru")
          })
          .exec(Add)
      )
    )

  setUp(
    AdvancedScenario1.inject(
      incrementUsersPerSec(1) // Double
        .times(5)
        .eachLevelLasting(10 seconds)
        .separatedByRampsLasting(5 seconds)
        .startingFrom(5) // Double
    ).protocols(httpConf),
    AdvancedScenario2.inject(
      incrementConcurrentUsers(1) // Int
        .times(5)
        .eachLevelLasting(10 seconds)
        .separatedByRampsLasting(5 seconds)
        .startingFrom(5) // Int
    ).protocols(httpConf)
  ).maxDuration(1 minute)
    .assertions(
      forAll.failedRequests.percent.lt(5.0),
      details("Closed").responseTime.percentile(.95).lt(1500),
      details("Open / GetByEmail").successfulRequests.percent.gt(99.99)
    )
}
