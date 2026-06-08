# Edge Cases & Migração - Conexão Viva

Este documento cobre os pontos cegos e requisitos não funcionais para o lançamento da funcionalidade "Conexão Viva", garantindo que a feature entre no ar sem quebrar a experiência dos usuários existentes.

## 1. Gatilhos Orgânicos no Chat (Frontend)
Atualmente, o motor (`LiveConnectionEngine`) processa notas perfeitamente, mas **precisamos garantir que a UI envie os eventos do dia a dia.**
- **Ação:** A squad de Frontend/Android deve adicionar chamadas ao repositório dentro do fluxo de envio padrão de mensagens.
- **Exemplo:** Ao apertar "Enviar" em uma mensagem de texto, disparar `logConnectionEvent(..., MESSAGE_SENT)`.
- Se o usuário enviar uma foto, disparar `MEDIA_SHARED`.
- *Atenção:* Sem isso, a conexão só evoluirá quando eles jogarem os dilemas, matando o propósito do "Radar da Conexão" orgânico.

## 2. Estratégia de Migração (Matches Antigos)
Se ativarmos a feature hoje, pessoas que já conversam há meses no FypMatch começarão com o termômetro no status `ICE_COLD` ou `WARMING_UP` com nota zero, o que vai gerar frustração.
- **Ação:** Criar um Worker/Script de migração no servidor ou rodar no app uma vez.
- **Lógica Simples:** Ler a quantidade de mensagens no `ChatRepository` para cada match antigo. Se o match tiver >50 mensagens, inicializar a conexão no Firestore direto como `ACTIVE` (nota 55). Se tiver >100, iniciar como `ON_FIRE` (nota 85).
- Evitar processamento retroativo pelo LLM (muito caro). Migrar usando apenas volumetria básica.

## 3. Retenção via Push Notifications (FCM)
A alteração de status do radar ocorre de forma silenciosa no background.
- **Ação:** Quando o `LiveConnectionEngine` calcular o novo Score e notar que o `status` mudou de fase (ex: `ACTIVE` -> `ON_FIRE`), o backend/Firebase Functions deve disparar um Push Notification.
- **Copy Sugerido:** "Sua conexão com [Nome] subiu de nível! 🔥 Que tal aproveitar a sintonia para um próximo passo?"

## 4. Firebase Analytics (Tracking de Produto)
O banco de dados salva a conexão, mas o time de negócio não sabe se a feature fez sucesso.
- **Ação:** A squad Mobile deve disparar eventos do Firebase Analytics para o Mixpanel/DataStudio.
- **Eventos vitais a rastrear:** 
  - `game_mission_clicked` (clicou no botão de enviar dilema).
  - `radar_sheet_opened` (abriu a tela detalhada de status).
  - `ai_hint_viewed` (visualizou a dica da IA no meio do chat).
