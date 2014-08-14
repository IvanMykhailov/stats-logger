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
    
  implicit val StatsErrorHandler = Macros.handler[StatsError]
  
  implicit val StatsResultHandler = Macros.handler[StatsResult]
  
  implicit val CalculationMetaStatsHandler = Macros.handler[CalculationMetaStats]
  
  implicit val CalculationResultHandler = {
    import slogger.model.specification.BsonHandlers.SpecsBundleHandler
    implicit val internalHandler = Macros.handler[CalculationResult]
    
    new BSONHandler[BSONDocument, CalculationResult] with BSONDocumentWriter[CalculationResult] with BSONDocumentReader[CalculationResult]{
      def read(bson: BSONDocument): CalculationResult = internalHandler.read(bson)      
      def write(e: CalculationResult): BSONDocument = internalHandler.write(e) ++ BSONDocument("_id" -> e.bundle.id) 
    }
  }
}


object BsonHandlers extends BsonHandlers