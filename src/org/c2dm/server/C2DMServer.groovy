package org.c2dm.server
@Grapes([
	@Grab("org.codehaus.groovy.modules.http-builder#http-builder;0.5.0-RC2" ),
	@GrabExclude("xerces#xercesImpl")
])
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import java.util.Collections.SynchronizedCollection
import org.c2dm.server.registration.LogHandler
import org.c2dm.server.registration.RegistrationHandler
import org.c2dm.server.registration.RestHandler

/**
 * C2DM Server this is a very simple implementation and really only suitable for testing
 * 
 * Currently in only store a single registration id to use in sending push 
 * notifications to google and so is only suitable for use with a single device.
 *
 */
class C2DMServer {
	//Config keys
	public static String SENDER_EMAIL_KEY ="senderEmail"
	public static String SENDER_PASSWORD_KEY ="senderPassword"
	public static String PUSH_MESSAGE_BUILDER ="pushMessageBuilder"
	
	def config
	def authToken =null
	def registrationId=null
	def regHandler
	
	public C2DMServer(Map config){
		this.config = config		
	}
	
	def start(){
		getSenderAuthToken()
		registerRegistrationHandler()
		println "Ready. Press enter to push a message or Ctrl-c to quit."
		println "If you need to pass data to your message generation code type it in before pressing enter"
		while(true){
			System.in.eachLine{ line ->
				sendToC2DM(line)
			}
		}
		addShutdownHook  {
			println "Shutting down"
			regHandler.stop()
			
		}
	}
	
/*
 * Since a new registration id could come from google at 
 * any time we have the handler running in a separate thread and do 
 * all access through synchronized methods
 */
synchronized getRegistrationId(){
	return registrationId
}
synchronized setRegistrationId(id){
	registrationId=id
}

/**
 * create instance of the registrationHandler and start it listening
 */
def registerRegistrationHandler() {
	if("Log".equals(config[RegistrationHandler.LISTENER_TYPE])){
		println "Setting up listener for the emulator log to get registration ids"
		regHandler = new LogHandler()
	}else if ("Rest".equals(config[RegistrationHandler.LISTENER_TYPE])){
		println "Setting up REST server to listen for messages from the emulator to get registration ids"
		regHandler = new RestHandler()
	}else {
	    throw new IllegalArgumentException("Unknown registrationType : ${config[RegistrationHandler.LISTENER_TYPE]}");
	}
	regHandler.start(config, this)
}

/*
 * Make a POST to google to get the auth token.  This is that same as 
 * 
 * 	curl https://www.google.com/accounts/ClientLogin -d Email=SENDER_EMAIL
 *   -d Passwd=SENDER_EMAIL_PASSWORD -d accountType=HOSTED_OR_GOOGLE 
 *   -d source=companyName-applicationName-version -d service=ac2dm
 */
def getSenderAuthToken(){
	println "Get Sender Auth Token -> sending request for token to google"
	def http = new HTTPBuilder( 'https://www.google.com/' )
	def body = [Email:config[SENDER_EMAIL_KEY],
				Passwd:config[SENDER_PASSWORD_KEY],
				accountType:'HOSTED_OR_GOOGLE',
				source:'companyName-applicationName-version',
				service:'ac2dm']

	http.post( path: 'accounts/ClientLogin', body: body,
			requestContentType: 'application/x-www-form-urlencoded' ) { resp, InputStreamReader reader ->
				println "Get Sender Auth Token -> response code from google : ${resp.statusLine}"
				def found=false
				reader.readLines().each{ line ->
					if(line ==~ /Auth=.*/){
						authToken = (line =~ /Auth=(.*)/)[0][1]
						println "Get Sender Auth Token -> Got AuthToken : ${authToken}"
						found=true
					}
				}
				if(!found){
					println "ERROR Sender Auth Token -> Unable to find Auth Token in response"
					System.exit(0);
				}
			}

}


/**
 * Send a message to C2DM with 
 * - Registration_id of the device
 * - Sender Auth token
 * 
 * The application specific data below should be returned from the method getPushMessageForDevice
 * - 'collapse_key' for use by google to collapse duplicate messages
 * - Your application messages in the expected form
 *   data.XXXX=YYYY
 *   
 *   Supply the collapse_key and other application data in a map<String,String>
 * 
 * @param bodyParams
 * @return
 */
def sendToC2DM(input){
	/*
	 * curl -d 'collapse_key=1.ABS&data.XXXX=YYYY&data.123=ABC&
	 * registration_id=AAAAAAA'  
	 * --header 'Authorization: GoogleLogin auth=BBBBBBB'  
	 *  https://android.apis.google.com/c2dm/sen
	 */
	def requestBody = config[PUSH_MESSAGE_BUILDER](input)
	//add in registration id
	requestBody["registration_id"]=getRegistrationId()
	println "Pushing message to C2DM(google) ->  body : ${requestBody}"
	def http = new HTTPBuilder( 'https://android.apis.google.com' )
	def requestHeaders = ["Authorization: GoogleLogin auth":authToken]

	http.request(POST){
		uri.path='/c2dm/send'
		requestContentType= 'application/x-www-form-urlencoded'
		body =  requestBody
		headers."Authorization"= "GoogleLogin auth=" + authToken
		
		response.'200' = { resp, reader -> println "Push to C2DM succeeded" }
		response.'503' = { resp, reader -> println "Push to C2DM failed C2DM system currently unavaliable" }
		response.'401' = { resp, reader ->
			println "Push to C2DM failed invalid Sender_Auth_Token.  Requesting new one"
			getSenderAuthToken()
		}
		http.handler.failure = { resp ->
			println "Push to C2DM failed because of unexpected response code ${resp.statusLine}"
		}
	}
}

}