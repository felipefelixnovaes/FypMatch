# FypMatch — Benefícios dos Planos de Assinatura

## Visão Geral

O FypMatch possui três níveis de acesso: **Grátis**, **Premium** e **VIP**.

---

## Plano Grátis

**Preço:** Gratuito

| Recurso                     | Limite               |
|-----------------------------|----------------------|
| Curtidas por dia            | 10                   |
| Super curtidas              | 0                    |
| Ver quem te curtiu          | Não                  |
| Boost de perfil             | Não                  |
| Créditos IA por dia         | 3 (via anúncio)      |
| Anúncios                    | Sim                  |
| Filtros avançados           | Não                  |
| Fotos no perfil             | Até 6                |
| Upload HD                   | Não                  |
| Analytics do perfil         | Não                  |
| Conversas simultâneas       | Até 3                |

---

## Plano Premium

**Preço:** R$ 19,90/mês  
**Badge:** Nenhum  

| Recurso                     | Limite / Detalhe              |
|-----------------------------|-------------------------------|
| Curtidas por dia            | 100                           |
| Super curtidas por dia      | 5                             |
| Ver quem te curtiu          | Sim                           |
| Boost de perfil             | 1 boost/mês (30 min)          |
| Créditos IA por dia         | 10 (sem anúncio)              |
| Anúncios                    | Não                           |
| Filtros avançados           | Sim                           |
| Fotos no perfil             | Até 12                        |
| Upload HD                   | Sim                           |
| Analytics do perfil         | Não                           |
| Conversas simultâneas       | Ilimitadas                    |
| Badge visual                | Não                           |
| Suporte                     | Padrão                        |

---

## Plano VIP ⭐ POPULAR

**Preço:** R$ 39,90/mês  
**Badge:** Selo VIP exclusivo no perfil  

| Recurso                     | Limite / Detalhe              |
|-----------------------------|-------------------------------|
| Curtidas por dia            | Ilimitadas                    |
| Super curtidas por dia      | Ilimitadas                    |
| Ver quem te curtiu          | Sim                           |
| Boost de perfil             | 5 boosts/mês (30 min) + Super Boost (2h) |
| Créditos IA por dia         | 25 (sem anúncio)              |
| Anúncios                    | Não                           |
| Filtros avançados           | Sim (prioridade no algoritmo) |
| Fotos no perfil             | Até 20                        |
| Upload HD                   | Sim                           |
| Analytics do perfil         | Sim — insights detalhados     |
| Conversas simultâneas       | Ilimitadas                    |
| Badge visual                | Selo VIP exclusivo            |
| Suporte                     | Prioritário (resposta em até 2h) |
| Prioridade no algoritmo     | Sim — aparece primeiro nas descobertas |

---

## Comparativo Rápido

| Recurso                | Grátis | Premium | VIP     |
|------------------------|--------|---------|---------|
| Curtidas/dia           | 10     | 100     | ∞       |
| Super curtidas/dia     | —      | 5       | ∞       |
| Boosts/mês             | —      | 1       | 5 + SB  |
| Créditos IA/dia        | 3*     | 10      | 25      |
| Fotos                  | 6      | 12      | 20      |
| Analytics              | —      | —       | Sim     |
| Selo VIP               | —      | —       | Sim     |
| Suporte prioritário    | —      | —       | Sim     |

*via anúncio

---

## Notas de Implementação

- O badge **"POPULAR"** deve aparecer **somente no plano VIP** (não no Premium).
- A seção "Recursos Premium" (Phase5FeaturesSection) em `PremiumScreen.kt` deve ser exibida para assinantes **Premium e VIP**.
- Os créditos IA são resetados diariamente à meia-noite (UTC-3).
- Super Boost (2h) é exclusivo do VIP — `BoostType.SUPER_BOOST`.
- Referências de modelo: `PremiumFeatures.kt`, `SubscriptionStatus`, `PhotoUploadLimits`.

---

*Última atualização: Sprint 5 — v1.0*
