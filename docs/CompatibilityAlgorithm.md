# FypMatch — Algoritmo de Compatibilidade v1.0

## Visão Geral

O score de compatibilidade do FypMatch é calculado a partir de **6 dimensões ponderadas**, retornando um inteiro de 0 a 100. O objetivo é priorizar matches com mais chance de conexão real — não apenas aparência.

---

## Dimensões e Pesos

| # | Dimensão             | Peso | Descrição |
|---|----------------------|------|-----------|
| 1 | Interesses           | 30%  | Similaridade Jaccard de interesses + hobbies |
| 2 | Intenções            | 25%  | Alinhamento de objetivos (sério, casual, amizade) |
| 3 | Estilo de vida       | 20%  | Compatibilidade: fumo, bebida, filhos |
| 4 | Faixa etária         | 10%  | Usuário está dentro do range preferido do outro (bidirecional) |
| 5 | Valores              | 10%  | Idiomas compartilhados + religião |
| 6 | Bônus de atividade   | 5%   | Online agora, perfil verificado, fotos completas |

**Score final** = `Σ (score_dimensão × peso)`, arredondado para inteiro.

---

## Detalhes por Dimensão

### 1. Interesses (30%)
Usa **Jaccard Similarity** sobre a união dos arrays `interests` e `hobbies`:
```
Jaccard = |A ∩ B| / |A ∪ B|
score = Jaccard × 100
```
- ≥ 70%: "Vocês têm muitos interesses em comum!"
- 40–70%: "Alguns interesses compartilhados"
- < 40%: "Interesses diferentes — ótimo para descobrir coisas novas"

### 2. Intenções (25%)
Verifica se os campos de intenção são iguais (sério, casual, amizade, networking):
- Mesmo tipo = 100 pts
- Não informado = 55 pts (neutro)
- Diferente = 30 pts

### 3. Estilo de Vida (20%)
Média de 3 sub-scores:
- **Fumo**: igual=100, never+regularly=20, outros=60
- **Bebida**: igual=100, never+regularly=20, outros=60
- **Filhos**: ambos têm=80, ambos não=90, ambos querem o mesmo=85, incompatível=30

### 4. Faixa Etária (10%)
Verificação bidirecional:
- Ambos dentro da preferência do outro = 100
- Apenas um dentro = 60
- Nenhum dentro = 20

### 5. Valores (10%)
```
langScore = Jaccard(idiomas_a, idiomas_b)
relScore  = 1.0 (mesmo) | 0.7 (não informado) | 0.4 (diferente)
score     = (langScore × 0.6 + relScore × 0.4) × 100
```

### 6. Bônus de Atividade (5%)
| Condição | Pontos |
|----------|--------|
| Online agora (< 5 min) | 60 |
| Perfil verificado / completo | 20 |
| Fotos verificadas / ≥3 fotos | 20 |

---

## Interpretação do Score

| Score | Cor     | Significado |
|-------|---------|-------------|
| ≥ 80  | 🟢 Verde | Alta compatibilidade — match com alto potencial |
| 60–79 | 🟡 Amarelo | Compatibilidade moderada — vale a pena explorar |
| < 60  | ⚪ Cinza | Baixa compatibilidade — pode haver incompatibilidades importantes |

---

## Exemplos de Scores

| Cenário | Score Esperado |
|---------|----------------|
| Mesmo perfil de interesses + intenção + online | 90–100 |
| Interesses similares, intenções diferentes | 55–65 |
| Perfis opostos (não fuma vs fuma, filhos vs não quer) | 30–45 |
| Campos não preenchidos | ~50 (neutro por default) |

---

## Regras de Negócio

1. **Score mínimo para aparecer na descoberta**: não há filtro mínimo na v1.0 — todos os usuários são exibidos, mas ordenados por score descendente.
2. **Usuários com score < 30**: ainda aparecem mas ficam no final da fila de descoberta.
3. **Campos não preenchidos**: assumem valor neutro (não penalizam, não bonificam).
4. **Bloqueados e reportados**: nunca aparecem independente do score.

---

## Roadmap de Melhorias

### v1.1 — Aprendizado por comportamento
- Ajustar pesos baseado no histórico de swipe do usuário
- Se usuário da = like em perfis com X característica, aumentar peso dessa dimensão

### v1.2 — NLP em bios
- Comparar similaridade semântica entre bios usando embeddings leves (MobileNet/TFLite)
- Detectar tópicos de interesse no texto livre

### v2.0 — Modelo ML real
- Treinar modelo supervisionado com dados de matches bem-sucedidos
- Features: todas as dimensões atuais + comportamento + NLP bio + preferências implícitas
- Target: "conversa iniciada e mantida por > 7 dias"

---

## Implementação

| Plataforma | Arquivo | Método principal |
|------------|---------|-----------------|
| iOS | `Sources/Services/CompatibilityEngine.swift` | `calculate(current:target:) -> CompatibilityResult` |
| Android | `model/CompatibilityML.kt` | `analyzeCompatibility(currentUser:targetUser:) -> CompatibilityScore` |

**Nota**: iOS retorna score como `Int` (0-100). Android retorna como `Float` (0.0-1.0) por compatibilidade com código legado.

---

*Última atualização: Sprint 3 — fonte da verdade para scores de compatibilidade*
