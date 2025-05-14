# PowerShell script to create a patch from runelite/ changes
# Usage: .\create-runelite-patch.ps1 -PatchName "your-description"

param(
    [Parameter(Mandatory=$true)]
    [string]$PatchName
)

Write-Host "[create-runelite-patch.ps1] Starting patch creation..."

# Set paths
$runelitePath = Join-Path $PSScriptRoot 'runelite'
$patchesPath = Join-Path $PSScriptRoot 'osrs-helper-patches'

# Ensure patch folder exists
if (!(Test-Path $patchesPath)) {
    Write-Host "[create-runelite-patch.ps1] Creating patch directory: $patchesPath"
    New-Item -ItemType Directory -Path $patchesPath | Out-Null
}

# Get timestamp
$timestamp = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
$patchFile = Join-Path $patchesPath ("$PatchName-$timestamp.patch")

Write-Host "[create-runelite-patch.ps1] Running git diff (including untracked files) in $runelitePath..."
Push-Location $runelitePath
try {
    # Use git diff HEAD to include all changes to tracked files
    git diff HEAD > $patchFile
    # Also include untracked files (new files) in the patch
    $untrackedFiles = git ls-files --others --exclude-standard
    foreach ($file in $untrackedFiles) {
        if (Test-Path $file) {
            Write-Host "[create-runelite-patch.ps1] Adding untracked file: $file"
            git diff --no-index -- /dev/null $file >> $patchFile
        }
    }
    Write-Host "[create-runelite-patch.ps1] Patch created: $patchFile"
} finally {
    Pop-Location
}

# Optionally prompt for changelog/README
$readmeFile = Join-Path $patchesPath ("$PatchName-$timestamp.md")
$addReadme = Read-Host "Add a changelog/README for this patch? (y/n)"
if ($addReadme -eq 'y') {
    $desc = Read-Host "Enter a short description for this patch"
    Set-Content -Path $readmeFile -Value "# Patch: $PatchName`n`n**Created:** $timestamp`n`n$desc`n"
    Write-Host "[create-runelite-patch.ps1] Changelog/README created: $readmeFile"
}

Write-Host "[create-runelite-patch.ps1] Patch creation complete."
