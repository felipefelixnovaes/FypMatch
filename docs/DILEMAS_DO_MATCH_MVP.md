# Biblioteca MVP: Dilemas do Match

Esta biblioteca documenta os 5 dilemas iniciais (Missões de Conexão) criados para extrair dados comportamentais sutis e gamificar a experiência da funcionalidade "Conexão Viva" no FypMatch.

Cada dilema foca em um eixo comportamental específico. A linguagem foi desenhada para ser imersiva (estilo RPG da vida real) e fugir de testes psicológicos tradicionais.

## 1. Gestão de Conflito
*Mapeia: Como a pessoa reage a desentendimentos e resolve problemas interpessoais.*

> "Vocês tiveram um desentendimento bobo no final do encontro, o clima esfriou um pouco. Qual é o seu movimento na manhã seguinte?"

- A) Mando uma mensagem longa e bem resolvida pra não deixar o assunto pendente. (🔥 Perfil: Direto / Confronto maduro)
- B) Espero a outra pessoa falar pra sentir o clima, não quero forçar nada. (🌱 Perfil: Cauteloso / Observador)
- C) Finjo que nada aconteceu e chamo pra fazer algo divertido pra aliviar a tensão. (🎢 Perfil: Evita conflito / Foca na leveza)

## 2. Estilo de Comunicação
*Mapeia: Preferência por comunicação síncrona vs assíncrona, texto vs voz.*

> "O que você prefere quando a conversa pelo WhatsApp está ficando profunda e complexa?"

- A) Ligo ou mando um áudio, odeio mal-entendidos por texto. (🎙️ Perfil: Comunicação rica / Imediato)
- B) Deixo as mensagens profundas rendendo ao longo do dia, no nosso tempo. (⏳ Perfil: Assíncrono / Respeita espaço)
- C) Paro o assunto e marco de falar sobre isso pessoalmente tomando alguma coisa. (🥂 Perfil: Presencial / Valoriza o momento)

## 3. Individualidade (Limites Pessoais)
*Mapeia: A necessidade de proximidade física constante vs respeito ao espaço/independência.*

> "Domingo nublado, zero compromissos. Como você descreveria seu cenário ideal estando com alguém?"

- A) No sofá, maratonando série embaixo da mesma coberta o dia todo. (🛋️ Perfil: Simbiótico / Aconchego físico)
- B) Cada um num canto da sala fazendo suas coisas, mas curtindo a companhia. (📚 Perfil: Independente / Presença passiva)
- C) Pegando o carro de última hora pra ir almoçar numa cidade vizinha. (🚗 Perfil: Aventureiro / Espontâneo)

## 4. Flexibilidade
*Mapeia: Adaptação a frustrações e imprevistos na rotina.*

> "O encontro estava marcado para daqui a uma hora, mas o lugar fechou por causa da chuva. Qual é a sua reação automática?"

- A) Tento resolver rápido e sugerir um lugar parecido perto dali pra não perder a noite. (🧭 Perfil: Solucionador / Proativo)
- B) Proponho algo caseiro, um delivery e um filme. (🍕 Perfil: Adaptável / Conforto)
- C) Prefiro remarcar pra outro dia onde a gente possa fazer o plano original direito. (📅 Perfil: Planejador / Metódico)

## 5. Ritmo do Romance
*Mapeia: Intensidade emocional e velocidade de apego/investimento na relação.*

> "Vocês saíram, foi muito bom, e o assunto rendeu. Como costuma ser sua empolgação nos dias seguintes?"

- A) Acelero as mensagens, já quero marcar o próximo e fico pensando na pessoa. (🚀 Perfil: Intenso / Atração Imediata)
- B) Dou um passo de cada vez. Foi bom, mas prefiro manter meu ritmo normal pra ver se sustenta. (🐢 Perfil: Pé no chão / Conexão construída)
- C) Fico esperando a outra pessoa dar sinais mais claros antes de me soltar totalmente. (🛡️ Perfil: Defensivo / Precisa de segurança)

---
*Como o Engine Processa:*
As respostas alimentam indiretamente o `LiveConnectionEngine.kt`. Por exemplo, ambos responderem "A" na pergunta 2 aumenta consideravelmente o score de `Affinity` e `Communication Match`.
