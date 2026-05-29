# @community-retention — Briefing FypMatch

**Ativar com:** `@community-retention *churn-radar fypmatch`

## Contexto
FypMatch tem base Android ativa. Maior risco: usuários Free sem match nos primeiros 7 dias → churn.
Meta: 20% conversão Free→Premium e D30 retention ≥ 25%.

## Segmentos de Usuários
| Segmento | Comportamento | Risco |
|----------|--------------|-------|
| Free sem match 3 dias | Sem swipe mútuo | CRÍTICO |
| Free com match sem conversa | Não iniciou chat | ALTO |
| Free 7 dias sem upgrade | Ativo mas não converteu | MÉDIO |
| Premium sem conversa 7 dias | Pagou mas não engajou | ALTO |
| VIP inativo | Cancelamento iminente | CRÍTICO |

## Alavancas de Engajamento
- **Créditos de IA** — percepção de valor antes de pagar
- **Anúncios rewarded** — 3 créditos por anúncio (max 9/dia) — ponte para Premium
- **Lista de espera** — sistema de convites como prova social
- **Push notifications** — FCM já configurado com deep linking

## Entregas

### 1. Mapa de Jornada + Churn Points
Documento: `apps/FypMatch/squads/retention-journey-map.md`
- Jornada completa com pontos de abandono
- Métricas-alvo por etapa

### 2. Sequência de Push Notifications (7 dias)
| Dia | Trigger | Mensagem | Deep Link |
|-----|---------|----------|-----------|
| 0 | install | Boas-vindas | /profile-setup |
| 1 | profile_complete | "Seu perfil está pronto" | /discovery |
| 2 | sem swipe | "X pessoas te estão esperando" | /discovery |
| 3 | sem match | Dica de otimização de perfil | /profile-edit |
| 5 | match sem chat | "Você tem matches esperando" | /matches |
| 7 | sem upgrade | Oferta especial 7 dias grátis | /premium |

### 3. Re-engagement (usuários inativos 3-7 dias)
- Template de push personalizado por segmento
- Oferta de créditos bônus como reativação

### 4. Upgrade Triggers (Free → Premium)
- Após 3º match sem poder ver quem curtiu
- Após usar todos os 9 créditos de anúncio
- Após 7 dias ativo sem converter

### 5. Programa de Embaixadores
- Usuários ativos há 30+ dias → convite para programa de afiliados
- Integração com sistema de afiliados (Fase 6 Fullstack)
