README

The SpeedGuide tool is a simple, GUI-based, market data consumer written in Java, using the LSEG Real-Time Java SDK,
that allows the ability to navigate through realtime data offered within LSEG.  

Refer to GitHub (https://github.com/LSEG-API-Samples/Example.EMA.Java.SpeedGuide) for full details of the operation of the utility.

Note: While the utility will launch the "REFINITIV" RIC, which represents the root code of the Speed Guide pages, the utility
      is extremely useful for developers to visualize any other instrument available within real-time platform.

The user is presented with a basic window allowing the selection of navigation items, as identified by values within <> brackets,
or to input items manually.

The package includes the SpeedGuide.jar file, an executable JAR available for both Windows and Linux.  JavaFX is not bundled
within the JAR.

Prerequisites:
==============

  o A Java SDK supported by the Real-Time SDK - Java edition.  The project is built with Java 11 as its baseline.
    See the API Compatibility Matrix for the supported versions:
    https://developers.lseg.com/en/api-catalog/real-time-opnsrc/rt-sdk-java/documentation#api-compatibility-matrix

  o A JavaFX SDK that matches your Java version (https://gluonhq.com/products/javafx/#downloads).  Extract it into the same folder as
    SpeedGuide.jar.  The folder name depends on the version you download (for example, javafx-sdk-17.0.18 or javafx-sdk-21.0.13):

        SpeedGuide.jar
        javafx-sdk-<version>/
            lib/
            ...


Launching the tool from the console:
====================================

At the console, you can pass command-line parameters to the utility:

	o Launching the executable JAR
		> java --module-path ".\javafx-sdk-<version>\lib" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics -jar ".\SpeedGuide.jar" [options]

		Or

		> java -cp ./SpeedGuide.jar --module-path ./javafx-sdk-<version>/lib --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics com.lseg.ema.example.gui.SpeedGuide [options]

		Set --module-path to the lib folder of your JavaFX SDK.  When no connection options are specified, the application
		presents a Connection Dialog requesting them.

Console output will provide additional log messages, and if the user specifies the --d[ebug] option on the command-line,
additional debug details.  These messages can be useful to better understand connection details, messages returned, etc.

Command-line Options
--------------------

Options:

 --service=serviceName   Optional. Service Name providing market data content.
                         Eg: ELEKTRON_DD. Default: Determined from Directory response.

************* ADS Connection Parameters **************
  --host=hostname:port    Required. Elektron Server address/hostname and port of your Market Data server.
                          Syntax: <host/ip>:<port>.  Eg: myserver:14002 or 192.168.1.1:14002
  --user=userName         Optional. DACS User name required if authentication is enabled on server.
                          Note: if no user name is provided, the utility will use your desktop login
  --appid=ApplicationId   Optional. DACS Application ID if authentication is enabled on server.
                          Application ID has no default.
  --position=Position     Optional. DACS Position if authentication is enabled on server.
                          Position has no default.
                          
************* Real-Time -- Optimized Connection Parameters **************
  --region=location       Optional. Specify the location to connect within the cloud.
	                        Eg: ap-northeast-1 (Asia) eu-west-1 (EU) us-east-2 (US). Default: us-east-1

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


  --keyStore=keystorefile Optional. A Java KeyStore (JKS) file to use for secure package exchange.
                          Default: none. The default Java cacerts keystore is used.
  --keyStorePasswd=passwd Optional. Password associated with the specified keystore file.
                          Default: none.

  --d[ebug]               Debug Mode.  Display verbose messages to the console
  --h[elp]                Prints this screen

If neither the required parameters for the ADS or Real-Time -- Optimized are specified, the utility will prompt the user.

Example (<javafx> = --module-path "./javafx-sdk-<version>/lib" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics):
  > java <javafx> -jar SpeedGuide.jar --host=myserver:14002 --service=ELEKTRON_AD --user=testuser --appid=256 --position=127.0.0.1
  > java <javafx> -jar SpeedGuide.jar --clientId=GE-XXXX1234XXXX --clientSecret=9x999999-9xxx-9999-9x99-9x9xx99x9x99
  > java <javafx> -jar SpeedGuide.jar --clientId=GE-123X9ABCDE9Z --clientSecret=9z123456-9abc-5555-9a12-1a2bc34d5e67 --region=eu-west-1

