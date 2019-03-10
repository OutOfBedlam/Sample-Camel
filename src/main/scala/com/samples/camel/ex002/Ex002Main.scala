package com.samples.camel.ex002

import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext

object Ex002Main extends App {
  val context = new DefaultCamelContext()
  context.addRoutes(new RouteBuilder() {
    override def configure(): Unit = {

      from("file:data/inbox?noop=true")
        .process { exchange: Exchange => // 🅐 Define Processor
          val filename = exchange.getIn.getHeader("CamelFileName")  // 🅑
          println(s"=====> processing file: $filename}")
        }
        .log("-----> processing file: ${in.header.CamelFileName}")  // 🅒
        .to("file:data/outbox")
    }
  })
  context.start()
  Thread.sleep(10000)
  context.stop()
}
