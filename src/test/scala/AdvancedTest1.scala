import io.gatling.core.Predef._
import io.gatling.core.feeder.SourceFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

class AdvancedTest1 extends Simulation{

  val httpConf:HttpProtocolBuilder = http
    .check(status in 200)
    .disableFollowRedirect
    .disableCaching
    .baseUrl("http://localhost:5000")

  val adds: SourceFeederBuilder[String] = csv("pools/adds.csv").random
  val olds: SourceFeederBuilder[String] = csv("pools/olds.csv").random

  val GetByName: HttpRequestBuilder = http("GetByName")
    .get("/get/name")
    .queryParam("name", "${name}")

  val GetByEmail: HttpRequestBuilder = http("GetByEmail")
    .get("/get/email")
    .queryParam("email", "${email}")

  val Add: HttpRequestBuilder = http("Add")
    .post("/add")
    .body(ElFileBody("templates/add.json")).asJson


  val AdvancedScenario: ScenarioBuilder = scenario("AdvancedScenario")
      .randomSwitch(
        0.01 -> feed(adds).exec(Add),
        99.99 -> exec(
          feed(olds)
          .randomSwitch(
            50.0 -> exec(GetByEmail).randomSwitch(1.0 -> feed(adds).exec(Add)),
            50.0 -> exec(GetByName).randomSwitch(30.0 -> exec(GetByEmail).randomSwitch(20.0 -> feed(adds).exec(Add)))
          )
        )
      )


  setUp(
    AdvancedScenario.inject(
      rampUsersPerSec(0) to 1 during (10 seconds),
      constantUsersPerSec(1) during (5 minutes)
    ).protocols(httpConf)
  ).maxDuration(6 minutes)
}
