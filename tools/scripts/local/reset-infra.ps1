[CmdletBinding()]
param(
    [switch] $Force
)

. (Join-Path $PSScriptRoot '_common.ps1')

if (-not $Force) {
    Write-Warning 'Esto detendra Docker Compose y borrara los volumenes locales de PostgreSQL y MongoDB.'
    $confirmation = Read-Host 'Escribe RESET para continuar'
    if ($confirmation -ne 'RESET') {
        Write-Host 'Operacion cancelada.'
        exit 1
    }
}

$repoRoot = Get-PaideiaRepoRoot

Push-Location $repoRoot
try {
    Write-Host 'Reiniciando infraestructura local y borrando volumenes...'
    & docker compose down -v
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    & docker compose up -d
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}
finally {
    Pop-Location
}

