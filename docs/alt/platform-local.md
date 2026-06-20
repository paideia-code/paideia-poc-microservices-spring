# alt/platform-local — Plataforma completa sin costo cloud

| | |
|---|---|
| Rama | `alt/platform-local` |
| Bifurca de | `v7.0.0` / `v7/deployment` |
| Diferencia clave | Vault + ArgoCD + Terraform corriendo localmente (kind/minikube) sin cuenta cloud ni costo |

## Qué cambia respecto a v8

v8 (`v8/platform`) despliega en cloud real (AWS/Azure/GCP): Terraform provisiona infraestructura cloud, ArgoCD sincroniza sobre un clúster gestionado, Vault en modo producción. Requiere cuenta activa y genera costos.

Esta alternativa reproduce el mismo stack **completamente en local**:
- **kind** o **minikube** como clúster Kubernetes
- **Vault dev mode** para secretos dinámicos locales
- **ArgoCD** instalado en el clúster local, sincronizando desde un repositorio Git local o GitHub
- **Terraform** apuntando a providers locales (kind provider, local provider)

## Qué explora

- GitOps local: ArgoCD sincroniza el estado declarativo del clúster
- Vault dev: secretos dinámicos sin infraestructura cloud
- Terraform con kind provider: IaC sobre clústeres locales
- Diferencia entre "simular" la plataforma localmente y operarla en cloud real
- Costos operacionales y limitaciones de un entorno local para platform engineering

## Cuándo tiene sentido

- Aprender platform engineering sin cuenta cloud
- Demostrar el stack completo en una máquina local o en CI
- Como paso previo a v8 para validar configuraciones antes de pagar por infraestructura cloud
