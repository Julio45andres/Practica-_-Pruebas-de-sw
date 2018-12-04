package v1.sale

import play.api._
import play.api.mvc._
import play.api.libs.json._
import javax.inject.Inject
import play.api.db._
import play.api.Logger
import play.api.data.Forms._
import play.api.data.Form
import java.sql.Timestamp


import models._
import scala.concurrent.{ExecutionContext, Future}

class SaleController @Inject()
(	repo: SaleRepository,
	cc: ControllerComponents)(implicit ec: ExecutionContext)
extends AbstractController(cc) {
	private val logger = Logger(getClass)

	val saleForm: Form[SaleForm] = Form {
		mapping(
			"fecha_venta" -> sqlTimestamp("yyyy-MM-dd'T'HH:mm:ss.SS'Z'"),
			"valor_total" -> longNumber,
			"nombre_cajero" -> nonEmptyText,
			"iva_calculado" -> longNumber
		)(SaleForm.apply)(SaleForm.unapply)
	}
	def process = Action { request =>
		Ok("Venta Insertada")
	}

	def addPerson = Action.async { implicit request => 
		saleForm.bindFromRequest.fold(
			errorForm => {
				Future.successful(Ok("Venta insertada"))
			},
			sale => {
				val newSale = Sale(0, 0, sale.fechaVenta, sale.valorTotal, sale.nombreCajero, sale.ivaCalculado)
				repo.insert(newSale).map { _ => 
					Redirect("/").flashing("success" -> "sale.created")
				}
			}
		)
	}
}

case class SaleForm(fechaVenta: Timestamp,
				valorTotal: Long,
				nombreCajero: String,
				ivaCalculado: Long)