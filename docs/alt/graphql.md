# alt/graphql — API Gateway con GraphQL (BFF)

| | |
|---|---|
| Rama | `alt/graphql` |
| Bifurca de | `v2.0.0` / `v2/infrastructure` |
| Diferencia clave | GraphQL como BFF en el Gateway vs REST puro con múltiples endpoints |

## Qué cambia respecto a v2

v2 usa Spring Cloud Gateway como proxy de enrutamiento REST: cada cliente hace tantas llamadas HTTP como recursos necesita.

Esta alternativa agrega una capa **GraphQL** en el Gateway que actúa como BFF (*Backend For Frontend*): el cliente describe exactamente los datos que necesita en una sola query, y el servidor los ensambla llamando a los microservicios correspondientes.

## Qué explora

- `spring-boot-starter-graphql` con `graphql-java`
- Schema SDL: tipos, queries, mutations, resolvers
- Data fetchers que llaman internamente a los microservicios REST o gRPC
- Over-fetching y under-fetching: por qué REST los produce y cómo GraphQL los evita
- N+1 problem y DataLoader para batching de llamadas
- Introspección y GraphiQL como herramienta de exploración

## Cuándo tiene sentido

- Clientes (web/mobile) con necesidades de datos muy variadas o cambiantes
- Cuando over-fetching o under-fetching son problemas reales de rendimiento
- APIs orientadas a producto donde el frontend define qué datos necesita
- No recomendado cuando el caching HTTP por recurso es crítico o la API es pública y simple
