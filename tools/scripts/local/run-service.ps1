[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $Service,

    [int] $Port,

    [switch] $SkipInfra,

    [switch] $DryRun
)

. (Join-Path $PSScriptRoot '_common.ps1')

$serviceDefinition = Get-PaideiaService -Name $Service
$repoRoot = Get-PaideiaRepoRoot
$mavenWrapper = Get-PaideiaMavenWrapper
$effectivePort = if ($Port -gt 0) { $Port } else { $serviceDefinition.DefaultPort }

Set-Item -Path "Env:$($serviceDefinition.PortEnv)" -Value $effectivePort

if (-not $SkipInfra) {
    & (Join-Path $PSScriptRoot 'up-infra.ps1')
}

Write-Host "Servicio: $($serviceDefinition.Name)"
Write-Host "Puerto: $effectivePort ($($serviceDefinition.PortEnv))"
Write-Host "Modulo: $($serviceDefinition.Module)"

if ($DryRun) {
    Write-Host "`"$mavenWrapper`" spring-boot:run -pl $($serviceDefinition.Module)"
    exit 0
}

Push-Location $repoRoot
try {
    & $mavenWrapper spring-boot:run -pl $serviceDefinition.Module
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}
finally {
    Pop-Location
}
