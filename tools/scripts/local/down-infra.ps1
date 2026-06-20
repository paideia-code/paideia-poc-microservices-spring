[CmdletBinding()]
param()

. (Join-Path $PSScriptRoot '_common.ps1')

$repoRoot = Get-PaideiaRepoRoot

Push-Location $repoRoot
try {
    Write-Host 'Deteniendo infraestructura local...'
    & docker compose down
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}
finally {
    Pop-Location
}

