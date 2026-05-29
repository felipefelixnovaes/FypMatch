# @fullstack-chief — Briefing FypMatch Fase 6

**Ativar com:** `@fullstack-chief *build fypmatch-fase6`

## Contexto
App Android/Kotlin já completo (Fases 1-5). Fase 6 = expansão multiplataforma.
Stack atual: Kotlin + Jetpack Compose + Firebase (Auth, Firestore, Storage, FCM).

## Missão Imediata
Implementar iOS/SwiftUI mantendo paridade de features com Android.

## Ordem de Execução
1. **@dev-mobile** → Setup Xcode + Firebase iOS SDK + Auth Google
2. **@dev-mobile** → SwipeCardsView (coração do app)
3. **@dev-firebase** → Firestore listeners iOS + FCM APNs
4. **@dev-mobile** → ChatView + DiscoveryView + ProfileView
5. **@qa-engineer** → Testes unitários + Firestore rules
6. **@devops-engineer** → CI/CD + App Store pipeline

## Arquivos de Referência
- `apps/FypMatch/ARQUITETURA.md` — stack e padrões
- `apps/FypMatch/IOS_IMPLEMENTATION_CHECKLIST.md` — 60+ arquivos a implementar
- `apps/FypMatch/IOS_ROADMAP_DETALHADO.md` — cronograma detalhado
- `apps/FypMatch/app/` — código Android de referência

## Gate de Saída
Build iOS verde + 3 telas funcionais (Welcome, Discovery, Chat) + Firebase conectado.
