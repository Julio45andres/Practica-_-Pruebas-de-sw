# Basics

This guide will walk you through how to make a REST API with JSON using [Play Framework](https://playframework.com).

To see the associated Github project, please go to <https://github.com/playframework/play-scala-rest-api-example> or clone the project:

```bash
git clone https://github.com/playframework/play-scala-rest-api-example.git
```

We're going to be showing an already working Play project with most of the code available under the `app/v1` directory.  There will be several different versions of the same project as this series expands, so you can compare different versions of the project against each other.

To run Play on your own local computer, please see the instructions in the @ref[appendix](../appendix.md).

## Introduction

We'll start off with a REST API that displays information for blog products.  Users should be able to write a title and a body of a blog product and create new blog products, edit existing blog products, and delete new blog products.

## Modelling a Product Resource

The way to do this in REST is to model the represented state as a resource.  A blog product resource will have a unique id, a URL hyperlink that indicates the canonical location of the resource, the title of the blog product, and the body of the blog product.

This resource is represented as a single case class in the Play application [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductResourceHandler.scala#L13):

```scala
case class ProductResource(
  id: String,
  link: String,
  title: String,
  body: String
)
```

This resource is mapped to and from JSON on the front end using Play, and is mapped to and from a persistent datastore on the backend using a handler.

Play handles HTTP routing and representation for the REST API and makes it easy to write a non-blocking, asynchronous API that is an order of magnitude more efficient than other web application frameworks.

## Routing Product Requests

Play has two complimentary routing mechanisms.  In the conf directory, there's a file called "routes" which contains entries for the HTTP method and a relative URL path, and points it at an action in a controller.

```
GET    /               controllers.HomeController.index()
```

This is useful for situations where a front end service is rendering HTML.  However, Play also contains a more powerful routing DSL that we will use for the REST API.

For every HTTP request starting with `/v1/products`, Play routes it to a dedicated `ProductRouter` class to handle the Products resource, through the [`conf/routes`](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/conf/routes) file:

```
->     /v1/products               v1.product.ProductRouter
```

The `ProductRouter` examines the URL and extracts data to pass along to the controller [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductRouter.scala):

```scala
package v1.product

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ProductRouter @Inject()(controller: ProductController) extends SimpleRouter {
  val prefix = "/v1/products"

  def link(id: ProductId): String = {
    import com.netaporter.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case GET(p"/$id") =>
      controller.show(id)
  }

}
```

Play’s [routing DSL](https://www.playframework.com/documentation/latest/ScalaSirdRouter) (technically "String Interpolation Routing DSL", aka SIRD) shows how data can be extracted from the URL concisely and cleanly.  SIRD is based around HTTP methods and a string interpolated extractor object – this means that when we type the string “/$id” and prefix it with “p”, then the path parameter id can be extracted and used in the block. Naturally, there are also operators to extract queries, regular expressions, and even add custom extractors.  If you have a URL as follows:

```
/products/?sort=ascending&count=5
```

Then you can extract the "sort" and "count" parameters in a single line:

```scala
GET("/" ? q_?"sort=$sort" & q_?”count=${ int(count) }")
```

SIRD is especially useful in a REST API where there can be many possible query parameters. Cake Solutions covers SIRD in more depth in a [fantastic blog product](http://www.cakesolutions.net/teamblogs/all-you-need-to-know-about-plays-routing-dsl).

## Using a Controller

The `ProductRouter` has a `ProductController` injected into it through standard [JSR-330 dependency injection](https://github.com/google/guice/wiki/JSR330) [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductRouter.scala#L12):

```scala
class ProductRouter @Inject()(controller: ProductController) extends SimpleRouter
```

Before heading into the `ProductController`, let's discuss how controllers work in Play.

A controller [handles the work of processing](https://www.playframework.com/documentation/latest/ScalaActions)  the HTTP request into an HTTP response in the context of an Action: it's where page rendering and HTML form processing happen.  A controller extends [`play.api.mvc.BaseController`](https://www.playframework.com/documentation/latest/api/scala/index.html#play.api.mvc.BaseController), which contains a number of utility methods and constants for working with HTTP.  In particular, a `Controller` contains `Result` objects such as `Ok` and `Redirect`, and `HeaderNames` like `ACCEPT`.

The methods in a controller consist of a method returning an [Action](https://www.playframework.com/documentation/latest/api/scala/index.html#play.api.mvc.Action).  The Action provides the "engine" to Play.

Using the action, the controller passes in a block of code that takes a [`Request`](https://www.playframework.com/documentation/latest/api/scala/index.html#play.api.mvc.Request) passed in as implicit – this means that any in-scope method that takes an implicit request as a parameter will use this request automatically.  Then, the block must return either a [`Result`](https://www.playframework.com/documentation/latest/api/scala/index.html#play.api.mvc.Result), or a [`Future[Result]`](http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future), depending on whether or not the action was called as `action { ... }` or [`action.async { ... }`](https://www.playframework.com/documentation/latest/ScalaAsync#How-to-create-a-Future[Result]).

### Handling GET Requests

Here's a simple example of a Controller:

```scala
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent._

class MyController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index1: Action[AnyContent] = Action { implicit request =>
    val r: Result = Ok("hello world")
    r
  }

  def asyncIndex: Action[AnyContent] = Action.async { implicit request =>
    val r: Future[Result] = Future.successful(Ok("hello world"))
    r
  }
}
```

In this example, `index1` and `asyncIndex` have exactly the same behavior.  Internally, it makes no difference whether we call `Result` or `Future[Result]` -- Play is non-blocking all the way through.

However, if you're already working with `Future`, async makes it easier to pass that `Future` around. You can read more about this in the [handling asynchronous results](https://www.playframework.com/documentation/latest/ScalaAsync) section of the Play documentation.

The ProductController methods dealing with GET requests is [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductController.scala). Let's take a look at the most important parts:

```scala
package v1.product

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

class ProductController @Inject()(cc: ProductControllerComponents)(implicit ec: ExecutionContext)
  extends ProductBaseController(cc) {

  def index: Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace("index: ")
    productResourceHandler.find.map { products =>
      Ok(Json.toJson(products))
    }
  }

  def show(id: String): Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace(s"show: id = $id")
    productResourceHandler.lookup(id).map { product =>
      Ok(Json.toJson(product))
    }
  }
}
```

Let's take `show` as an example.  Here, the action defines a workflow for a request that maps to a single resource, i.e. `GET /v1/products/123`.

```scala
def show(id: String): Action[AnyContent] = ProductAction.async { implicit request =>
  logger.trace(s"show: id = $id")
  productResourceHandler.lookup(id).map { product =>
    Ok(Json.toJson(product))
  }
}
```

The `id` is passed in as a `String`, and the handler looks up and returns a `ProductResource`.  The `Ok()` sends back a `Result` with a status code of "200 OK", containing a response body consisting of the `ProductResource` serialized as JSON.

### Processing Form Input

Handling a `POST` request is also easy and is done through the `process` method:

```scala
private val form: Form[ProductFormInput] = {
  import play.api.data.Forms._

  Form(
    mapping(
      "title" -> nonEmptyText,
      "body" -> text
    )(ProductFormInput.apply)(ProductFormInput.unapply)
  )
}

def process: Action[AnyContent] = ProductAction.async { implicit request =>
  logger.trace("process: ")
  processJsonProduct()
}

private def processJsonProduct[A]()(implicit request: ProductRequest[A]): Future[Result] = {
  def failure(badForm: Form[ProductFormInput]) = {
    Future.successful(BadRequest(badForm.errorsAsJson))
  }

  def success(input: ProductFormInput) = {
    productResourceHandler.create(input).map { product =>
      Created(Json.toJson(product)).withHeaders(LOCATION -> product.link)
    }
  }

  form.bindFromRequest().fold(failure, success)
}
```

Here, the `process` action is an action wrapper, and `processJsonProduct` does most of the work.  In `processJsonProduct`, we get to the [form processing](https://www.playframework.com/documentation/latest/ScalaForms) part of the code.

Here, `form.bindFromRequest()` will map input from the HTTP request to a [`play.api.data.Form`](https://www.playframework.com/documentation/latest/api/scala/index.html#play.api.data.Form), and handles form validation and error reporting.

If the `ProductFormInput` passes validation, it's passed to the resource handler, using the `success` method.  If the form processing fails, then the `failure` method is called and the `FormError` is returned in JSON format.

```scala
private val form: Form[ProductFormInput] = {
  import play.api.data.Forms._

  Form(
    mapping(
      "title" -> nonEmptyText,
      "body" -> text
    )(ProductFormInput.apply)(ProductFormInput.unapply)
  )
}
```

The form binds to the HTTP request using the names in the mapping -- `title` and `body` to the `ProductFormInput` case class [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductController.scala#L12).

```scala
case class ProductFormInput(title: String, body: String)
```

That's all you need to do to handle a basic web application!  As with most things, there are more details that need to be handled.  That's where creating custom Actions comes in.

## Using Actions

We saw in the `ProductController` that each method is connected to an Action through the `ProductAction.async` method [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductController.scala#L33):

```scala
def index: Action[AnyContent] = ProductAction.async { implicit request =>
  logger.trace("index: ")
  productResourceHandler.find.map { products =>
    Ok(Json.toJson(products))
  }
}
```

The `ProductAction.async` is a [custom action builder](https://www.playframework.com/documentation/2.6.x/ScalaActionsComposition#Custom-action-builders) defined [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductActionBuilder.scala#L49-L53) that can handle `ProductRequest`s (see definition [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductActionBuilder.scala#L20)):

`ProductAction` is involved in each action in the controller -- it mediates the paperwork involved with processing a request into a response, adding context to the request and enriching the response with headers and cookies.  ActionBuilders are essential for handling authentication, authorization and monitoring functionality.

ActionBuilders work through a process called [action composition](https://www.playframework.com/documentation/latest/ScalaActionsComposition).  The ActionBuilder class has a method called `invokeBlock` that takes in a `Request` and a function (also known as a block, lambda or closure) that accepts a `Request` of a given type, and produces a `Future[Result]`.

So, if you want to work with an `Action` that has a "FooRequest" that has a Foo attached, it's easy:

```scala
class FooRequest[A](request: Request[A], val foo: Foo) extends WrappedRequest(request)

class FooAction @Inject()(parsers: PlayBodyParsers)(implicit val executionContext: ExecutionContext) extends ActionBuilder[FooRequest, AnyContent] {

  type FooRequestBlock[A] = FooRequest[A] => Future[Result]

  override def parser: BodyParser[AnyContent] = parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: FooRequestBlock[A]): Future[Result] = {
    block(new FooRequest[A](request, Foo()))
  }
}
```

You create an `ActionBuilder[FooRequest, AnyContent]`, override `invokeBlock`, and then call the function with an instance of `FooRequest`.

Then, when you call `fooAction`, the request type is `FooRequest`:

```scala
fooAction { request: FooRequest =>
  Ok(request.foo.toString)
}
```

And `request.foo` will be added automatically.

You can keep composing action builders inside each other, so you don't have to layer all the functionality in one single ActionBuilder, or you can create a custom `ActionBuilder` for each package you work with, according to your taste.  For the purposes of this blog product, we'll keep everything together in a single class.

You can see `ProductAction` builder [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductActionBuilder.scala#L49-L78):

```scala
trait ProductRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class ProductRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with ProductRequestHeader

class ProductActionBuilder @Inject()(messagesApi: MessagesApi, playBodyParsers: PlayBodyParsers)
                                 (implicit val executionContext: ExecutionContext)
    extends ActionBuilder[ProductRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {


  val parser: BodyParser[AnyContent] = playBodyParsers.anyContent


  type ProductRequestBlock[A] = ProductRequest[A] => Future[Result]


  private val logger = Logger(this.getClass)


  override def invokeBlock[A](request: Request[A],
                              block: ProductRequestBlock[A]): Future[Result] = {
    // Convert to marker context and use request in block
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(request)
    logger.trace(s"invokeBlock: ")


    val future = block(new ProductRequest(request, messagesApi))


    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }
}
```

`ProductAction` does a couple of different things here.  The first thing it does is to log the request as it comes in.  Next, it pulls out `MessagesApi` for the request, and adds that to a `ProductRequest` , and runs the function, returning a `Future[Result]`.

When the future completes, we map the result so we can replace it with a slightly different result.  We compare the result's method against `HttpVerbs`, and if it's a GET or HEAD, we append a `Cache-Control` header with a `max-age` directive.  We need an `ExecutionContext` for `future.map` operations, so we pass in the default execution context implicitly at the top of the class.

Now that we have a `ProductRequest`, we can call "request.messagesApi" explicitly from any action in the controller, for free, and we can append information to the result after the user action has been completed.

## Converting resources with ProductResourceHandler

The `ProductResourceHandler` is responsible for converting backend data from a repository into a `ProductResource`. We won't go into detail on the `ProductRepository` details for now, only that it returns data in an backend-centric state.

A REST resource has information that a backend repository does not -- it knows about the operations available on the resource, and contains URI information that a single backend may not have.  As such, we want to be able to change the representation that we use internally without changing the resource that we expose publicly.

You can see the `ProductResourceHandler` [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductResourceHandler.scala#L35-L66):

```scala
class ProductResourceHandler @Inject()(
    routerProvider: Provider[ProductRouter],
    productRepository: ProductRepository)(implicit ec: ExecutionContext) {


  def create(productInput: ProductFormInput)(implicit mc: MarkerContext): Future[ProductResource] = {
    val data = ProductData(ProductId("999"), productInput.title, productInput.body)
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
    ProductResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }

}
```
Here, it's a straight conversion in `createProductResource`, with the only hook being that the router provides the resource's URL, since it's something that `ProductData` does not have itself.

## Rendering Content as JSON

Play handles the work of converting a `ProductResource` through [Play JSON](https://www.playframework.com/documentation/latest/ScalaJson). Play JSON provides a DSL that looks up the conversion for the `ProductResource` singleton object, so you don't need to declare it at the use point.

You can see the `ProductResource` object [here](https://github.com/playframework/play-scala-rest-api-example/blob/2.6.x/app/v1/product/ProductResourceHandler.scala#L15-L30):

```scala
object ProductResource {

  implicit val implicitWrites = new Writes[ProductResource] {
    def writes(product: ProductResource): JsValue = {
      Json.obj(
        "id" -> product.id,
        "link" -> product.link,
        "title" -> product.title,
        "body" -> product.body
      )
    }
  }
}
```

Once the implicit is defined in the companion object, then it will be looked up automatically when handed an instance of the class.  This means that when the controller converts to JSON, the conversion will just work, without any additional imports or setup.

```scala
val json: JsValue = Json.toJson(product)
```

Play JSON also has options to incrementally parse and generate JSON for continuously streaming JSON responses.

## Summary

We've shown how to easy it is to put together a basic REST API in Play.  Using this code, we can put together backend data, convert it to JSON and transfer it over HTTP with a minimum of fuss.

In the next guide, we'll discuss content representation and provide an HTML interface that exists alongside the JSON API.
