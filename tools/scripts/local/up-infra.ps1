[CmdletBinding()]
param()

. (Join-Path $PSScriptRoot '_common.ps1')

$repoRoot = Get-PaideiaRepoRoot

Push-Location $repoRoot
try {
    Write-Host 'Levantando infraestructura local con Docker Compose...'
    & docker compose up -d
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}
finally {
    Pop-Location
}

