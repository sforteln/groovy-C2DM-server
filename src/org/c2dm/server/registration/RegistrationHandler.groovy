package org.c2dm.server.registration

import org.c2dm.server.C2DMServer

/*
 * Interface for Registrationhander's which through various 
 *   means get the registration Id from the device or emulator.
 *   
 *  When an impl get a new registration id it should call 
 *   server.setRegistrationId() to update the server with the new id 
 */
public interface RegistrationHandler {
	public static String LISTENER_TYPE ="registrationListenerType"
	
	
	def start(Map config, C2DMServer server)
	def stop()
}
