package slogger.model.common

import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONDocument
import java.util.UUID
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONValue
import play.api.libs.json.Format
import com.github.nscala_time.time.Imports._
import play.modules.reactivemongo.json.ImplicitBSONHandlers._


trait BsonHandlers {
  
  implicit def MapHandler[T](implicit valueHandler: BSONHandler[_ <: BSONValue, T]): BSONHandler[BSONDocument, Map[String, T]] =
    new BSONHandler[BSONDocument, Map[String, T]] {
      def read(doc: BSONDocument): Map[String, T] = {
        doc.elements.collect {
          case (key, value) => value.seeAsOpt[T](valueHandler) map {
            ov => (key, ov)
          }
        }.flatten.toMap
      }
      def write(doc: Map[String, T]): BSONDocument = {
        BSONDocument(doc.toTraversable map (t => (t._1, valueHandler.write(t._2))))
      }
    }
  
  
  implicit val BigDecimalHandler = new BSONHandler[BSONString, BigDecimal] {
    def write(bigDecimal: BigDecimal) = BSONString(bigDecimal.toString)
    def read(doc: BSONString) = BigDecimal.apply(doc.value)
  }
  
  
  
  implicit val EntityIdUuidFandler = new BSONHandler[BSONString, UUID] {
    def read(bson: BSONString): UUID = UUID.fromString(bson.value)    
    def write(uuid: UUID): BSONString = BSONString(uuid.toString())
  }
  
  
  def jsonFormatToBsonHandler[T](format: Format[T]): BSONHandler[BSONDocument, T] = {    
    new BSONHandler[BSONDocument, T] {
      def read(bson: BSONDocument): T = format.reads(JsValueReader.read(bson)).get
      def write(o: T): BSONDocument = JsValueWriter.write(format.writes(o))
    }
  }
  
  
  implicit val DurationHandler: BSONHandler[BSONDocument, Duration] =
    new BSONHandler[BSONDocument, Duration] {
      def read(doc: BSONDocument): Duration = new Duration(doc.getAs[Long]("millis").get)
      def write(d: Duration): BSONDocument = BSONDocument(
        "millis" -> d.getMillis()    
      )
    }
  
  
  implicit val JodaDateTimeFandler = new BSONHandler[BSONDateTime, DateTime] {
    def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)    
    def write(dt: DateTime): BSONDateTime = BSONDateTime(dt.getMillis())
  }
}


object BsonHandlers extends BsonHandlers