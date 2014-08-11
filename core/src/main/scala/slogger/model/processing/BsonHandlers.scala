package slogger.model.processing

import slogger.model.specification.aggregation.AggregationSpecs
import slogger.model.specification.extraction.SlicingSpecs
import slogger.model.specification.extraction.ExtractionSpecs
import reactivemongo.bson.Macros
import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONDocumentReader


trait BsonHandlers extends slogger.model.common.BsonHandlers {
   
  implicit val SliceHandler = Macros.handler[Slice]
  
  implicit val SliceResultHandler = Macros.handler[SliceResult]
    
  
  implicit val StatsResultHandler = {
    import slogger.model.specification.BsonHandlers.SpecsBundleHandler
    implicit val internalHandler = Macros.handler[StatsResult]
    
    new BSONHandler[BSONDocument, StatsResult] with BSONDocumentWriter[StatsResult] with BSONDocumentReader[StatsResult]{    
      def copyId(bson: BSONDocument): BSONDocument = {
        val bundle = bson.getAs[BSONDocument]("bundle").get
        val id = bundle.get("id").get
        bson ++ BSONDocument("_id" -> id)        
      }       
      def read(bson: BSONDocument): StatsResult = internalHandler.read(bson)      
      def write(e: StatsResult): BSONDocument = copyId(internalHandler.write(e))
    }
  }
}


object BsonHandlers extends BsonHandlers