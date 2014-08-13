package slogger.model.processing

import play.api.libs.json.JsValue


class AggregationException(msg: String, val errorDocument: Option[JsValue] = None) extends RuntimeException(msg) {

}