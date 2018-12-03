package v1.sale

import play.api._
import play.api.mvc._
import play.api.libs.json._
import javax.inject.Inject

class SaleController @Inject()
(cc: ControllerComponents)
extends AbstractController(cc) {
	def process = Action { request =>
		Ok("Venta Insertada")
	}
}