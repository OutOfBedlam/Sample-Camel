package com.samples.camel.ex001

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext

object Ex001Main extends App {
  val context = new DefaultCamelContext()
  context.addRoutes(new RouteBuilder() {
    override def configure(): Unit = {
      // 🅐 Java DSL route
      from("file:data/inbox?noop=true") // 🅑 consumer
        .to("file:data/outbox")         // 🅒 producer
    }
  })
  context.start()
  Thread.sleep(10000)
  context.stop()
}
