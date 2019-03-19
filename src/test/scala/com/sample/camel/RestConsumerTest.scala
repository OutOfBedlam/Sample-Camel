package com.sample.camel

import org.apache.camel._
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.impl.JndiRegistry
import org.apache.camel.model.rest.RestBindingMode
import org.apache.camel.test.junit4.CamelTestSupport
import org.junit.Test

import scala.collection.JavaConverters._

class User(var name: String) {
  def this() = this("")
  def setName(n: String): Unit = name = n
  def getName: String = name
}

class UserService {
  def user(id: Int): User = new User(s"Hong Gil Dong - $id")
  def delete(id: Int): String = s"Delete-${id}"
  def update(user: User): User = user
}

/**
  * 1. Start api server on 8080 port
  * 2. curl -o - --get http://localhost:8080/user/view/1234
  * 3. curl -o - -H "Content-Type: application/json" --data '{"name": "Hong Gil Dong"}' http://localhost:8080/user/view
  */
class RestConsumerTest extends CamelTestSupport {

  @EndpointInject(uri = "mock:ignore")
  protected var resultEndpoint: MockEndpoint = _

  override def createRegistry(): JndiRegistry = {
    val reg = super.createRegistry()
    reg.bind("userService", new UserService)
    reg
  }

  override def createRouteBuilder(): RoutesBuilder = new RouteBuilder(context){
    override def configure(): Unit = {
      restConfiguration().component("spark-rest").port(8080)
        .bindingMode(RestBindingMode.json)
        .dataFormatProperty("prettyPrint", "true")

      rest("/user").get("/view/{id}").outType(classOf[User])
        .route().to("bean:userService?method=user(${header.id})", "mock:ignore")
        .endRest()

      rest("/user").post("/view").`type`(classOf[User]).outType(classOf[String])
        .route().to("bean:userService?method=update", "mock:ignore")
        .endRest()
    }
  }

  @Test
  def testSimpleApi(): Unit = {
    val rsp = template.requestBodyAndHeader("http4://localhost:8080/user/view/2345", "",
      Exchange.HTTP_METHOD, "GET", classOf[String])

    assert(rsp == "{\n  \"name\" : \"Hong Gil Dong - 2345\"\n}")

    resultEndpoint.expectedMessageCount(1)
    resultEndpoint.assertIsSatisfied(1000)
  }

  @Test
  def testPostApi(): Unit = {
    val rsp = template.requestBodyAndHeaders(
      "http4://localhost:8080/user/view","""{ "name" : "Hong Gil Dong - 2345" }""",
      Map[String, AnyRef](Exchange.HTTP_METHOD ->"POST", Exchange.CONTENT_TYPE -> "application/json").asJava,
      classOf[String])

    assert(rsp == "{\n  \"name\" : \"Hong Gil Dong - 2345\"\n}")

    resultEndpoint.expectedMessageCount(1)
    resultEndpoint.assertIsSatisfied(1000)
  }
}
