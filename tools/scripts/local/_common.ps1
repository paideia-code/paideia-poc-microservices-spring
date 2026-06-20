Set-StrictMode -Version Latest

$Script:RepoRoot = (Resolve-Path (Join-Path (Join-Path $PSScriptRoot '..') '..')).Path
$Script:ScriptsDir = Join-Path $Script:RepoRoot 'scripts'
$Script:LocalScriptsDir = Join-Path $Script:ScriptsDir 'local'
$Script:RunDir = Join-Path $Script:LocalScriptsDir '.run'
$Script:LogDir = Join-Path $Script:LocalScriptsDir 'logs'

$Script:Services = @(
    [pscustomobject]@{
        Name = 'course-service'
        ShortName = 'course'
        Module = 'services/course-service'
        PortEnv = 'COURSE_SERVICE_PORT'
        DefaultPort = 8081
    },
    [pscustomobject]@{
        Name = 'enrollment-service'
        ShortName = 'enrollment'
        Module = 'services/enrollment-service'
        PortEnv = 'ENROLLMENT_SERVICE_PORT'
        DefaultPort = 8082
    },
    [pscustomobject]@{
        Name = 'payment-service'
        ShortName = 'payment'
        Module = 'services/payment-service'
        PortEnv = 'PAYMENT_SERVICE_PORT'
        DefaultPort = 8083
    },
    [pscustomobject]@{
        Name = 'user-service'
        ShortName = 'user'
        Module = 'services/user-service'
        PortEnv = 'USER_SERVICE_PORT'
        DefaultPort = 8084
    },
    [pscustomobject]@{
        Name = 'content-service'
        ShortName = 'content'
        Module = 'services/content-service'
        PortEnv = 'CONTENT_SERVICE_PORT'
        DefaultPort = 8085
    },
    [pscustomobject]@{
        Name = 'notification-service'
        ShortName = 'notification'
        Module = 'services/notification-service'
        PortEnv = 'NOTIFICATION_SERVICE_PORT'
        DefaultPort = 8086
    }
)

function Get-PaideiaRepoRoot {
    return $Script:RepoRoot
}

function Get-PaideiaRunDir {
    return $Script:RunDir
}

function Get-PaideiaLogDir {
    return $Script:LogDir
}

function Test-PaideiaIsWindows {
    if ($PSVersionTable.PSEdition -eq 'Desktop') {
        return $true
    }

    return [System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform(
        [System.Runtime.InteropServices.OSPlatform]::Windows
    )
}

function Get-PaideiaMavenWrapper {
    if (Test-PaideiaIsWindows) {
        return Join-Path $Script:RepoRoot 'mvnw.cmd'
    }

    return Join-Path $Script:RepoRoot 'mvnw'
}

function Get-PaideiaPowerShellHost {
    $pwsh = Get-Command 'pwsh' -ErrorAction SilentlyContinue
    if ($pwsh) {
        return $pwsh.Source
    }

    $powershell = Get-Command 'powershell.exe' -ErrorAction SilentlyContinue
    if ($powershell) {
        return $powershell.Source
    }

    throw 'No se encontro pwsh ni powershell.exe en PATH.'
}

function Get-PaideiaServices {
    return $Script:Services
}

function Get-PaideiaService {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Name
    )

    $service = $Script:Services | Where-Object {
        $_.Name -eq $Name -or $_.ShortName -eq $Name
    } | Select-Object -First 1

    if (-not $service) {
        $validNames = ($Script:Services | ForEach-Object { "$($_.Name) / $($_.ShortName)" }) -join ', '
        throw "Servicio invalido '$Name'. Valores validos: $validNames"
    }

    return $service
}

function Resolve-PaideiaServiceList {
    param(
        [string[]] $Only
    )

    if (-not $Only -or $Only.Count -eq 0) {
        return Get-PaideiaServices
    }

    return $Only | ForEach-Object { Get-PaideiaService -Name $_ }
}

function New-PaideiaDirectory {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
}

function Get-PaideiaPidPath {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    return Join-Path $Script:RunDir "$($Service.Name).pid"
}

function Get-PaideiaProcessFromPidFile {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    $pidPath = Get-PaideiaPidPath -Service $Service
    if (-not (Test-Path -LiteralPath $pidPath)) {
        return $null
    }

    $rawPid = (Get-Content -LiteralPath $pidPath -Raw).Trim()
    if (-not $rawPid) {
        return $null
    }

    return Get-Process -Id ([int] $rawPid) -ErrorAction SilentlyContinue
}

function Get-PaideiaProcessCommandLine {
    param(
        [Parameter(Mandatory = $true)]
        [int] $ProcessId
    )

    if (Test-PaideiaIsWindows) {
        $processInfo = Get-CimInstance Win32_Process -Filter "ProcessId=$ProcessId" -ErrorAction SilentlyContinue
        if ($processInfo) {
            return [string] $processInfo.CommandLine
        }

        return ''
    }

    $commandLine = & ps -p $ProcessId -o args= 2>$null
    if ($commandLine) {
        return ($commandLine -join ' ')
    }

    return ''
}

function Test-PaideiaProcessNameCanStop {
    param(
        [Parameter(Mandatory = $true)]
        [string] $ProcessName
    )

    $allowedNames = @(
        'bash',
        'cmd',
        'java',
        'javaw',
        'mvn',
        'mvnw',
        'powershell',
        'pwsh',
        'sh'
    )

    return $allowedNames -contains $ProcessName.ToLowerInvariant()
}

function Test-PaideiaCommandMatchesService {
    param(
        [AllowEmptyString()]
        [string] $CommandLine = '',

        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    if (-not $CommandLine) {
        return $false
    }

    $normalizedCommand = $CommandLine.Replace('\', '/')
    $normalizedModule = ([string] $Service.Module).Replace('\', '/')

    return (
        $normalizedCommand.Contains($normalizedModule) -or
        (
            $normalizedCommand.Contains('spring-boot:run') -and
            $normalizedCommand.Contains($Service.Name)
        )
    )
}

function Remove-PaideiaPidFile {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    $pidPath = Get-PaideiaPidPath -Service $Service
    if (-not (Test-Path -LiteralPath $pidPath)) {
        return
    }

    for ($attempt = 1; $attempt -le 3; $attempt++) {
        try {
            Remove-Item -LiteralPath $pidPath -Force -ErrorAction Stop
            return
        }
        catch {
            if ($attempt -eq 3) {
                try {
                    Set-Content -LiteralPath $pidPath -Value '' -ErrorAction Stop
                    Write-Warning "No se pudo eliminar ${pidPath}, pero se dejo vacio: $($_.Exception.Message)"
                }
                catch {
                    Write-Warning "No se pudo limpiar ${pidPath}: $($_.Exception.Message)"
                }
                return
            }

            Start-Sleep -Milliseconds 200
        }
    }
}

function Stop-PaideiaProcessIfSafe {
    param(
        [Parameter(Mandatory = $true)]
        [int] $ProcessId,

        [Parameter(Mandatory = $true)]
        [object] $Service,

        [switch] $RequireServiceMatch
    )

    $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if (-not $process) {
        return $false
    }

    if ($ProcessId -eq $PID) {
        Write-Warning "No se detuvo PID $ProcessId ($($process.ProcessName)): es el proceso actual del script."
        return $false
    }

    if (-not (Test-PaideiaProcessNameCanStop -ProcessName $process.ProcessName)) {
        Write-Warning "No se detuvo PID $ProcessId ($($process.ProcessName)): no parece un proceso lanzado por los scripts locales."
        return $false
    }

    $commandLine = Get-PaideiaProcessCommandLine -ProcessId $ProcessId
    if ($RequireServiceMatch -and -not (Test-PaideiaCommandMatchesService -CommandLine $commandLine -Service $Service)) {
        Write-Warning "No se detuvo PID $ProcessId ($($process.ProcessName)): no coincide con $($Service.Name)."
        return $false
    }

    Write-Host "Deteniendo PID $ProcessId ($($process.ProcessName))..."
    Stop-Process -Id $ProcessId -Force
    return $true
}

function Get-PaideiaServiceProcessIdsByCommand {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    if (Test-PaideiaIsWindows) {
        $processes = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue
        return @(
            $processes | Where-Object {
                $candidateProcessId = [int] $_.ProcessId
                $candidateProcessName = $_.Name -replace '\.exe$', ''
                $candidateProcessId -ne $PID -and
                    (Test-PaideiaProcessNameCanStop -ProcessName $candidateProcessName) -and
                    (Test-PaideiaCommandMatchesService -CommandLine ([string] $_.CommandLine) -Service $Service)
            } | ForEach-Object { [int] $_.ProcessId }
        )
    }

    $module = [regex]::Escape($Service.Module)
    return @(
        & pgrep -f $module 2>$null | ForEach-Object { [int] $_ }
    )
}

function Get-PaideiaServiceProcessIdsByPort {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    if (-not (Test-PaideiaIsWindows)) {
        return @()
    }

    $connections = Get-NetTCPConnection -LocalPort $Service.DefaultPort -State Listen -ErrorAction SilentlyContinue
    return @(
        $connections | ForEach-Object { [int] $_.OwningProcess } | Sort-Object -Unique
    )
}

function Stop-PaideiaServiceProcess {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    $stoppedAny = $false
    $candidateIds = @()

    $candidateIds += Get-PaideiaServiceProcessIdsByPort -Service $Service
    $candidateIds += Get-PaideiaServiceProcessIdsByCommand -Service $Service

    $pidFileProcess = Get-PaideiaProcessFromPidFile -Service $Service
    if ($pidFileProcess) {
        $candidateIds += [int] $pidFileProcess.Id
    }

    $candidateIds = @($candidateIds | Where-Object { $_ -gt 0 } | Sort-Object -Unique)

    if (-not $candidateIds -or $candidateIds.Count -eq 0) {
        Write-Host "$($Service.Name) no tiene un proceso registrado."
        Remove-PaideiaPidFile -Service $Service
        return
    }

    foreach ($candidateId in $candidateIds) {
        $commandLine = Get-PaideiaProcessCommandLine -ProcessId $candidateId
        $requiresServiceMatch = -not (Test-PaideiaCommandMatchesService -CommandLine $commandLine -Service $Service)
        if (Stop-PaideiaProcessIfSafe -ProcessId $candidateId -Service $Service -RequireServiceMatch:$requiresServiceMatch) {
            $stoppedAny = $true
        }
    }

    if (-not $stoppedAny) {
        Write-Warning "No se encontro un proceso seguro para detener $($Service.Name)."
    }

    Remove-PaideiaPidFile -Service $Service
}

function Get-PaideiaHealthUrl {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    $port = [Environment]::GetEnvironmentVariable($Service.PortEnv)
    if (-not $port) {
        $port = $Service.DefaultPort
    }

    return "http://localhost:$port/actuator/health"
}
