package v1.product

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class ProductData(id: ProductId, producto: String, valorUnitario: Int, iva: String)

class ProductId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object ProductId {
  def apply(raw: String): ProductId = {
    require(raw != null)
    new ProductId(Integer.parseInt(raw))
  }
}


class ProductExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the ProductRepository.
  */
trait ProductRepository {
  def create(data: ProductData)(implicit mc: MarkerContext): Future[ProductId]

  def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]]

  def get(id: ProductId)(implicit mc: MarkerContext): Future[Option[ProductData]]
}

/**
  * A trivial implementation for the Product Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class ProductRepositoryImpl @Inject()()(implicit ec: ProductExecutionContext) extends ProductRepository {

  private val logger = Logger(this.getClass)

  private val productList = List(
    ProductData(ProductId("1"),  "Bolsa de regalo",     500,    "19"),
    ProductData(ProductId("2"),  "Botella deportiva",   50000,  "19"),
    ProductData(ProductId("3"),  "Pañuelo facial",      5000,   "19"),
    ProductData(ProductId("4"),  "Pan perro",           3500,   "0"),
    ProductData(ProductId("5"),  "Pan tajado",          4500,   "0"),
    ProductData(ProductId("6"),  "Mermelada",           7800,   "0"),
    ProductData(ProductId("7"),  "Arroz",               1400,   "0"),
    ProductData(ProductId("8"),  "Acondicionador",      600,    "19"),
    ProductData(ProductId("9"),  "Pañales Etapa2 x 30", 47000,  "5"),
    ProductData(ProductId("10"), "Banano criollo",      500,    "0"),
    ProductData(ProductId("11"), "Crema dental x 3",    3800,   "19"),
    ProductData(ProductId("12"), "Te helado",           2900,   "19"),
    ProductData(ProductId("13"), "Galleta x 3 tacos",   3500,   "19"),
    ProductData(ProductId("14"), "Cereal",              1100,   "19"),
    ProductData(ProductId("15"), "Shampoo",             8000,   "19"),
    ProductData(ProductId("16"), "Pasta larga",         2500,   "5"),
    ProductData(ProductId("17"), "Avena 500g",          3600,   "5"),
    ProductData(ProductId("18"), "Mantequilla",         4200,   "0"),
    ProductData(ProductId("19"), "Cuajada",             3000,   "0"),
    ProductData(ProductId("20"), "Servilletas x 100",   2700,   "5")
  )

  override def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]] = {
    Future {
      logger.trace(s"list: ")
      productList
    }
  }

  override def get(id: ProductId)(implicit mc: MarkerContext): Future[Option[ProductData]] = {
    Future {
      logger.trace(s"get: id = $id")
      productList.find(product => product.id == id)
    }
  }

  def create(data: ProductData)(implicit mc: MarkerContext): Future[ProductId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }
}
