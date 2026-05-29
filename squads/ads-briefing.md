# @ads-traffic-chief — Briefing FypMatch

**Ativar com:** `@ads-traffic-chief *research-protocol fypmatch`

## Contexto
App de relacionamento Android com base de usuários existente.
Fase atual: preparar estrutura de campanhas para lançamento iOS.

## Produto e Monetização
- App gratuito com freemium (R$19,90 / R$39,90)
- Receita por: assinaturas + créditos IA + anúncios rewarded
- LTV estimado: R$50-150/usuário convertido (6 meses de assinatura média)

## KPIs-Alvo
- CPI (Custo por Install): < R$3,50
- CAC (Custo por Assinante): < R$15
- D7 Retention: ≥ 35%
- D30 Retention: ≥ 25%
- Conversão FREE→Premium: 20%

## ICP Primário
- Homens e mulheres 20-32 anos
- Localizados em capitais BR (SP, RJ, BH, Curitiba, Porto Alegre)
- Interesses: LGBTQIA+, psicologia, ansiedade social, neurodiversidade, apps de relacionamento
- Comportamento: usuários ativos de Tinder/Bumble há 6+ meses (frustração com superficialidade)

## Ângulos Criativos para Testar
1. **Neurodiversidade** — "Um app que entende você, não só seu rosto"
2. **Tédio do Tinder** — "Cansado de matches que não viram conversa?"
3. **LGBTQIA+** — "Seu espaço, seu ritmo, sem julgamento"
4. **IA Conselheira** — "Primeiro app com conselheiro IA de relacionamentos"
5. **Autenticidade** — "Matches reais com score de compatibilidade"

## Entregas
1. Research Protocol completo (ICP, mercado, concorrência)
2. Estrutura de campanhas (ABO Meta + Google App Campaign)
3. 10 hooks para UGC/Reels
4. Creative Brief para 3 ângulos prioritários
5. Mapeamento de eventos Firebase Analytics por funil
6. Kill/Scale Rules (thresholds de corte e escala)

## Firebase Events a Configurar
`install` → `sign_up` → `profile_complete` → `first_swipe` → `first_match` →
`first_message` → `ai_credit_used` → `subscribe` → `vip_upgrade`
