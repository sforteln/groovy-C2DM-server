# Simple development C2DM server written in groovy

This is meant to allow for easy testing of android apps that will use C2DM.

## Requirements
* [Groovy](http://groovy.codehaus.org/) 1.8
* [Http-builder](http://groovy.codehaus.org/HTTP+Builder) 0.5.0-RC2 (The server will grab these libraries itself using [@Grab](http://groovy.codehaus.org/Grapes+and+grab())
* [Restlet](http://www.restlet.org/) 2.0.9 (The server will grab these libraries itself using [@Grab](http://groovy.codehaus.org/Grapes+and+grab())


## Limitations
The server only stores one Registration id so it can only server one device/app at a time.

## Running it
Once you have groovy installed you can [run](http://groovy.codehaus.org/Running) the script from the [command line](http://groovy.codehaus.org/Running#Running-commandline) with<br/>
    groovy --classpath ./src ./src/org/c2dm/server/C2DMServerRunner.groovy
at the root of the project
**NOTE:**  Because this downloads the dependencies at runtime it can take awhile to get going on a slow connection.

## Using it

When you run it it may take a bit to respond but it should output something similar to this

> Get Sender Auth Token -> sending request for token to google<br/>
> Sep 11, 2011 10:35:31 PM groovyx.net.http.ParserRegistry getCharset<br/>
> WARNING: Could not find charset in response<br/>
> Get Sender Auth Token -> response code from google : HTTP/1.1 200 OK<br/>
> Get Sender Auth Token -> Got AuthToken : DQAAAL8AAABDIDSJVCg70gVMxZCenMTmdkhK0KK8CjfLJprqysLQFP8aCvfULKp-ebSAMj13nKV-j12fESyleyAAh6poHIljOQtuAygKPv1xlkw8sQ9mOTYfS34e_cRXgHSgyVr9yrMBx1QuXQbHhcifFSWaVy0KdJHgb9kpdr2kVfzs-ebBuC-SZfzgo-iR2P8O5ikPm_DEIpJ2p85CDgfdJ-CzifXVm-IjImSiZw2DPvN37cAud6jJbM0SRhrEJuOedBSgnTQ<br/>
> Setting up listener for the emulator log to get registration ids<br/>
> Waiting to find first registration id in the log<br/>
> Waiting to find first registration id in logs. See you in 2 seconds<br/>
> Registration id from device found in log APA91bEUgzU60oxP--dESJmHzQsxn859Ge_tkjG2GzUdOLgHBrpWjbFfPheqcrkjtKoVyjs_xPxu9zErDioPLcldIqKp-HSPhEH0ZMLJ858zVGLsfTc6fq8<br/>
> Ready. Press enter to push a message or Ctrl-c to quit.<br/>
> If you need to pass data to your message generation code type it in before pressing enter<br/>

As you can see 
* the server has started 
* talked to the C2DM server to get its auth token
* set up a listener on the emulators log
* found the registration id in the log

Now you can either just hit enter to send an empty message or type in some input that will be passed to your implementation of the config[C2DMServer.PUSH_MESSAGE_BUILDER] closure, see C2DMServerRunner for details.

> Pushing message to C2DM(google) ->  body : [collapse_key:1315885841965, registration_id:APA91bHI_QMC0EOoyntlBW5XfW1wv29NiXPbHgbRjJWs0yiWjP8BmkadbCVOUpOx4lew6smRiUYKfqgpwI3PzcM5G8A_A8-L66B6IeDH1xc-bix_iOehe0Q]<br/>
> Sep 12, 2011 8:50:44 PM groovyx.net.http.ParserRegistry getCharset<br/>
> WARNING: Could not find charset in response<br/>
> Push to C2DM succeeded<br/>

You can continue sending messages as before by typing input and pressing enter.  When you are done use Ctrl-C to exit.

## Configuring it 
All configuration is done by editing the file *src/org/c2dm/server/C2DMServerRunner.groovy*.






