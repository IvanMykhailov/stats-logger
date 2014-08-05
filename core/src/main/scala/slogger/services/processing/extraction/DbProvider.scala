package slogger.services.processing.extraction

import reactivemongo.api.DB
import reactivemongo.api.MongoDriver
import reactivemongo.api.MongoConnection


trait DbProvider {
  
  def driver: MongoDriver
  
  def connection: MongoConnection
  
  def db: DB
}


class DirectMongoDbProvider(dbName: String, hosts: Seq[String]) extends DbProvider {
  import reactivemongo.api._
  import scala.concurrent.ExecutionContext.Implicits.global
  
  val driver: MongoDriver = new MongoDriver
  
  val connection: MongoConnection = driver.connection(hosts)
  
  val db: DB = connection(dbName)
  
}