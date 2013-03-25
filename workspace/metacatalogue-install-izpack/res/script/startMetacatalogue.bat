
		@ECHO off
		:: Batch file to start Sitools2
		:: written by m.marseille (AKKA) 20/01/2011
		
		:: Clear the screen
		CLS
		SETLOCAL
		TITLE startSitools
		
		:: Chemins courants du sitools
		SET sitoolsHome="%INSTALL_PATH"
		SET sitoolsSnap=%sitoolsHome%\workspace
		SET sitoolsCots=%sitoolsHome%\cots
		SET sitoolsCore=%sitoolsSnap%\fr.cnes.sitools.metacatalogue.core
		
		SET sitoolsJarName=fr.cnes.sitools.metacatalogue.core.jar
		
		:: Parametres du script
		SET prog=%0
		SET prog=%prog:.bat=%
		SET myDir=CHDIR
		FOR /F "tokens=2 delims= " %%A IN ('TASKLIST /FI ^"WINDOWTITLE eq startSitools^" /NH') DO SET myPid=%%A
		
		:: Creation du repertoire et du fichier 'LOG'
		SET LOG_DIR="%USERPROFILE%\LOG"
		IF NOT EXIST %LOG_DIR% MKDIR %LOG_DIR%
		SET LOG="%LOG_DIR:~1,-1%\%prog%-%myPid%.log"
		IF EXIST %LOG% DEL %LOG%
		ECHO Fichier de LOG : %LOG:~1,-1%
		
		:: Verifie que le fichier metacatalogue.properties est présent
		SET SITOOLS_PROPS=%sitoolsCore%\metacatalogue.properties
		IF EXIST %SITOOLS_PROPS% GOTO NOERROR 
		ECHO --- ERREUR --- > %LOG%
		ECHO Impossible de trouver %SITOOLS_PROPS%. Abandon. >> %LOG%
		ECHO --- ERREUR --- >> %LOG%
		GOTO :EOF
		:NOERROR
		
		:: Lancement de JAVA
		SET ARGS=-Xms256m -Xmx512m -Djava.net.preferIPv4Stack=true
		
		::List of parameters to pass to the java program
		SET PROGRAM_PARAMS=%1
	  	TITLE Sitools2
	  	ECHO Refreshing CLASSPATH
		java -jar ./sitools-update-classpath/sitools-update-classpath.jar --tmp_directory=ext --directory=ext --jar_target=%sitoolsJarName% 2>&1 >> %LOG%
  	  	ECHO JAVA Sitools2-Metacatalogue starting ...
	  	ECHO JAVA Sitools2-Metacatalogue starting ... >> %LOG%
	  	java -jar %ARGS% %sitoolsJarName% %PROGRAM_PARAMS% >> %LOG% 2>&1
	  	GOTO :EOF
		
		
		:: -------------
		:: fin du script
		:: -------------
		
		ENDLOCAL		
	
	