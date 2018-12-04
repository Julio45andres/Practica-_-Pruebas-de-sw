package v1.sale

import javax.inject._

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class SaleRouter @Inject()(controller: SaleController) extends SimpleRouter{
	
	val prefix = "/v1/sales"
	
	override def routes: Routes = {
		case POST(p"/") => 
			controller.addPerson
	}
}