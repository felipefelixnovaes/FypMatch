# FypMatch — Mobilização de Squads
# Status: ATIVO | Fase 6 — Expansão Multiplataforma
# Atualizado: 2026-05-28

---

## CONTEXTO DO APP

**FypMatch** é um app de relacionamento moderno e inclusivo (Android/Kotlin + Jetpack Compose + Firebase).
- **Design System:** Rosa `#E91E63` + Roxo `#9C27B0`, Material Design 3
- **Monetização:** FREE / Premium R$19,90/mês / VIP R$39,90/mês + créditos de IA
- **Diferencial:** Inclusão neural (neurodiversidade), selos LGBTQIA+, conselheiro IA de relacionamentos
- **Fase atual:** Fase 6 — Expansão para iOS (SwiftUI), Web PWA (Next.js), API pública e sistema de afiliados

### Fases concluídas
- ✅ Fase 1 — Auth + lista de espera + perfis
- ✅ Fase 2 — Swipe cards + matching + monetização
- ✅ Fase 3 — Chat em tempo real + FCM push notifications
- ✅ Fase 4 — IA avançada (MBTI, Big Five, assistente neural)
- ✅ Fase 5 — Funcionalidades premium (multi-fotos, filtros avançados, analytics)
- 🚀 Fase 6 — Em andamento (iOS SwiftUI, Web PWA, API REST, afiliados)

---

## SQUAD 1 — AIOX FULLSTACK (`@fullstack-chief`)

**Missão:** Liderar toda a implementação técnica da Fase 6.

### Agentes Ativos
| Agente | Responsabilidade |
|--------|-----------------|
| `@fullstack-chief` | Orquestra o pipeline, gates de qualidade, escalações |
| `@dev-mobile` | iOS/SwiftUI — port completo do Android para iOS |
| `@dev-firebase` | Firebase Functions, Firestore rules, FCM para iOS |
| `@qa-engineer` | Testes, coverage ≥70%, security scan, Firestore rules test |
| `@devops-engineer` | CI/CD GitHub Actions, App Store pipeline, Firebase Hosting |

### Backlog Prioritizado

#### P0 — iOS App (SwiftUI) — 8-12 semanas
```
FypMatch-iOS/
├── Models/          → User, Match, Message (Swift)
├── Views/           → SwipeCardsView, ChatView, ProfileView, DiscoveryView
├── ViewModels/      → AuthViewModel, DiscoveryViewModel, ChatViewModel (ObservableObject)
└── Services/        → FirebaseService, HealthKitService, PushNotificationService
```
- [ ] Setup projeto Xcode + Firebase SDK iOS
- [ ] AuthViewModel com Google Sign-In iOS
- [ ] SwipeCardsView com gesture recognizers nativos
- [ ] ChatView com realtime Firestore listener
- [ ] ProfileView com photo picker nativo
- [ ] FCM push notifications iOS (APNs)
- [ ] HealthKit integration
- [ ] App Store submission pipeline

#### P1 — Web PWA (Next.js 14)
- [ ] Setup Next.js 14 + Firebase Web SDK
- [ ] Autenticação Google
- [ ] Discovery page com swipe (touch/mouse)
- [ ] Chat em tempo real
- [ ] Design system compartilhado (tokens CSS)
- [ ] PWA manifest + service worker
- [ ] Firebase Hosting deploy

#### P2 — API REST Pública
- [ ] Endpoints OpenAPI 3.0
- [ ] Auth via Firebase tokens
- [ ] Rate limiting inteligente
- [ ] Webhooks para eventos (match, mensagem)
- [ ] Documentação interativa

#### P3 — Sistema de Afiliados
- [ ] Códigos únicos por afiliado
- [ ] Dashboard de ganhos (Firebase Functions)
- [ ] Comissões automáticas por assinatura convertida

### Gate de Qualidade
- Build passa + typecheck + lint ✓
- Coverage ≥70% em código novo ✓
- Firestore security rules testadas ✓
- Smoke test HTTP 200 em todas as rotas ✓

---

## SQUAD 2 — AIOX DESIGN (`@design-chief`)

**Missão:** Garantir consistência visual e acessibilidade em todas as plataformas.

### Contexto de Design
- **Cores primárias:** Rosa `#E91E63` / Roxo `#9C27B0`
- **Gradiente principal:** Linear Rosa → Roxo
- **Framework Android:** Material Design 3 (já implementado)
- **Framework iOS:** SwiftUI + Human Interface Guidelines
- **Framework Web:** Material-UI v5

### Entregas

#### Design System Multiplataforma
- [ ] Tokens de design exportáveis (cores, tipografia, espaçamentos, raios)
- [ ] Componentes SwiftUI equivalentes aos Compose existentes
  - SwipeCard → `CardView` SwiftUI com DragGesture
  - MatchBadge → SF Symbols equivalente
  - PremiumBadge → Material equivalente
- [ ] Componentes Web (CSS/MUI) alinhados ao Android
- [ ] Guia de gradientes para iOS/Web

#### Acessibilidade
- [ ] VoiceOver (iOS) — labels para todos os elementos interativos
- [ ] Screen reader (Web) — ARIA roles
- [ ] Contrastes WCAG AA verificados
- [ ] Tamanhos mínimos de toque (44px iOS, 48dp Android)

#### Assets
- [ ] Ícone do app para App Store (1024x1024)
- [ ] Screenshots App Store (6.7", 6.1", iPad)
- [ ] Onboarding illustrations iOS
- [ ] Loading states e empty states para Web

---

## SQUAD 3 — AIOX COPY (`@copy-chief`)

**Missão:** Criar toda a camada textual do FypMatch para conversão e engajamento.

### Contexto de Produto
- App de relacionamento inclusivo com diferencial de IA e neurodiversidade
- Público: 18-35 anos, LGBTQIA+, neurodiversos, pessoas que buscam conexões autênticas
- Tom: moderno, acolhedor, direto, sem jargões

### Entregas

#### App Store (iOS) — Alta Prioridade
- [ ] **Título:** FypMatch — Relacionamentos Reais (máx. 30 chars)
- [ ] **Subtítulo:** Conexões com IA e inclusão (máx. 30 chars)
- [ ] **Descrição curta** (máx. 170 chars) — gancho emocional + diferencial
- [ ] **Descrição completa** (máx. 4000 chars) — features, monetização, diferenciais
- [ ] **Keywords** — 100 chars, otimizadas para ASO

#### Google Play — Revisão
- [ ] Revisar/atualizar descrição existente com novos features (Fases 4 e 5)
- [ ] What's New para próxima versão

#### In-App Copy
- [ ] Onboarding (5 telas) — texto de boas-vindas e proposta de valor
- [ ] Push notifications — templates de match, mensagem nova, boost expirando
- [ ] Upgrade prompt — copy de conversão FREE → Premium → VIP
- [ ] Tela de afiliados — explicação do programa e CTA
- [ ] Empty states — sem matches, sem mensagens, perfil incompleto

#### Marketing
- [ ] Headlines para Meta Ads (5 variações)
- [ ] Headlines para Google App Campaigns (5 variações)
- [ ] Bio do app no TikTok/Instagram

---

## SQUAD 4 — GO-TO-MARKET WAR ROOM (`@gtm-war-room`)

**Missão:** Estratégia de posicionamento para lançamento iOS e expansão de mercado.

### Contexto Estratégico
- **Mercado:** Apps de relacionamento no Brasil — Tinder, Bumble, Grindr, Happn, OkCupid
- **Diferencial defensável:** Inclusão neural + selos LGBTQIA+ + conselheiro IA de relacionamentos
- **Momento:** Lançamento iOS se aproximando + Web PWA em desenvolvimento

### War Game — Perguntas-Chave
1. Como o Tinder destruiria o FypMatch nos próximos 12 meses?
2. Qual é o maior risco de churn nos primeiros 30 dias?
3. Quais features o Bumble ou Grindr poderiam copiar mais rápido?
4. Qual nicho é mais defensável: LGBTQIA+ ou neurodiversidade?

### Entregas
- [ ] **War Game Report** — vulnerabilidades vs. forças subestimadas
- [ ] **Plano de Posicionamento** — mensagem central e proof points por segmento
- [ ] **Estratégia de Lançamento iOS** — sequência de ativação (lista de espera → beta → público)
- [ ] **Mapa de Concorrência** — matriz de features vs. preço vs. público
- [ ] **Pricing Review** — R$19,90/R$39,90 competitivo? Recomendação baseada em benchmarks

### Hipóteses a Validar
- Nicho de neurodiversidade é subatendido e menos competitivo que LGBTQIA+
- Lista de espera como mecanismo de antecipação ainda funciona no Brasil em 2025
- Conselheiro IA é o diferencial mais monetizável a curto prazo

---

## SQUAD 5 — AIOX ADS (`@ads-traffic-chief`)

**Missão:** Estruturar campanhas de aquisição de usuários para Android + pré-lançamento iOS.

### Contexto
- **Plataformas:** Meta Ads (Instagram/Facebook) + Google App Campaigns + TikTok Ads
- **Objetivo imediato:** Crescer lista de espera iOS + usuários ativos Android
- **KPIs alvo:** CPI (Custo por Install) < R$3,50 | CAC < R$15 | D30 retention ≥ 25%

### Agentes
| Agente | Responsabilidade |
|--------|-----------------|
| `@campaign-manager` | Estrutura de campanhas e orçamento |
| `@creative-analyst` | Análise de criativos por ângulo |
| `@performance-analyst` | ROAS, CPI, métricas de retenção |
| `@pixel-specialist` | Firebase Analytics + Attribution tracking |

### Entregas

#### Fase Pré-Lançamento iOS (Agora)
- [ ] **Research Protocol** — ICP detalhado do usuário FypMatch BR
- [ ] **Ângulos criativos** (5) — neurodiversidade, LGBTQIA+, "tédio do Tinder", IA, autenticidade
- [ ] **Hook gallery** — 10 hooks para Reels/TikTok
- [ ] **Campaign Structure** — ABO vs. CBO, segmentações iniciais

#### Fase Lançamento iOS
- [ ] **App Install Campaign** — Meta + Google App Campaign configurados
- [ ] **Creative Bombardment** — 3 ad sets x 5 criativos por ângulo
- [ ] **Pixel/Firebase** — eventos: install, sign_up, first_swipe, first_match, subscribe
- [ ] **Kill/Scale Rules** — thresholds automáticos para otimização

#### Tracking
- [ ] Firebase Analytics events mapeados por funil
- [ ] Attribution configurada (GA4 + Firebase)
- [ ] Dashboard de performance (KPIs diários)

---

## SQUAD 6 — COMMUNITY RETENTION (`@community-retention`)

**Missão:** Maximizar engajamento, minimizar churn e converter usuários em embaixadores.

### Contexto
- **Usuários-alvo:** Free, Premium, VIP no Android (base atual)
- **Maior risco de churn:** Primeiros 7 dias sem match + usuários Free sem upgrade
- **Alavanca de monetização:** Créditos de IA → percepção de valor → upgrade Premium

### Comandos Ativos
- `*churn-radar` — identificar usuários em risco por sinais comportamentais
- `*ativar-usuario` — criar plano de ativação personalizado por segmento

### Entregas

#### Mapa de Jornada e Churn Points
- [ ] **Jornada completa:** Install → Cadastro → Primeiro swipe → Primeiro match → Primeira mensagem → 7 dias → 30 dias
- [ ] **Churn points identificados:** Onde usuários abandonam e por quê
- [ ] **Sinais de risco:** Comportamentos que precedem churn em apps de relacionamento

#### Estratégia de Ativação
- [ ] **Sequência de push notifications** — primeiros 7 dias (onboarding inteligente)
- [ ] **Re-engagement campaign** — usuários inativos 3-7 dias
- [ ] **Upgrade triggers** — momentos ideais para mostrar paywall Premium
- [ ] **Programa de embaixadores** — usuários power → afiliados orgânicos

#### Métricas de Acompanhamento
- D1/D7/D30 retention por segmento (Free/Premium/VIP)
- Taxa de conversão primeiro match < 48h
- Tempo médio até primeira mensagem após match
- Taxa de upgrade Free → Premium (objetivo: 20%)

---

## COORDENAÇÃO ENTRE SQUADS

```
CEO-SQUAD (oversight estratégico)
        │
        ├── GTM War Room ──────────────────────→ Posicionamento e estratégia
        │       │
        │       └──→ Copy Squad ───────────────→ Mensagens e copy derivadas da estratégia
        │               │
        │               └──→ Ads Squad ────────→ Criativos baseados no copy
        │
        ├── Fullstack Squad ──────────────────→ Desenvolvimento iOS + Web + API
        │       │
        │       └──→ Design Squad ────────────→ Componentes e assets para cada plataforma
        │
        └── Community Retention ─────────────→ Engajamento da base existente
```

### Dependências Críticas
- **Design → Fullstack:** Tokens e componentes SwiftUI antes de iniciar UI iOS
- **GTM → Copy:** Posicionamento definido antes de finalizar App Store copy
- **Copy → Ads:** Headlines e ângulos definidos antes de criar criativos
- **Fullstack → Ads:** Firebase Analytics configurado antes de ativar campanhas

### Checkpoint Semanal
- **Fullstack:** Progresso iOS (% de telas implementadas)
- **Design:** Componentes prontos para handoff
- **Copy:** Textos aprovados para App Store
- **GTM:** War Game Report entregue
- **Ads:** Estrutura de campanhas pronta para quando iOS estiver em beta
- **Retention:** Primeiros churn signals identificados

---

## LINKS RÁPIDOS

- App: `apps/FypMatch/`
- Arquitetura: `apps/FypMatch/ARQUITETURA.md`
- Roadmap: `apps/FypMatch/ROADMAP.md`
- Fase 6: `apps/FypMatch/FASE_6_INICIADA.md`
- iOS Checklist: `apps/FypMatch/IOS_IMPLEMENTATION_CHECKLIST.md`
- iOS Roadmap: `apps/FypMatch/IOS_ROADMAP_DETALHADO.md`
