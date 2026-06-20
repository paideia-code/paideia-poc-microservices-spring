# Scripts locales

Estos scripts automatizan el flujo local de la v1: infraestructura en Docker y microservicios Spring Boot ejecutados desde Maven.

Hay dos familias de scripts con los mismos objetivos:

- `.ps1`: PowerShell. Natural en Windows/Warp; tambien corre en Linux/macOS si instalas PowerShell 7 (`pwsh`).
- `.sh`: Bash. Natural en Linux, macOS, WSL y Git Bash.

## Requisitos

- PowerShell 7+ (`pwsh`) o Windows PowerShell 5.1 para `.ps1`.
- Bash para `.sh`.
- Docker con `docker compose`.
- Java 21 disponible en `PATH`.

Si PowerShell bloquea la ejecucion de scripts, habilita scripts solo para tu usuario:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

En Linux/macOS, puede ser necesario dar permiso de ejecucion a los `.sh`:

```bash
chmod +x scripts/local/*.sh
```

## Infraestructura

PowerShell:

```powershell
.\scripts\local\up-infra.ps1
.\scripts\local\down-infra.ps1
.\scripts\local\reset-infra.ps1
```

Linux/macOS:

```bash
./scripts/local/up-infra.sh
./scripts/local/down-infra.sh
./scripts/local/reset-infra.sh
```

`reset-infra.ps1` / `reset-infra.sh` borran los volumenes de Docker Compose. Usalos cuando quieras recrear PostgreSQL/MongoDB desde cero, por ejemplo si cambia `docker/postgres/init`.

## Servicios

Para correr un servicio en primer plano:

PowerShell:

```powershell
.\scripts\local\run-service.ps1 -Service course-service
.\scripts\local\run-service.ps1 -Service enrollment-service
```

Linux/macOS:

```bash
./scripts/local/run-service.sh --service course-service
./scripts/local/run-service.sh --service enrollment-service
```

Tambien acepta el nombre corto:

```powershell
.\scripts\local\run-service.ps1 -Service course
```

```bash
./scripts/local/run-service.sh course
```

Para correr otra instancia del mismo servicio, usa otro puerto:

```powershell
.\scripts\local\run-service.ps1 -Service course-service -Port 18081
```

```bash
./scripts/local/run-service.sh --service course-service --port 18081
```

Para levantar todos los servicios en background:

```powershell
.\scripts\local\start-services.ps1
```

```bash
./scripts/local/start-services.sh
```

Para detener los servicios que fueron levantados con `start-services.ps1`:

```powershell
.\scripts\local\stop-services.ps1
```

```bash
./scripts/local/stop-services.sh
```

Para revisar healthchecks:

```powershell
.\scripts\local\health.ps1
```

```bash
./scripts/local/health.sh
```

## Warp

En Warp, lo mas comodo es guardar `run-service.ps1` o `run-service.sh` como Workflow, segun el shell de la maquina, o crear Tab Configs que ejecuten un comando por pane.

Ejemplos de comandos para tabs/panes:

```powershell
.\scripts\local\up-infra.ps1
.\scripts\local\run-service.ps1 -Service course-service -SkipInfra
.\scripts\local\run-service.ps1 -Service enrollment-service -SkipInfra
.\scripts\local\run-service.ps1 -Service payment-service -SkipInfra
```

```bash
./scripts/local/up-infra.sh
./scripts/local/run-service.sh --service course-service --skip-infra
./scripts/local/run-service.sh --service enrollment-service --skip-infra
./scripts/local/run-service.sh --service payment-service --skip-infra
```

`-SkipInfra` / `--skip-infra` evita que cada pestana intente ejecutar `docker compose up -d`.

## Alternativas

Otra opcion transversal es agregar un task runner como `Taskfile.yml`, `justfile` o `Makefile`. No lo agregue ahora porque introduce una dependencia extra. Con `.ps1` + `.sh`, el repo queda usable en Windows, Linux y macOS con herramientas habituales del sistema.
