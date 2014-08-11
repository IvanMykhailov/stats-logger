package slogger.model.specification

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
  
  implicit val SlicingSpecsHandler = Macros.handler[SlicingSpecs]
  
  implicit val TimeLimitsHandler = jsonFormatToBsonHandler(slogger.model.specification.extraction.JsonFormats.TimeLimitsFromat)
  
  implicit val ExtractionSpecsHandle = Macros.handler[ExtractionSpecs]
  
  implicit val AggregationSpecsHandle = Macros.handler[AggregationSpecs]
  
  
  implicit val SpecsBundleHandler = {
    implicit val internalHandler = Macros.handler[SpecsBundle]
    
    new BSONHandler[BSONDocument, SpecsBundle] with BSONDocumentWriter[SpecsBundle] with BSONDocumentReader[SpecsBundle]{    
      def copyId(bson: BSONDocument): BSONDocument = {
        bson ++ BSONDocument("_id" -> bson.get("id").get)        
      }       
      def read(bson: BSONDocument): SpecsBundle = internalHandler.read(bson)      
      def write(e: SpecsBundle): BSONDocument = copyId(internalHandler.write(e))
    }
  }
}


object BsonHandlers extends BsonHandlers