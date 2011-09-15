#!/opt/local/bin/groovy
/*
 * C2DMServer startup script
 * This groovy script hold all the configuration values and uses them to start the server
 */

import org.c2dm.server.registration.LogHandler
import org.c2dm.server.registration.RegistrationHandler
import org.c2dm.server.registration.RestHandler
import org.c2dm.server.C2DMServer

/**
* Start Config.  Set these values for your app
*
*/
def config = [:]
/*
* Sender Auth Token settings
* See : http://code.google.com/android/c2dm/#arch
*/
config[C2DMServer.SENDER_EMAIL_KEY] = "myApp@gmail.com" //Sender Id
config[C2DMServer.SENDER_PASSWORD_KEY] = "123abc"

/*
* This method will be called each time a message is to be sent to get
* the payload for the message as a map.  Be sure to include the "collapse_key"
* and that any application specific data keys have the form data.<key>
*
* The registration_id and auth_token will be added in by the emulator.
*
* See: http://code.google.com/android/c2dm/#server
*/
config[C2DMServer.PUSH_MESSAGE_BUILDER] = {input ->
	def messageParams = [:]
	//You should set this to a collapse_key that has meaning for your application
	messageParams["collapse_key"] = System.currentTimeMillis().toString();
	/*
	 * Add any application specific message params here.
	 * Make sure your message params are of the form "data.<key>"
	 * See : http://code.google.com/android/c2dm/#server
	 */
	if(input){
		messageParams["data.message"] = input;
	}
	return messageParams
}

/*
* Registration id settings
*/

/* Set how the server should get the devices registration id.
 * 
 * Log - the server must be running on the same host as the emulator 
 *   and will tail the android emulator logs using adb to find the reg id
 * Rest - the server will start listening for Rest PUT from your application with the 
 *   registration id in it
 */
config[RegistrationHandler.LISTENER_TYPE] = "Log"//Log or Rest

/*
 * Log registration settings, these only need to be changed if you are getting the reg id from the emulators logs
 *  using adb
 */
// Full path to the adb binary
config[LogHandler.ADB_BINARY_LOCATION_KEY] = "/opt/android-sdk-mac_x86/platform-tools/adb"
// This regex will be used to capture the registration ids from the emulator logs
config[LogHandler.REGISTRATION_REG_EX_CAPTURE_KEY] = /.*registration_id\s=\s(.*)/


/*
* REST registration settings, these only need to be changed if you are using the REST server to get the reg id
*/
//port the rest server should listen on
config[RestHandler.PORT] = "8083"
//print the requests received into the console
config[RestHandler.SHOW_REQUEST]= false
//closure used to extract the registration id from the rest request from your application. 
//If the closure is unable to find a registration id in the message it should return null
config[RestHandler.REST_REQUEST_HANDLER] = { it ->
					def captureRegEx = ~/.*"NotificationChannelUri": "([^"]*)".*/
					def matcher = captureRegEx.matcher(it)
					def id = null
					if(matcher.find()) {
						id =  matcher.group(1)
					}
					return id
				}
/**
 * End config section - You shouldn't need to edit anything below here 
 */

//start up the server with config
C2DMServer server = new C2DMServer(config)
server.start()
