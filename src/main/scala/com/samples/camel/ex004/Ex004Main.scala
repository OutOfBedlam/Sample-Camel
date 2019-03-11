package com.samples.camel.ex004

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.{DefaultCamelContext, SimpleRegistry}
import org.h2.jdbcx.JdbcDataSource

object Ex004Main extends App {

  val basedir = "/tmp/camel_example/data"

  // 🅐 h2 database setup
  val ds = new JdbcDataSource
  ds.setURL(s"jdbc:h2:$basedir/h2db;mode=MySQL")
  ds.setUser("sa")
  ds.setPassword("sa")

  // create table and insert test data
  val conn = ds.getConnection
  val stmt1 = conn.createStatement()
  stmt1.executeUpdate(
    """
      |create table if not exists test_msg(
      |   ID INT PRIMARY KEY,
      |   SUBJECT VARCHAR(80),
      |   BODY VARCHAR(1000),
      |   EMAIL VARCHAR(40),
      |   STATUS CHAR(1)
      |)
    """.stripMargin)

  val data = Seq(
    (1, "Mail for Smith", "Dear Smith,\n This is a notification email",
      "smith@email.com", "R"),
    (2, "Mail for Marry", "Dear Marry,\n This is a notification email",
      "marry@email.com", "R"),
    (3, "Mail for John", "Dear John,\n This is a notification email",
      "john@email.com", "R"),
  )
  data.foreach{ r =>
    val stmt = conn.prepareStatement("INSERT INTO test_msg VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = 'R'")
    stmt.setInt(1, r._1)
    stmt.setString(2, r._2)
    stmt.setString(3, r._3)
    stmt.setString(4, r._4)
    stmt.setString(5, r._5)
    stmt.executeUpdate()
  }
  conn.close()

  // 🅑 register DataSource so that Camel route can find/use database
  val registry = new SimpleRegistry
  registry.put("ds", ds)

  // 🅒 camel context & routes
  val context = new DefaultCamelContext(registry)
  context.addRoutes(new RouteBuilder() {
    override def configure(): Unit = {
      val sqlSelect = "select * from test_msg where status = 'R'"
      val sqlUpdate = "update test_msg set status = :#${in.header.status} where id = :#${in.header.id}"
      from(s"sql:$sqlSelect?dataSource=ds&maxMessagesPerPoll=1&delay=3s") // 🅓
        .process{ exchange =>  // 🅔
          val rs = exchange.getIn.getBody.asInstanceOf[java.util.Map[String, AnyRef]]
          val id = rs.get("ID").asInstanceOf[Int]
          val subject = rs.get("SUBJECT").asInstanceOf[String]
          val body = rs.get("BODY").asInstanceOf[String]
          val email = rs.get("EMAIL").asInstanceOf[String]
          val status = rs.get("STATUS").asInstanceOf[String]

          println(s"===========> $id, $subject, $body, $email, $status")
          // processing the record
          // ......
          // then send the result to the sql producer
          exchange.getOut.setHeader("status", "X")
          exchange.getOut.setHeader("id", id)
        }
        .to(s"sql:$sqlUpdate?dataSource=ds", "log:samples.Ex004Main") // 🅕
    }
  })

  context.start()
  Thread.sleep(10000)
  context.stop()
}
