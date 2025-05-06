@echo off
setlocal
REM Build the launcher (optional, comment out if already built)
mvn -f "%~dp0pom.xml" package

REM Run the launcher
java -jar target\osrs-helper-launcher-1.0-SNAPSHOT.jar > launcher-output.txt 2>&1

type launcher-output.txt
pause

endlocal