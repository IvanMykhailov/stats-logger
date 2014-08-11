package slogger.model


trait BsonHandlers 
  extends slogger.model.common.BsonHandlers
  with slogger.model.specification.BsonHandlers 
  with slogger.model.processing.BsonHandlers {

}

object BsonHandlers extends BsonHandlers