# @design-chief — Briefing FypMatch Fase 6

**Ativar com:** `@design-chief *design-system fypmatch-ios`

## Contexto
Design system Android (Material Design 3) já implementado e funcionando.
Precisa ser portado para iOS (SwiftUI) e Web (CSS/MUI).

## Tokens Existentes (Android)
- Rosa: `#E91E63` (primary)
- Roxo: `#9C27B0` (secondary)
- Gradiente: LinearGradient Rosa → Roxo
- Border radius cards: 16dp
- Fonte: Material Design 3 defaults

## Entregas Prioritárias
1. **Design tokens** exportáveis (JSON/YAML) — cores, tipografia, espaçamentos
2. **SwiftUI components:**
   - `SwipeCardView` — equivalente ao `DiscoveryCard` Compose
   - `GradientButton` — botões com gradiente Rosa→Roxo
   - `PremiumBadge` / `VIPBadge` — selos de assinatura
   - `MatchCard` — card de match na lista
3. **Ícone App Store** — 1024x1024, fundo gradiente, símbolo FYP
4. **Screenshots App Store** — 6.7" e 6.1", 5 telas principais

## Referência Visual
Ver telas Android em `apps/FypMatch/app/src/main/java/` — copiar fidelidade visual,
adaptar aos padrões HIG do iOS.

## Acessibilidade
Todos os componentes com suporte a Dynamic Type (iOS) e VoiceOver labels.
