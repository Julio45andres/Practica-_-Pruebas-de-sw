package v1.product

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * DTO for displaying product information.
  */
case class ProductResource(id: String, link: String, producto: String, valorUnitario: Int, body: String)

object ProductResource {

  /**
    * Mapping to write a ProductResource out as a JSON value.
    */
  implicit val implicitWrites = new Writes[ProductResource] {
    def writes(product: ProductResource): JsValue = {
      Json.obj(
        "codigo" -> product.id,
        "enlace" -> product.link,
        "producto" -> product.producto,
        "valor_unitario" -> product.valorUnitario,
        "iva" -> product.body
      )
    }
  }
}

/**
  * Controls access to the backend data, returning [[ProductResource]]
  */
class ProductResourceHandler @Inject()(
    routerProvider: Provider[ProductRouter],
    productRepository: ProductRepository)(implicit ec: ExecutionContext) {

  def create(productInput: ProductFormInput)(implicit mc: MarkerContext): Future[ProductResource] = {
    val data = ProductData(ProductId("999"), productInput.title, productInput.valorUnitario, productInput.body)
    // We don't actually create the product, so return what we have
    productRepository.create(data).map { id =>
      createProductResource(data)
    }
  }

  def lookup(id: String)(implicit mc: MarkerContext): Future[Option[ProductResource]] = {
    val productFuture = productRepository.get(ProductId(id))
    productFuture.map { maybeProductData =>
      maybeProductData.map { productData =>
        createProductResource(productData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[ProductResource]] = {
    productRepository.list().map { productDataList =>
      productDataList.map(productData => createProductResource(productData))
    }
  }

  private def createProductResource(p: ProductData): ProductResource = {
    ProductResource(p.id.toString, routerProvider.get.link(p.id), p.producto, p.valorUnitario, p.iva)
  }

}
