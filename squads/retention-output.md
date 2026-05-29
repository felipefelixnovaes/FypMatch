# FypMatch — Retention Strategy Output
# @community-retention | 2026-05-28

---

## 1. MAPA DE JORNADA + CHURN POINTS

```
INSTALL
   │
   ▼ [D0] ──────────────────────── Churn Point 1: Perfil incompleto → abandono
SIGN UP + PERFIL
   │ Meta: < 10 min até perfil completo
   ▼ [D0-D1]
PRIMEIRO SWIPE
   │ Meta: acontece nas primeiras 2h
   ▼ [D1-D3] ─────────────────────── Churn Point 2: Sem match em 3 dias → desânimo
PRIMEIRO MATCH
   │ Meta: D3 para 60% dos usuários
   ▼ [D2-D5] ─────────────────────── Churn Point 3: Match sem conversa (48h) → match perdido
PRIMEIRA MENSAGEM
   │ Meta: < 48h após match
   ▼ [D3-D7]
USO DO CONSELHEIRO IA
   │ Meta: 40% dos usuários usam na primeira semana
   ▼ [D5-D7] ─────────────────────── Churn Point 4: Créditos esgotados sem upgrade → abandono
PRIMEIRO GATILHO DE UPGRADE
   │ Meta: 3 matches sem ver quem curtiu → paywall natural
   ▼ [D7] ──────────────────────────── Churn Point 5: 7 dias sem converter → usuário entrando em inatividade
CONVERSÃO FREE → PREMIUM
   │ Meta: 20% até D30
   ▼ [D8-D30]
USUÁRIO RETIDO
   │ Meta: D30 retention ≥ 25%
   ▼ [D30+]
EMBAIXADOR / AFILIADO
     Meta: usuários ativos 30+ dias → convite para programa
```

### Métricas-alvo por etapa

| Etapa | Métrica | Target | Alarme (abaixo de) |
|-------|---------|--------|-------------------|
| Install → Perfil completo | Taxa de conclusão | ≥ 70% | < 50% |
| Perfil → Primeiro swipe | Tempo médio | < 2h | > 24h |
| D3 com match | % usuários | ≥ 60% | < 40% |
| Match → Primeira mensagem | Tempo médio | < 48h | > 72h |
| D7 Retention | % usuários ativos | ≥ 35% | < 20% |
| D30 Retention | % usuários ativos | ≥ 25% | < 15% |
| Free → Premium | Conversão | ≥ 20% | < 10% |
| Premium → VIP | Conversão | ≥ 15% | < 5% |

---

## 2. SEQUÊNCIA DE PUSH NOTIFICATIONS — PRIMEIROS 7 DIAS

### Regras gerais
- Máx. 1 push/dia nos primeiros 7 dias
- Janela de envio: 18h-21h (maior taxa de abertura)
- Sempre com deep link para a ação esperada
- Personalizar com primeiro nome quando disponível

### Sequência completa

| Dia | Trigger de Envio | Título | Corpo | Deep Link | Segmento |
|-----|-----------------|--------|-------|-----------|---------|
| **D0** | Install + perfil < 50% | `Seu perfil está quase pronto 🌟` | `Adicione uma foto e bio para que as pessoas certas te encontrem.` | `/profile-edit` | Todos |
| **D1** | Perfil completo, sem swipe | `{X} pessoas perto de você estão no app agora 👀` | `Comece a descobrir — você pode se surpreender.` | `/discovery` | Perfil completo |
| **D2** | < 5 swipes no D1 | `Seu próximo match pode estar esperando` | `Que tal dar mais alguns swipes hoje? A probabilidade aumenta com o tempo no app.` | `/discovery` | Pouco uso |
| **D3** | Sem match ainda | `Dica: perfis com 3+ fotos têm 3x mais matches 📸` | `Adicione mais fotos e veja a diferença. É rápido.` | `/profile-edit` | Sem match |
| **D3** | Tem match, sem chat | `💕 Você tem um match esperando!` | `{Nome} curtiu você de volta. Não deixa esfriar — diga oi.` | `/matches` | Com match |
| **D4** | Sem atividade | `Seu conselheiro IA tem algo para você 🤖` | `Use seus créditos grátis para descobrir como aumentar suas chances de match.` | `/ai-counselor` | Inativo |
| **D5** | Créditos IA não usados | `9 créditos de IA esperando por você 💡` | `Assista a um vídeo curto e ganhe créditos para falar com seu conselheiro. É de graça.` | `/ai-counselor` | Não usou IA |
| **D6** | Match sem conversa 48h+ | `⏰ Seu match ainda está por aqui` | `{Nome} ainda não te esqueceu. Que tal uma mensagem curta?` | `/chat/{matchId}` | Match frio |
| **D7** | Ativo, sem upgrade | `✨ Você merece mais do FypMatch` | `Veja quem curtiu você, tenha 100 curtidas por dia e sem anúncios. 7 dias grátis.` | `/premium?trial=true` | Free ativo |
| **D7** | Premium, sem conversa | `Você está aproveitando o Premium? 💎` | `Explore os filtros avançados e aumente a qualidade dos seus matches.` | `/filters` | Premium inativo |

---

## 3. RE-ENGAGEMENT — USUÁRIOS INATIVOS (3-7 dias sem abrir)

### Segmento: Inativo 3 dias (já teve match)

**Push:**
- Título: `{Nome} ainda está esperando 💬`
- Corpo: `Você tinha um match promissor. Ainda dá tempo de retomar.`
- Deep link: `/matches`
- Oferta: nenhuma (urgência natural)

### Segmento: Inativo 5 dias (nunca teve match)

**Push:**
- Título: `Novos perfis perto de você 🗺️`
- Corpo: `Toda semana chegam novos usuários. Desta vez pode ser diferente.`
- Deep link: `/discovery`
- Oferta: nenhuma

### Segmento: Inativo 7 dias (qualquer)

**Push:**
- Título: `Sentimos sua falta 💜`
- Corpo: `Ganhe 5 créditos de IA bônus só por voltar hoje. Seu conselheiro tem novidades.`
- Deep link: `/discovery`
- Oferta: **+5 créditos IA bônus** (expira em 24h)

### Segmento: Premium inativo 7 dias

**Push:**
- Título: `Você está pagando e não aproveitando 💎`
- Corpo: `Seu Premium está ativo. Use Boost agora e apareça para 10x mais pessoas hoje.`
- Deep link: `/boost`
- Urgência: "Boost gratuito disponível por 2h"

---

## 4. UPGRADE TRIGGERS — FREE → PREMIUM

### Trigger 1: Tentou ver quem curtiu (após 3 curtidas recebidas)
```
Momento: usuário toca em "Ver quem curtiu"
Frequência: máx 1x/semana para o mesmo usuário

Modal:
  Título: "{N} pessoas curtiram você 💕"
  Body: "Veja quem são e responda antes que percam o interesse."
  Badge: "⭐ Mais popular agora"
  CTA primário: "Ver todos — R$19,90/mês"
  CTA secundário: "Continuar no gratuito"
  Rodapé: "Cancele quando quiser. Sem fidelidade."
```

### Trigger 2: Créditos IA esgotados (usou os 9 créditos do dia)
```
Momento: tenta enviar mensagem ao conselheiro com 0 créditos
Frequência: sempre que tentar

Bottom Sheet:
  Título: "Seus créditos de hoje acabaram 🤖"
  Body: "Você já usou bem o conselheiro! Para continuar agora:"
  Opção A: "Assistir 1 anúncio → +3 créditos (gratuito)"
  Opção B: "Ir Premium → 10 créditos todo dia, sem anúncios"
  Rodapé: "Créditos gratuitos renovam amanhã às 00h"
```

### Trigger 3: Limite de curtidas atingido (10/dia no free)
```
Momento: swipe com 0 curtidas restantes
Frequência: 1 modal + só push nos dias seguintes

Modal:
  Título: "Curtidas do dia esgotadas 🃏"
  Body: "Você curtiu bastante hoje! Com Premium são 100 curtidas por dia — 10x mais chances."
  CTA primário: "Desbloquear Premium"
  CTA secundário: "Voltar amanhã"
  Timer: "Renova em {horas}h {min}min"
```

### Trigger 4: 7 dias ativo sem upgrade (oferta de trial)
```
Momento: sétimo dia de uso consecutivo (via push D7)
Frequência: 1x apenas

Tela especial:
  Headline: "7 dias com a gente 🎉"
  Subhead: "Você já faz parte do FypMatch. Que tal experimentar o Premium?"
  Oferta: "7 dias grátis, cancele quando quiser"
  CTA: "Ativar teste grátis"
  Fine print: "Após o período, R$19,90/mês. Cancele antes e não paga nada."
```

---

## 5. PROGRAMA DE EMBAIXADORES → AFILIADOS

### Critérios de elegibilidade
- Conta ativa há ≥ 30 dias
- ≥ 1 assinatura ativa (Premium ou VIP)
- ≥ 3 sessões nos últimos 14 dias
- Perfil completo (foto + bio preenchidos)

### Fluxo de convite
```
D30 do usuário elegível:
  Push: "Você já faz parte do FypMatch há 30 dias 💜"
  Corpo: "Quer ganhar dinheiro indicando amigos? Conheça nosso programa."
  Deep link: /affiliates/invite
  
Tela de afiliados:
  Headline: "Indique e ganhe"
  Mecânica: Cada indicação que vira assinante = R$X de comissão
  Código único: FELIX30 (baseado no nome + dias no app)
  Dashboard: installs, conversões, ganhos acumulados
```

### Estrutura de comissões (sugerida)
| Conversão | Comissão |
|-----------|----------|
| Indicação → Install | R$0 (só install não paga) |
| Install → Cadastro | R$0,50 |
| Cadastro → Premium | R$5,00 |
| Cadastro → VIP | R$8,00 |
| Renovação mês 2+ | R$1,00/mês recorrente |

---

## 6. CHURN RADAR — SINAIS DE RISCO

### Sinais de alto risco (acionar re-engagement em 24h)

| Sinal | Segmento | Ação |
|-------|---------|------|
| 3 dias sem abrir + tem match não respondido | Free/Premium | Push "Match esperando" |
| Cancelou notificações | Qualquer | In-app banner na próxima sessão |
| Abriu o app, não fez nenhuma ação | Qualquer | Push no dia seguinte com dica |
| Usou todos os créditos + não assistiu anúncio | Free | Push oferta bônus |
| Premium: não abriu em 5 dias | Premium | Push urgência "Boost gratuito" |
| VIP: não abriu em 3 dias | VIP | Push personalizado + oferta suporte |

### Dashboard de saúde (Firebase Analytics)
```
Evento: churn_risk_detected
Parâmetros:
  - user_segment: free|premium|vip
  - days_inactive: number
  - has_pending_match: boolean
  - credits_remaining: number
  - last_action: string
```

---

## 7. MÉTRICAS DE ACOMPANHAMENTO SEMANAL

| Métrica | Cálculo | Meta |
|---------|---------|------|
| Install → D1 Active | Usuários que abriram D1 / Installs | ≥ 60% |
| D7 Retention | Usuários ativos D7 / Installs D0 | ≥ 35% |
| D30 Retention | Usuários ativos D30 / Installs D0 | ≥ 25% |
| Match Rate D7 | Usuários com ≥1 match / Installs D0 | ≥ 50% |
| Chat Conversion | Matches com ≥1 msg / Total matches | ≥ 60% |
| IA Activation | Usuários que usaram IA / Installs | ≥ 40% |
| Free→Premium D30 | Assinantes / Installs D0 | ≥ 20% |
| Push CTR | Cliques / Envios | ≥ 8% |
| Trial→Paid | Pagantes após trial / Trials iniciados | ≥ 50% |
