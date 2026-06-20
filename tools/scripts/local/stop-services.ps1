[CmdletBinding()]
param(
    [string[]] $Only
)

. (Join-Path $PSScriptRoot '_common.ps1')

$services = Resolve-PaideiaServiceList -Only $Only

foreach ($service in $services) {
    Stop-PaideiaServiceProcess -Service $service
}
