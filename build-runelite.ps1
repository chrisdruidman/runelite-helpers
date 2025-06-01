# PowerShell script to build the patched RuneLite client
# Usage: Run from the workspace root

# --- Set release version here ---
$releaseVersion = '1.11.8'

Write-Host "[build-runelite.ps1] Changing directory to runelite/"
Set-Location -Path "$PSScriptRoot/runelite"

# --- Release build patch for runelite.properties ---
$propsPath = Join-Path $PSScriptRoot 'runelite\runelite-client\src\main\resources\net\runelite\client\runelite.properties'
if (Test-Path $propsPath) {
    $props = Get-Content $propsPath
    $props = $props -replace 'runelite.version=.*', "runelite.version=$releaseVersion"
    $props = $props -replace 'runelite.dirty=.*', 'runelite.dirty=false'
    $props = $props -replace 'runelite.api.base=.*', "runelite.api.base=https://api.runelite.net/runelite-$releaseVersion"
    $props = $props -replace 'runelite.pluginhub.version=.*', "runelite.pluginhub.version=$releaseVersion"
    Set-Content -Path $propsPath -Value $props
    Write-Host "[build-runelite.ps1] Patched runelite.properties for release build."
} else {
    Write-Host "[build-runelite.ps1] WARNING: Could not find runelite.properties to patch for release build."
}

#Write-Host "[build-runelite.ps1] Running mvn clean install..."
#mvn clean install

Write-Host "[build-runelite.ps1] Running mvn clean install skipping tests..."
mvn clean install -DskipTests

Set-Location -Path $PSScriptRoot
Write-Host "[build-runelite.ps1] Build complete."
