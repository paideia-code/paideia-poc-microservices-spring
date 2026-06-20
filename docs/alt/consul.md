# alt/consul — Service Discovery + Config con Consul

| | |
|---|---|
| Rama | `alt/consul` |
| Bifurca de | `v2.0.0` / `v2/infrastructure` |
| Diferencia clave | Consul unificado (discovery + config + KV + health) vs Eureka + Config Server separados |

## Qué cambia respecto a v2

v2 usa **Eureka** para service discovery y **Spring Cloud Config Server** para configuración externa. Son dos componentes independientes con dos dependencias distintas.

Esta alternativa reemplaza ambos con **Consul**, que los unifica en un solo agente:
- Service discovery: registro y resolución de servicios por nombre
- Key/Value store: reemplaza Config Server para propiedades externas
- Health checks: Consul comprueba activamente si los servicios responden

## Qué explora

- Consul agent, datacenter, registro de servicios
- `spring-cloud-starter-consul-discovery` y `spring-cloud-starter-consul-config`
- Diferencia entre health checks activos (Consul) vs pasivos (Eureka heartbeats)
- KV store de Consul como fuente de configuración vs repositorio Git en Config Server
- Cuándo un único componente de infraestructura es preferible a dos especializados

## Cuándo tiene sentido

- Si el equipo ya usa Consul en infraestructura (HashiCorp stack)
- Si se quiere reducir el número de componentes en el clúster
- Como punto de entrada a Vault (v8), que comparte el ecosistema HashiCorp
