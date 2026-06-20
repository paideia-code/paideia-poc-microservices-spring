[CmdletBinding()]
param(
    [string[]] $Only,

    [switch] $SkipInfra,

    [switch] $DryRun
)

. (Join-Path $PSScriptRoot '_common.ps1')

$repoRoot = Get-PaideiaRepoRoot
$runDir = Get-PaideiaRunDir
$logDir = Get-PaideiaLogDir
$mavenWrapper = Get-PaideiaMavenWrapper
$powershellHost = Get-PaideiaPowerShellHost
$services = Resolve-PaideiaServiceList -Only $Only

New-PaideiaDirectory -Path $runDir
New-PaideiaDirectory -Path $logDir

if (-not $SkipInfra) {
    & (Join-Path $PSScriptRoot 'up-infra.ps1')
}

foreach ($service in $services) {
    $existingProcess = Get-PaideiaProcessFromPidFile -Service $service
    if ($existingProcess) {
        Write-Host "$($service.Name) ya esta corriendo con PID $($existingProcess.Id)."
        continue
    }

    $pidPath = Get-PaideiaPidPath -Service $service
    $outLog = Join-Path $logDir "$($service.Name).out.log"
    $errLog = Join-Path $logDir "$($service.Name).err.log"
    $command = "`$env:$($service.PortEnv) = '$($service.DefaultPort)'; & '$mavenWrapper' spring-boot:run -pl '$($service.Module)'; exit `$LASTEXITCODE"

    if ($DryRun) {
        Write-Host "$($service.Name): $powershellHost -NoProfile -Command $command"
        continue
    }

    Write-Host "Levantando $($service.Name) en puerto $($service.DefaultPort)..."
    $startProcessArgs = @{
        FilePath = $powershellHost
        ArgumentList = @('-NoProfile', '-Command', $command)
        WorkingDirectory = $repoRoot
        RedirectStandardOutput = $outLog
        RedirectStandardError = $errLog
        PassThru = $true
    }

    if (Test-PaideiaIsWindows) {
        $startProcessArgs.WindowStyle = 'Hidden'
    }

    $process = Start-Process @startProcessArgs

    Set-Content -LiteralPath $pidPath -Value $process.Id
}

if (-not $DryRun) {
    Write-Host "Logs: $logDir"
}
