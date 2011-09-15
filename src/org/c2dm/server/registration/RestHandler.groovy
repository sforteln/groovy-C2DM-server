package org.c2dm.server.registration

@GrabResolver(name='restlet.org', root='http://maven.restlet.org')
@Grab(group='org.restlet.jse', module='org.restlet', version='2.0.9')
@GrabExclude("xerces#xercesImpl")
import org.c2dm.server.C2DMServer
import java.util.Map
import org.restlet.*
import org.restlet.data.*

/**
 * Rest listener that listens for messages from your app 
 * just like a full fledged C2DM 3rd party server
 * 
 * Groovy REST server based on : 
 * http://blog.arc90.com/2008/06/04/building-restful-web-apps-with-groovy-and-restlet-part-1-up-and-running/
 */
public class RestHandler  implements RegistrationHandler{
	public final static String PORT = "port"
	public final static String REST_REQUEST_HANDLER ="restRequestHandler"
	public final static String SHOW_REQUEST = "showRequst"
	def restServer
	def config
	def c2dmServer
	def start(Map config, C2DMServer c2dmServer) {
		this.c2dmServer = c2dmServer
		this.config = config
		/* Create a new HTTP Server on port config[PORT], pass it a new instance of RequestHandler,
		 to which it will pass all incoming Requests, and start it. */
		restServer = new Server(Protocol.HTTP, Integer.valueOf(config[PORT]), new Handler(c2dmServer, {
		}))
		restServer.start()
	}
	def stop(){
		restServer.stop()
	}

	/**
	 * Rest server that listens for messages from the android application.  
	 *
	 */
	private class Handler extends Restlet{
		def c2dmServer
		def registrationExtractor
		public Handler( C2DMServer c2dmServer, Closure registrationExtractor){
			this.c2dmServer= c2dmServer
			this.registrationExtractor
		}

		// handle() is called by the framework whenever there's a HTTP request
		def void handle(Request request, Response response){
			def reqBody = request.getEntity().getText()
			if(config[SHOW_REQUEST]){
				println "Request received\nmethod ${request.method}\nbody ${reqBody}"
			}
			//only process POST requests
			if (request.method == Method.POST){
				/*
				 * Pass the request's body into the user defined 
				 * closure, that is stored in config[REST_REQUEST_HANDLER],
				 *  and capture the return value which should be either the reg id or null
				 */
				def regId = config[REST_REQUEST_HANDLER](reqBody )
				if(regId){
					println "Registration id received from device over REST \n${regId}"
					c2dmServer.setRegistrationId(regId)
				}else {
				    println "Request did not contain a registration id.  Leaving old registration id inplace.  request body\n${reqBody}"
				}
			}else{
				println "Received request for the unsupported method[${request.method}] only suported method POST"
				// The request method is not POST, so set an error response status
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED)
				response.setAllowedMethods([Method.POST]as Set)
			}
		}
	}

}
