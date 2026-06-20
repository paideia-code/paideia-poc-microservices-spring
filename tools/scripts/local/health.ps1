[CmdletBinding()]
param(
    [int] $TimeoutSeconds = 1
)

. (Join-Path $PSScriptRoot '_common.ps1')

$results = foreach ($service in Get-PaideiaServices) {
    $url = Get-PaideiaHealthUrl -Service $service
    try {
        $response = Invoke-RestMethod -Uri $url -TimeoutSec $TimeoutSeconds
        [pscustomobject]@{
            Service = $service.Name
            Url = $url
            Status = $response.status
        }
    }
    catch {
        [pscustomobject]@{
            Service = $service.Name
            Url = $url
            Status = 'UNAVAILABLE'
        }
    }
}

$results | Format-Table -AutoSize
