import io.gatling.core.Predef._
import io.gatling.core.feeder.SourceFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

class BasicTest extends Simulation{

  val words: SourceFeederBuilder[String] = csv("pools/999words.csv").random

  val EngWiki: HttpRequestBuilder = http("EngWiki")
    .get("https://en.wikipedia.org/wiki/${eng_word}")


  val BasicScenario: ScenarioBuilder = scenario("BasicScenario")
    .feed(words)
    .exec(EngWiki)

  setUp(
    BasicScenario.inject(
      rampUsersPerSec(0)
        .to(5)
        .during(10 seconds)
    )
  ).maxDuration(60 seconds)

}
