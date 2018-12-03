// @GENERATOR:play-routes-compiler
// @SOURCE:/home/julio/dev/play-scala-rest-api-example/conf/routes
// @DATE:Mon Dec 03 13:17:53 COT 2018

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:1
  HomeController_0: controllers.HomeController,
  // @LINE:3
  v1_product_ProductRouter_1: v1.product.ProductRouter,
  // @LINE:5
  v1_sale_SaleRouter_0: v1.sale.SaleRouter,
  // @LINE:8
  Assets_1: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:1
    HomeController_0: controllers.HomeController,
    // @LINE:3
    v1_product_ProductRouter_1: v1.product.ProductRouter,
    // @LINE:5
    v1_sale_SaleRouter_0: v1.sale.SaleRouter,
    // @LINE:8
    Assets_1: controllers.Assets
  ) = this(errorHandler, HomeController_0, v1_product_ProductRouter_1, v1_sale_SaleRouter_0, Assets_1, "/")

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, HomeController_0, v1_product_ProductRouter_1, v1_sale_SaleRouter_0, Assets_1, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.HomeController.index"""),
    prefixed_v1_product_ProductRouter_1_1.router.documentation,
    prefixed_v1_sale_SaleRouter_0_2.router.documentation,
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/""" + "$" + """file<.+>""", """controllers.Assets.at(path:String = "/public", file:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:1
  private[this] lazy val controllers_HomeController_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_HomeController_index0_invoker = createInvoker(
    HomeController_0.index,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "index",
      Nil,
      "GET",
      this.prefix + """""",
      """""",
      Seq()
    )
  )

  // @LINE:3
  private[this] val prefixed_v1_product_ProductRouter_1_1 = Include(v1_product_ProductRouter_1.withPrefix(this.prefix + (if (this.prefix.endsWith("/")) "" else "/") + "v1/products"))

  // @LINE:5
  private[this] val prefixed_v1_sale_SaleRouter_0_2 = Include(v1_sale_SaleRouter_0.withPrefix(this.prefix + (if (this.prefix.endsWith("/")) "" else "/") + "v1/sales"))

  // @LINE:8
  private[this] lazy val controllers_Assets_at3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at3_invoker = createInvoker(
    Assets_1.at(fakeValue[String], fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "at",
      Seq(classOf[String], classOf[String]),
      "GET",
      this.prefix + """assets/""" + "$" + """file<.+>""",
      """ Map static resources from the /public folder to the /assets URL path""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:1
    case controllers_HomeController_index0_route(params@_) =>
      call { 
        controllers_HomeController_index0_invoker.call(HomeController_0.index)
      }
  
    // @LINE:3
    case prefixed_v1_product_ProductRouter_1_1(handler) => handler
  
    // @LINE:5
    case prefixed_v1_sale_SaleRouter_0_2(handler) => handler
  
    // @LINE:8
    case controllers_Assets_at3_route(params@_) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at3_invoker.call(Assets_1.at(path, file))
      }
  }
}
