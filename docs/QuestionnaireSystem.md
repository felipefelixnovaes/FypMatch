# FypMatch — Sistema de Questionários de Compatibilidade

> Documento de referência técnica e de produto para o sistema de questionários.  
> Versão: 1.0 — Sprint 5

---

## 1. Visão Geral do Sistema

O FypMatch utiliza compatibilidade baseada em psicologia real, dividida em três modos com propósitos distintos:

| Modo | Duração | Obrigatoriedade | Propósito |
|------|---------|-----------------|-----------|
| **Modo Rápido** | ~5 min | Obrigatório para todos | Filtragem essencial: personalidade (Big Five via TIPI-10), valores centrais, estilo de comunicação, rotina e deal-breakers. Habilita o matching básico. |
| **Modo Profundo** | ~20 min | Opcional | Compatibilidade psicológica aprofundada: personalidade completa (IPIP-50), valores (Schwartz PVQ), estilo de apego (ECR-RS), gestão de conflito e projeto de vida compartilhado. Eleva a qualidade do match significativamente. |
| **Modo Autoconhecimento** | ~10 min | Opcional | Dimensão lúdica e de autoexploração: Eneagrama, linguagens do cuidado (Chapman), arquétipos. Não impacta o score de compatibilidade diretamente — contribui apenas via Camada 3 (peso leve). |

Os três modos são cumulativos: cada modo adicional enriquece o perfil e melhora a precisão do match sem invalidar dados anteriores.

---

## 2. Módulos Implementados — Sprint 5 (Modo Rápido)

### 2.1 TIPI-10 — Big Five Abreviado (pt-BR)

**Escala:** 1 = Discordo totalmente → 7 = Concordo totalmente  
**Instrução ao usuário:** "Indique o quanto cada afirmação abaixo descreve você."

| # | Item | Fator | Direção |
|---|------|-------|---------|
| 1 | "Me vejo como: extrovertido(a) e entusiasmado(a)" | Extroversão | Direto |
| 2 | "Me vejo como: crítico(a) e conflituoso(a)" | Amabilidade | Reverso (R) |
| 3 | "Me vejo como: confiável e autodisciplinado(a)" | Conscienciosidade | Direto |
| 4 | "Me vejo como: ansioso(a) e facilmente perturbado(a)" | Neuroticismo | Direto |
| 5 | "Me vejo como: aberto(a) a novas experiências e curioso(a)" | Abertura | Direto |
| 6 | "Me vejo como: reservado(a) e quieto(a)" | Extroversão | Reverso (R) |
| 7 | "Me vejo como: simpático(a) e afetuoso(a)" | Amabilidade | Direto |
| 8 | "Me vejo como: desorganizado(a) e descuidado(a)" | Conscienciosidade | Reverso (R) |
| 9 | "Me vejo como: calmo(a) e emocionalmente estável" | Neuroticismo | Reverso (R) |
| 10 | "Me vejo como: convencional e pouco criativo(a)" | Abertura | Reverso (R) |

**Fórmulas de scoring** (resultado em escala 1.0–7.0):

```
rev(x) = 8 - x

Extroversão        = (item1 + rev(item6)) / 2
Amabilidade        = (rev(item2) + item7) / 2
Conscienciosidade  = (item3 + rev(item8)) / 2
Neuroticismo       = (item4 + rev(item9)) / 2
Abertura           = (item5 + rev(item10)) / 2
```

---

### 2.2 Valores — Ranking Top-3 (Schwartz Simplificado)

**Instrução ao usuário:** "Escolha os 3 valores mais importantes para você, em ordem de prioridade."

| Valor | Emoji | Descrição |
|-------|-------|-----------|
| Segurança | 🛡️ | Estabilidade, proteção e previsibilidade na vida |
| Liberdade | 🦋 | Autonomia, independência e autodeterminação |
| Família | 🏡 | Vínculos próximos, cuidado e pertencimento |
| Realização | 🏆 | Sucesso, competência e reconhecimento |
| Tradição | 🕯️ | Respeito ao legado, costumes e crenças herdadas |
| Hedonismo | 🎉 | Prazer, diversão e aproveitamento do presente |
| Benevolência | 💛 | Cuidar do próximo e promover o bem-estar alheio |
| Universalismo | 🌍 | Justiça social, sustentabilidade e igualdade |
| Poder | 👑 | Influência, liderança e status social |
| Conformidade | 🤝 | Harmonia social, respeito às normas e cooperação |

**Scoring de compatibilidade:**
- 1º valor coincide: +40 pontos
- 2º valor coincide: +35 pontos
- 3º valor coincide: +25 pontos
- Valor do parceiro está no top-3 do outro (ordem diferente): +10 pontos por sobreposição adicional
- Máximo: 100 pontos

---

### 2.3 Comunicação — 5 Cenários

**Instrução ao usuário:** "Escolha a opção que melhor representa você."

| # | Pergunta | Opções |
|---|----------|--------|
| 1 | "Quando estou chateado(a), prefiro:" | Conversar na hora / Esperar esfriar / Me afastar por um tempo |
| 2 | "Mensagens ao longo do dia:" | Gosto de frequência / Depende do contexto / Prefiro espaço |
| 3 | "Quando alguém some por horas:" | Normal, não me afeta / Fico um pouco ansioso(a) / Me preocupo muito |
| 4 | "Prefiro conversas:" | Diretas e objetivas / Longas e profundas / Mistura dos dois |
| 5 | "Conflito:" | Resolver na hora / Depois que a emoção baixa / Por escrito antes de falar |

**Compatibilidade:** calculada por distância entre perfis. Opções opostas (ex: "Resolver na hora" vs "Me afastar por um tempo") penalizam o score. Opções adjacentes ou iguais somam.

---

### 2.4 Rotina e Energia — 5 Itens

| # | Pergunta | Opções |
|---|----------|--------|
| 1 | "Fim de semana ideal:" | Em casa relaxando / Equilíbrio / Sair e explorar |
| 2 | "Sou mais:" | Pessoa de rotina / Espontâneo(a) / Depende do momento |
| 3 | "Me recarrego:" | Sozinho(a), preciso de silêncio / Com as pessoas |
| 4 | "Trabalho e vida:" | Trabalho muito, ambição alta / Equilíbrio / Priorizo qualidade de vida |
| 5 | "Em casa preciso de:" | Silêncio para me concentrar / Um pouco de som/movimento / Qualquer ambiente |

---

### 2.5 Deal-Breakers — Multi-select

**Instrução ao usuário:** "Selecione tudo que seria inaceitável para você em um relacionamento."

| Deal-Breaker | Ícone |
|---|---|
| Fumar | 🚬 |
| Beber excessivamente | 🍺 |
| Não quer filhos jamais | 🚫👶 |
| Quer filhos obrigatoriamente | 👶 |
| Religiões incompatíveis | ✝️☪️ |
| Politicamente muito oposto(a) | ⚖️ |
| Relacionamento aberto | 🔓 |
| Violência/agressividade | ⚡ |
| Infidelidade | 💔 |
| Muito diferente de ritmo de vida | 🕐 |

**Lógica:** Qualquer deal-breaker não satisfeito pelo parceiro resulta em `compatibilityScore = 0` (ver Camada 0).

---

## 3. Algoritmo de Compatibilidade Multi-Camada

O score final de compatibilidade é calculado em camadas hierárquicas. As camadas 1–3 só são computadas se a Camada 0 passar.

```
scoreTotal = (camada1 * 0.50) + (camada2 * 0.35) + (camada3 * 0.15)
```

Caso a Camada 0 falhe em qualquer critério: `scoreTotal = 0` e o par não é exibido.

---

### Camada 0 — Filtro Obrigatório (Bloqueio)

> Critérios binários. Uma única incompatibilidade zera o match.

| Critério | Lógica de verificação |
|---|---|
| **Idade** | Faixa etária do usuário A dentro da preferência do usuário B, e vice-versa |
| **Intenção** | Ambos buscam o mesmo tipo de relacionamento (sério, casual, networking) |
| **Localização** | Distância ≤ raio máximo definido por cada usuário |
| **Deal-Breakers** | Para cada deal-breaker marcado por A: verificar se B satisfaz o critério oposto |

Se `camada0 = BLOQUEIO` → `scoreTotal = 0`. O par não entra na fila de matches.

---

### Camada 1 — Compatibilidade Forte (50% do score final)

| Módulo | Peso | Fonte de dados |
|--------|------|----------------|
| Valores compartilhados | 25% | Ranking top-3 Schwartz (Modo Rápido) + PVQ completo (Modo Profundo) |
| Estilo de apego / vínculo | 15% | Estimativa via comunicação (Modo Rápido) + ECR-RS (Modo Profundo) |
| Comunicação + gestão de conflito | 10% | 5 cenários de comunicação (Modo Rápido) + módulo de conflito (Modo Profundo) |

**Nota sobre apego no Modo Rápido:** As perguntas 1, 3 e 5 do módulo de Comunicação funcionam como proxy de apego ansioso/evitante até que o ECR-RS (Modo Profundo) seja concluído. Ao concluir o Modo Profundo, os dados do ECR-RS substituem o proxy.

---

### Camada 2 — Compatibilidade Média (35% do score final)

| Módulo | Peso | Fonte de dados |
|--------|------|----------------|
| Personalidade Big Five / HEXACO | 20% | TIPI-10 (Modo Rápido) + IPIP-50 (Modo Profundo) |
| Rotina + energia | 15% | 5 itens de rotina (Modo Rápido) |

**Scoring de personalidade:** Compatibilidade não é identidade. Para Neuroticismo e Conscienciosidade, pares similares pontuam mais. Para Extroversão, pares complementares (introvertido + extrovertido moderado) também pontuam bem. O modelo usa uma função de distância euclidiana ponderada nos 5 fatores.

---

### Camada 3 — Compatibilidade Leve (15% do score final)

| Módulo | Peso | Fonte de dados |
|--------|------|----------------|
| Hobbies e interesses | 8% | Seleção de interesses no perfil |
| Linguagens do cuidado | 4% | Módulo Autoconhecimento (Chapman) |
| Eneagrama | 3% | Módulo Autoconhecimento |

---

### Camada 4 — Bônus de Atividade (desempate apenas)

> Não altera o score das camadas 1–3. Usado apenas para ordenar matches com score igual.

| Bônus | Critério |
|---|---|
| Online recentemente | Ativo nas últimas 24h |
| Perfil verificado | Verificação de identidade concluída |
| Foto principal | Possui foto de rosto aprovada |

---

## 4. Scoring por Módulo (0–100)

### TIPI-10

Cada fator é calculado na escala 1.0–7.0. Para comparação entre pares, é aplicada normalização min-max para 0–100 e depois uma função de compatibilidade:

```
compatibilidade(a, b) = 100 - (|a_normalizado - b_normalizado| * penalidade_por_fator)
```

| Fator | Penalidade (diferença máxima = 100%) | Observação |
|---|---|---|
| Extroversão | 60% | Complementaridade parcial aceita |
| Amabilidade | 80% | Alta penalidade por incompatibilidade |
| Conscienciosidade | 70% | Pares muito diferentes tendem a conflito |
| Neuroticismo | 75% | Dois altos = risco; dois baixos = ótimo |
| Abertura | 50% | Tolerância maior à diversidade |

Score do módulo = média ponderada dos 5 fatores → normalizada 0–100.

---

### Valores

Score calculado por sobreposição do top-3 (descrito na seção 2.2). Range: 0–100.

---

### Comunicação

5 itens com 3 opções cada. Compatibilidade por item: Mesmo = 100, Adjacente = 60, Oposto = 0.  
Score do módulo = média dos 5 itens.

---

### Rotina

5 itens com 2–3 opções. Compatibilidade por item: Mesmo = 100, Adjacente = 50, Oposto = 10.  
Score do módulo = média dos 5 itens.

---

### Deal-Breakers

Binário: qualquer incompatibilidade → módulo retorna BLOQUEIO (Camada 0).

---

## 5. LGPD — Compliance e Dados Sensíveis

### Dados sensíveis identificados

| Dado | Motivo de sensibilidade | Módulo de origem |
|---|---|---|
| Neuroticismo elevado | Indicativo implícito de saúde mental | TIPI-10 (item 4 e 9) |
| Ansiedade de apego | Indicativo de saúde mental / histórico de vínculos | Comunicação Q3 + ECR-RS (Modo Profundo) |
| Religião | Dado sensível explícito (Art. 11 LGPD) | Deal-breakers |
| Orientação para relacionamento aberto | Dado sobre vida sexual | Deal-breakers |

### Requisitos obrigatórios

1. **Consentimento explícito e granular:** Antes de iniciar qualquer módulo que colete dado sensível, apresentar tela de consentimento com linguagem clara (não no meio de um fluxo de onboarding).
2. **Armazenamento criptografado:** Todos os resultados de questionários armazenados com criptografia AES-256 em repouso. Dados de apego e saúde mental em campo separado com controle de acesso adicional.
3. **Direito ao esquecimento:** O usuário pode, a qualquer momento, deletar individualmente os resultados de cada módulo ou deletar a conta completa. A deleção remove os dados do banco principal em até 72h e dos backups em até 30 dias.
4. **Minimização de dados:** O sistema de match consome apenas os scores calculados (0–100 por módulo), nunca as respostas brutas dos itens.
5. **Pseudonimização:** Logs de matching não contêm userId real — apenas hash pseudônimo rotacionado a cada 30 dias.
6. **DPO e registro de tratamento:** Necessário nomear DPO e registrar o tratamento desses dados no RIPD (Relatório de Impacto à Proteção de Dados Pessoais) antes do lançamento público.

---

## 6. Roadmap de Implementação

### Sprint 5 — Modo Rápido (atual)
- [x] TIPI-10 (Big Five abreviado)
- [x] Valores top-3 (Schwartz simplificado — 10 valores)
- [x] Comunicação — 5 cenários
- [x] Rotina e energia — 5 itens
- [x] Deal-breakers — multi-select (10 itens)
- [x] Algoritmo de matching multi-camada (Camadas 0–4)
- [x] Modelos de dados Swift e Kotlin
- [ ] UI dos questionários (iOS + Android)
- [ ] Integração com o motor de matching no backend

### Sprint 6 — Modo Profundo
- [x] **IPIP-50 abreviado (IPIP-20)** — Big Five completo (20 itens representativos, escala 1–5)
- [x] **Schwartz PVQ-21** — Valores aprofundados (21 itens)
- [x] **ECR-RS** — Estilo de apego romântico (Experiences in Close Relationships — Revised Short; 12 itens, subescalas Ansiedade e Evitação)
- [x] Módulo de gestão de conflito profundo (6 dimensões)
- [x] Projeto de vida compartilhado (5 dimensões: filhos, localização, carreira, finanças, espiritualidade)
- [ ] Substituição do proxy de apego pelo ECR-RS no algoritmo
- [ ] UI dos módulos do Modo Profundo (iOS + Android)
- [ ] Integração com o motor de matching no backend

---

## 7. Módulos Implementados — Sprint 6 (Modo Profundo)

### 7.1 IPIP-20 — Big Five Completo (20 itens representativos)

**Escala:** 1 = Discordo totalmente → 5 = Concordo totalmente  
**Instrução ao usuário:** "Indique o quanto cada afirmação abaixo descreve você."

| Fator | Itens (0-based) | Itens reversos |
|-------|-----------------|----------------|
| Extroversão | 0, 1, 2, 3 | 1 |
| Amabilidade | 4, 5, 6, 7 | 5 |
| Conscienciosidade | 8, 9, 10, 11 | 9 |
| Neuroticismo | 12, 13, 14, 15 | 13 |
| Abertura | 16, 17, 18, 19 | 17 |

**Fórmulas de scoring:**

```
rev(x) = 6 - x      (escala 1–5)

Cada fator = média dos 4 itens do fator (com reversão aplicada nos itens reversos)
Normalização 0–100: (valor - 1) / 4 * 100
```

Resultado entregue como `BigFiveResult` com `version = "IPIP-20"`, compatível com o scoring do TIPI-10.

---

### 7.2 PVQ-21 — Schwartz Portrait Values Questionnaire (21 itens)

**Escala:** 1 = Muito parecido comigo → 6 = Não se parece nada comigo  
**Inversão de score:** `7 - resposta` (para que 6 = alto alinhamento com o valor)

| Valor | Itens (0-based) | Correspondência SchwartzValue |
|-------|-----------------|-------------------------------|
| Conformidade | 0, 10 | `conformity` |
| Tradição | 1, 11 | `tradition` |
| Benevolência | 2, 12 | `benevolence` |
| Universalismo | 3, 13, 19 | `universalism` |
| Autodireção | 4, 14 | `freedom` |
| Estimulação | 5, 15 | — |
| Hedonismo | 6, 20 | `hedonism` |
| Realização | 7, 16 | `achievement` |
| Poder | 8, 17 | `power` |
| Segurança | 9, 18 | `security` |

Score por valor = média das respostas mapeadas após inversão. `topValues` retorna os 3 valores com maior score. Ao concluir o PVQ-21, substitui o ranking top-3 simplificado do Modo Rápido no algoritmo de Camada 1.

---

### 7.3 ECR-RS — Estilo de Apego Romântico (12 itens)

**Escala:** 1 = Discordo totalmente → 7 = Concordo totalmente

| Subescala | Itens (0-based) | Score |
|-----------|-----------------|-------|
| Ansiedade | 0–5 | Média dos 6 itens |
| Evitação | 6–11 | Média dos 6 itens |

**Classificação `AttachmentStyle`:**

| Estilo | Condição | Emoji |
|--------|----------|-------|
| Seguro | Ansiedade ≤ 3.5 e Evitação ≤ 3.5 | 💚 |
| Ansioso | Ansiedade > 3.5 e Evitação ≤ 3.5 | 💛 |
| Evitante | Ansiedade ≤ 3.5 e Evitação > 3.5 | 🩶 |
| Desorganizado | Ansiedade > 3.5 e Evitação > 3.5 | 🔮 |

Ao concluir, substitui o proxy de apego das perguntas Q1/Q3/Q5 do módulo de Comunicação (Modo Rápido) no cálculo da Camada 1.

---

### 7.4 Conflito Profundo (6 dimensões)

**Instrução ao usuário:** "Escolha a opção que melhor representa você em cada situação."

| Dimensão | Opções |
|----------|--------|
| Estilo de resolução | Resolver na hora / Processar antes / Evitar e ceder / Entender a raiz |
| Expressão emocional | Com intensidade / Controlada / Guarda para si / Escreve antes |
| Comportamento de reparo | Imediatamente / Aos poucos / Espera o outro / Gesto/ato |
| Período de silêncio | Nenhum / Algumas horas / Pelo menos 1 dia / Alguns dias |
| Estilo de desculpa | Verbal direto / Atitudes / Fala + ações / Dificuldade |
| Tolerância a feedback | Receptivo / Depende do como / Sensível / Defensivo |

Complementa e aprofunda o módulo de Comunicação do Modo Rápido. Contribui para Camada 1 (Comunicação + gestão de conflito, 10%).

---

### 7.5 Projeto de Vida (5 dimensões)

**Instrução ao usuário:** "Escolha a opção que melhor representa seus planos e valores de vida."

| Dimensão | Opções |
|----------|--------|
| Desejo de filhos | Quero (prioridade) / Aberto(a) / Indeciso(a) / Provavelmente não / Não quero |
| Flexibilidade de localização | Minha cidade / Mesma cidade / Outro estado / Brasil / Mundo |
| Prioridade de carreira | Central / Equilibrada / Vida primeiro / Em transição |
| Abordagem financeira | Poupador / Equilibrado / Gasta bem hoje / Investidor |
| Papel da espiritualidade | Central / Importante / Pessoal / Não importante |

Contribui para Camada 1 via sobreposição de projeto de vida (especialmente `childrenDesire` e `locationFlexibility`, que funcionam como extensão da Camada 0 quando há incompatibilidade forte).

---

### Sprint 7 — Modo Autoconhecimento
- [ ] **Eneagrama** — 9 tipos (versão curta, 27 itens)
- [ ] **Linguagens do cuidado** — Chapman (5 linguagens, 30 itens)
- [ ] **Arquétipos** — versão interna FypMatch (12 arquétipos, 24 itens)
- [ ] Tela de perfil "Autoconhecimento" — exibição lúdica dos resultados
- [ ] Integração na Camada 3 do algoritmo

---

*Documento mantido pelo Architecture Squad do FypMatch. Atualizar a cada sprint.*
