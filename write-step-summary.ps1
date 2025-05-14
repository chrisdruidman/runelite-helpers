param(
    [Parameter(Mandatory=$true)][string]$StepName,
    [Parameter(Mandatory=$true)][string]$Summary
)

if (-not $StepName -or -not $Summary) {
    Write-Host "[write-step-summary.ps1] ERROR: Both -StepName and -Summary parameters are required."
    exit 1
}

$ts = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
$out = "steps-taken/${ts}_${StepName}.md"
$content = @"
# Step Complete: $StepName

**Date:** $ts
**Agent:** GitHub Copilot

$Summary
"@
Set-Content -Path $out -Value $content
Write-Host "Step summary written to $out"
