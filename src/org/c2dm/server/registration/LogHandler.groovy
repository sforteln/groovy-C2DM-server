package org.c2dm.server.registration


import org.c2dm.server.C2DMServer
import java.util.Map;

/**
 * Watch the emulator log for registration ids.  Update the global variable when we see a new one so
 * we always use the newest one.
 *
 */
public class LogHandler implements RegistrationHandler{
	public static String ADB_BINARY_LOCATION_KEY ="adbCmd"
	public static String REGISTRATION_REG_EX_CAPTURE_KEY = "regExToCaptureRegistrationId"
	def regIdThread

	def start(Map config, C2DMServer server) {
		/*
		 * Create a separate thread to tail the log and update the
		 * registrationId when the device outputs a new one.  Need to
		 * watch the logs since google can send a new/updated reg id to the device at any time
		 */
		regIdThread = Thread.start{

			def tailLogCmd = "${config[ADB_BINARY_LOCATION_KEY]} logcat".execute()
			tailLogCmd.in.eachLine { line ->
				if(line==~config[REGISTRATION_REG_EX_CAPTURE_KEY]){
					server.setRegistrationId((line=~config[REGISTRATION_REG_EX_CAPTURE_KEY])[0][1])
					println "Registration id from device found in log \n${server.getRegistrationId()}"
				}
			}
			sleep(2000)
		}
		//Wait for first reg id to be found before moving on
		println "Waiting to find first registration id in the log"
		while(server.getRegistrationId()==null){
			println "Waiting to find first registration id in logs. See you in 2 seconds"
			sleep(2000)
		}
	}
	def stop(){
		//no-op
	}
}
