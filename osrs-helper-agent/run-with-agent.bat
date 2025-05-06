@echo off
REM Uses %localappdata%\Runelite as the base path for both agent and RuneLite JAR
setlocal
set "AGENT_JAR=%localappdata%\Runelite\osrs-helper-agent\target\osrs-helper-agent-1.0-SNAPSHOT.jar"
set "RUNELITE_JAR=%localappdata%\Runelite\RuneLite.jar"

REM Launch RuneLite with the agent attached
java -javaagent:"%AGENT_JAR%" -jar "%RUNELITE_JAR%"
endlocal
