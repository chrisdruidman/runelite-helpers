@echo off
setlocal
set "AGENT_JAR=.\target\osrs-helper-agent-1.0-SNAPSHOT-shaded.jar"
set "RUNELITE_JAR=%localappdata%\Runelite\RuneLite.jar"

REM Launch RuneLite with the agent attached and redirect output to a file
java -javaagent:"%AGENT_JAR%" -jar "%RUNELITE_JAR%" > output.txt 2>&1
endlocal
