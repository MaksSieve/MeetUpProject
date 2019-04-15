import io.gatling.core.Predef._
import io.gatling.core.feeder.SourceFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

class BasicTest extends Simulation{

  val httpConf:HttpProtocolBuilder = http
    .baseUrl("https://en.wiktionary.org/wiki/")

  val words: SourceFeederBuilder[String] = csv("pools/1000words.csv").random

  val WiktionaryQuestion: HttpRequestBuilder = http("WiktionaryQuestion")
    .get("${word}")

  val WikiQuestion: HttpRequestBuilder = http("WikiQuestion")
    .get("https://en.wikipedia.org/wiki/${word}")


  val BasicScenario: ScenarioBuilder = scenario("BasicScenario")
    .forever(
      pace(1 second)
      .feed(words)
      .group("Wiki")(
        exec(WiktionaryQuestion)
          .exec(WikiQuestion)
      )
    )

  setUp(
    BasicScenario.inject(
      rampConcurrentUsers(0)
        .to(5)
        .during(10 seconds)
    ).protocols(httpConf)
  ).maxDuration(190 seconds)

}
