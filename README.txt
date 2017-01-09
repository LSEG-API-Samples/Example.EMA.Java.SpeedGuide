README

The SpeedGuide tool is a simple, GUI-based, market data consumer written in EMA Java that allows the ability to navigate through 
the different data offered within Thomson Reuters.  The user is presented with a basic window allowing the selection of 
navigation items, as identified by values within <> brakets, or to input items manually.  

The package includes 2 components offering multiple ways to launch the tool.  Packaged are:

	- SpeedGuide.jar: An executable JAR available for both Windows and Linux
	- SpeedGuide.exe: A windows wrapper
	
Launching the tool by double clicking on the icon:
==================================================

Double-clicking either the .jar or .exe file will not pass any required parameters to the application.  As such, the application 
will present a Connection Dialog requesting for these connection parameters.  In either case, no console is involved thus no 
additional messages, such as log messages, can be viewed.

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
	
  
Command-line Options
--------------------

Options:

  --host=hostname:port		Server address/hostname and port of your Market Data server.
							Syntax: <host/ip>:<port>.  Eg: elektron:14002 or 192.168.1.1:14002
  --service=serviceName		Service Name providing market data content.
							Eg: ELEKTRON_AD.
  --user=userName			User name required if authentication is enabled on server.
							Note: if no user name is provided, the utility will use your desktop login
  --d[ebug]					Debug Mode.  Display verbose messages to the console
  --h[elp]					Prints the options screen
				
If neither the --host nor --service is provided, the utility will prompt the user to enter these values.

Example:
  > SpeedGuide --host=elektron:14002 --service=ELEKTRON_AD --user=testuser
