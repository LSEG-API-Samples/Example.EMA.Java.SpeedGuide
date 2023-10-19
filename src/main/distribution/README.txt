README

The SpeedGuide tool is a simple, GUI-based, market data consumer written in Java, using the LSEG Real-Time Java SDK,
that allows the ability to navigate through realtime data offered within LSEG.  

Refer to GitHub (https://github.com/LSEG-API-Samples/Example.EMA.Java.SpeedGuide) for full details of the operation of the utility.

Note: While the utility will launch the "REFINITIV" RIC, which represents the root code of the Speed Guide pages, the utility
      is extremely useful for developers to visualize any other instrument available within real-time platform.

The user is presented with a basic window allowing the selection of navigation items, as identified by values within <> brackets,
or to input items manually.

The package includes 2 components offering multiple ways to launch the tool.  Packaged are:

	- SpeedGuide.jar: An executable JAR available for both Windows and Linux
	- SpeedGuide.exe: A windows wrapper

Launching the tool by double clicking on the icon (Windows):
============================================================

Double-clicking either the .jar or .exe file will not pass any required parameters to the application.  As such, the application
will present a Connection Dialog requesting for these connection parameters.  In either case, no console is involved thus no
additional messages, such as log messages, can be viewed.

Note: Launching the executable JAR requires the Javaw (https://docs.oracle.com/javase/7/docs/technotes/tools/windows/java.html) 
program to open it.  When not associated, you will be presented with a request to select a program.


Launching the tool from the console:
====================================

At the console, you can pass command-line parameters to the utility:

	o Launching the executable JAR
		> java -jar SpeedGuide.jar [options]

		When launching the executable JAR, users optionally specify command-line options and have the opportunity to see
		the output on the console.

	o Launching the windows wrapper EXE
		> SpeedGuide.exe [options]

		The windows wrapper is strictly a GUI based facility that does not have an explicit console attached.  Thus, no
		output can be viewed on the console.  However, users can capture the output within a file.  for example:

		> SpeedGuide.exe>output.txt [options]

Console output will provide additional log messages, and if the user specifies the --d[ebug] option on the command-line,
additional debug details.  These messages can be useful to better understand connection details, messages returned, etc.

Command-line Options
--------------------

Options:

************* ADS Connection Parameters **************
  --host=hostname:port    Required. Elektron Server address/hostname and port of your Market Data server.
                          Syntax: <host/ip>:<port>.  Eg: myserver:14002 or 192.168.1.1:14002
  --service=serviceName   Required. ADS Service Name providing market data content.
                          Eg: ELEKTRON_AD.
  --user=userName         Optional. DACS User name required if authentication is enabled on server.
                          Note: if no user name is provided, the utility will use your desktop login
  --appid=ApplicationId   Optional. DACS Application ID if authentication is enabled on server.
                          Application ID has no default.
  --position=Position     Optional. DACS Position if authentication is enabled on server.
                          Position has no default.
                          
************* Real-Time -- Optimized Connection Parameters **************
**** Version 1 Authentication >
  --machineId=machine ID  Required. Real-Time -- Optimized Machine ID/User required for OAuth Password Grant.
                          Eg: GE-A-00000000-1-8888
  --password=password     Required. Real-Time -- OPtimized password required for OAuth Password Grant.
                          Eg: Sunshine_1_UserPass
  --appKey=App Key        Required. Real-Time -- Optimized AppKey or Client ID required for server authentication.
                          Eg: x888x8x88888888x88888x88x8888xx88x88888x
**** Version 2 Authentication >
  --clientId=Client ID    Required. Real-Time -- Optimized Client/Service Account ID required for OAuth Client Credentials.
                          Eg: GE-XXXXXXXXXXXX
  --clientSecret=secret   Required. Real-Time -- Optimized Client secret required for OAuth Client Credentials.
                          Eg: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

  --keyStore=keystorefile Optional. A Java KeyStore (JKS) required for secure package exchange.
                          Default: SpeedGuide provides a file for convenience.
  --keyStorePasswd=passwd Optional. Password associated with the specified keystore file.
                          Default: SpeedGuide includes the password for the default keystore file.

  --d[ebug]               Debug Mode.  Display verbose messages to the console
  --h[elp]                Prints this screen

If neither the required parameters for the ADS or Real-Time -- Optmized are specified, the utility will prompt the user.

Example:
  > SpeedGuide.exe --host=myserver:14002 --service=ELEKTRON_AD --user=testuser --appid=256 --position=127.0.0.1
  > SpeedGuide.exe --clientId=GE-XXXX1234XXXX --clientSecret=9x999999-9xxx-9999-9x99-9x9xx99x9x99
