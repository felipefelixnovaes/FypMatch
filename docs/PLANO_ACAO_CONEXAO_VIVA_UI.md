# Plano de Ação (UI/UX) - Conexão Viva

Este documento descreve as User Stories (tarefas) para a equipe de Front-end (Mobile/UI) materializar o MVP do recurso "Conexão Viva" na interface do FypMatch.

Todo o backend e arquitetura de dados (Firestore, LiveConnection, AI Prompt) já foram estruturados. O foco agora é construir as telas e componentes.

## Epics & User Stories

### Epic 1: O "Termômetro" no Chat
Como usuário, quero ver de forma sutil como minha conexão está evoluindo, sem sentir pressão de notas numéricas.

- **[UI-01] Componente de Status de Conexão no Header do Chat**
  - **O que é:** Uma tag ou barra fina embaixo do nome do Match na tela de Chat.
  - **Ação:** Consumir a propriedade `Match.connectionStatus`.
  - **Visual:** Se "WARMING_UP", ícone de semente 🌱. Se "ON_FIRE", ícone de fogo 🔥.
  - **Critério de Aceite:** Deve ser minimalista e não ocupar espaço de mensagens. Clicar nele abre a tela de "Radar da Conexão".

- **[UI-02] Mensagem do "Guia de Conexão" na Timeline**
  - **O que é:** Um "balão" flutuante e de cor neutra injetado no meio do chat.
  - **Ação:** Mostra o output gerado pela IA (ex: "Vocês já descobriram afinidades...").
  - **Critério de Aceite:** O balão não deve parecer uma mensagem de usuário (usar cor diferente/design de "sistema").

### Epic 2: A Tela "Radar da Conexão" (Mapa de Afinidade)
Como usuário VIP ou engajado, quero entender melhor nossas afinidades mapeadas.

- **[UI-03] Tela de Detalhes da Conexão (Bottom Sheet ou Modal)**
  - **O que é:** A tela que abre ao clicar no header do chat.
  - **Ação:** Exibir um gráfico simples (Radar/Aranha ou Barras de Progresso leves) lendo os dados de `LiveConnection.dimensions`.
  - **Visual:** Exibir os dados qualitativos ("Vocês combinam muito em Ritmo"). 
  - **Critério de Aceite:** Não exibir números de 0-100 para não gerar ansiedade de "prova de escola". Converter números em frases ou gradientes de cor.

### Epic 3: Cards Interativos (Dilemas do Match)
Como usuário com dificuldade de puxar assunto, quero jogar os "Dilemas" de forma fácil.

- **[UI-04] Botão "Missão de Conexão" (Gatilho)**
  - **O que é:** Um botão discreto perto do input de mensagem do chat (ex: ao lado do botão de anexar/foto).
  - **Ação:** Abre um carrossel rápido com opções de Dilemas para enviar.

- **[UI-05] Balão de Chat Interativo (Card de Dilema)**
  - **O que é:** Quando o usuário envia um dilema, ele aparece como um "Card" na conversa.
  - **Ação:** O outro usuário recebe e vê 3 opções clicáveis (A, B, C). Ao responder, a resposta é revelada para ambos, o evento `GAME_PLAYED` é despachado para o Firestore, e a UI celebra o match ("Vocês dois escolheram A!").
  - **Critério de Aceite:** A animação ao revelar a resposta igual deve ser gratificante (partículas, haptic feedback/vibração).

---

## Ordem de Implementação Recomendada
1. UI-01 (Mais fácil, agrega valor imediato).
2. UI-04 e UI-05 (Coração da gamificação, gera os eventos reais).
3. UI-02 (Mensagens injetadas de sistema).
4. UI-03 (Mais complexa visualmente, pode ser tratada numa Sprint v2 ou restrita apenas aos assinantes Premium/VIP).
