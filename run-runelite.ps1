# PowerShell script to run the latest built RuneLite client (shaded jar)
# Usage: Run from the workspace root

$targetDir = Join-Path $PSScriptRoot 'runelite\runelite-client\target'
$shadedJars = Get-ChildItem -Path $targetDir -Filter 'client-*-shaded.jar' | Sort-Object LastWriteTime -Descending

if ($shadedJars.Count -eq 0) {
    Write-Host "[run-runelite.ps1] ERROR: No shaded client jar found in $targetDir"
    exit 1
}

$clientJar = $shadedJars[0].FullName
Write-Host "[run-runelite.ps1] Launching RuneLite client: $clientJar"
java -jar $clientJar
