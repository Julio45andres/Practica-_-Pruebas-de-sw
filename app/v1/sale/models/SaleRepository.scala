package v1.sale.models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Timestamp
import scala.concurrent.{ Future, ExecutionContext }
import java.util.Date

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */

@Singleton
class SaleRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
	private val dbConfig = dbConfigProvider.get[JdbcProfile]

	// These imports are important, the first one brings db into scope, which will let you do the actual db operations.
	// The second one brings the Slick DSL into scope, which lets you define the table and other queries.
	import dbConfig._
	import profile.api._

	private class SaleTable(tag: Tag) extends Table[Sale](tag, "sale"){
		def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

		def numeroFactura = column[Long]("numero_factura", O.AutoInc)

		def fechaVenta = column[Timestamp]("fecha_venta", O.Default(new Timestamp(System.currentTimeMillis())))

		def valorTotal = column[Long]("valor_total")
 
		def nombreCajero = column[String]("nombre_cajero")

		def ivaCalculado = column[Long]("iva_calculado")

		def * = (id, numeroFactura, fechaVenta, valorTotal, nombreCajero, ivaCalculado) <> ((Sale.apply _).tupled, Sale.unapply)
	}

	private val sales = TableQuery[SaleTable]

	def list(): Future[Seq[Sale]] = db.run {
		sales.result
	}

	def insert(sale: Sale) = db.run(sales returning sales.map(_.id) += sale)
		.map(id => sale.copy(id = id))

	// val insertSale(numeroFactura: Long, fechaVenta: Timestamp, valorTotal: Long, nombreCajero: String, ivaCalculado: Long) = DBIO.seq(
	// 	sales.map(s => (s.numeroFactura, s.fechaVenta, s.valorTotal, s.nombreCajero, s.ivaCalculado)) += (numeroFactura, fechaVenta, valorTotal, nombreCajero, ivaCalculado)
	// )
	// val sql = sales.insertStatement
	
	// def insertSale(numeroFactura: Long, fechaVenta: Timestamp, valorTotal: Long, nombreCajero: String, ivaCalculado: Long): Future[Int] = db.run {
	// 	sales.map(s => (s.numeroFactura, s.fechaVenta, s.valorTotal, s.nombreCajero, s.ivaCalculado)) += (numeroFactura, fechaVenta, valorTotal, nombreCajero, ivaCalculado)
	// }
}