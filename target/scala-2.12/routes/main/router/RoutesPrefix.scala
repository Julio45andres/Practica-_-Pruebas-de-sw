// @GENERATOR:play-routes-compiler
// @SOURCE:/home/julio/dev/play-scala-rest-api-example/conf/routes
// @DATE:Mon Dec 03 13:17:53 COT 2018


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
