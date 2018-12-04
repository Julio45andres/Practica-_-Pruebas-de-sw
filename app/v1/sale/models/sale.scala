package v1.sale.models

import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json._

case class Sale(id: Long,
				numeroFactura: Long,
				fechaVenta: Timestamp,
				valorTotal: Long,
				nombreCajero: String,
				ivaCalculado: Long)

object Sale {

	implicit object timestampFormat extends Format[Timestamp] {
		val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
		def reads(json: JsValue) = {
			val str = json.as[String]
			JsSuccess(new Timestamp(format.parse(str).getTime))
		}
		def writes(ts: Timestamp) = JsString(format.format(ts))
	}

  implicit val saleFormat = Json.format[Sale]
}