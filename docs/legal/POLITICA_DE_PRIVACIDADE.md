# Política de Privacidade — FypMatch

> **RASCUNHO — requer revisão jurídica antes de publicar.** Este documento foi gerado a partir do código-fonte atual do app (models `UserProfile`, `Message`, `PhotoVerification`, Cloud Functions e ADR de privacidade de IA) e cobre LGPD (Lei 13.709/2018), já que o app opera em `southamerica-east1` e atende usuários no Brasil. Trechos marcados com ⚠️ precisam de validação de advogado antes da publicação na Google Play / App Store.

**Última atualização:** [preencher na publicação]
**Controlador dos dados:** [Razão social / CNPJ / endereço — ⚠️ preencher]
**Encarregado de Dados (DPO):** [nome e e-mail de contato — ⚠️ obrigatório pela LGPD, art. 41]
**Contato:** privacidade@fypmatch.com.br [⚠️ confirmar domínio/e-mail real]

---

## 1. Sobre o FypMatch

O FypMatch é um aplicativo de relacionamento e conexão social destinado exclusivamente a maiores de 18 anos. Esta política explica quais dados coletamos, por que coletamos, com quem compartilhamos e quais direitos você tem sobre eles.

Ao criar uma conta, você confirma que tem 18 anos ou mais. Não coletamos intencionalmente dados de menores de idade.

---

## 2. Dados que coletamos

### 2.1 Dados de cadastro e perfil
- Nome, idade, e-mail (via login Google), fotos de perfil, biografia
- Intenção de relacionamento, interesses, educação, profissão, altura
- Status de relacionamento, se tem/deseja filhos, hábitos (tabagismo, consumo de álcool)
- Filmes, livros, músicas, hobbies, esportes, idiomas, países visitados, preferência por pets
- Resultados de testes de autoconhecimento (eneagrama, arquétipo de personalidade, linguagem do amor)

### 2.2 Dados sensíveis (LGPD art. 5º, II)
Coletamos as seguintes categorias de **dados pessoais sensíveis**, que tratamos com base no seu **consentimento explícito e destacado** (não em legítimo interesse):

- **Orientação sexual e identidade de gênero** — usados para o algoritmo de correspondência (matching)
- **Religião** — campo opcional de perfil
- **Geolocalização precisa** (latitude/longitude via GPS do dispositivo) — usada para calcular distância entre usuários e sugerir a cidade mais próxima. Você pode ativar um "modo viagem" para buscar em outra localização
- **Dado biométrico (imagem facial/selfie)** — coletado exclusivamente para o processo de verificação de perfil (ver seção 2.4)
- **Dados de saúde/hábitos** (tabagismo, consumo de álcool) — campos opcionais de perfil

⚠️ *Advogado deve confirmar se o texto de consentimento no onboarding do app captura, de forma clara e destacada (não genérica), a autorização para cada uma dessas categorias separadamente, conforme exige a LGPD.*

### 2.3 Fotos
Fotos de perfil são armazenadas no Firebase Storage (Google Cloud, América do Sul). Você pode remover suas fotos a qualquer momento nas configurações do app.

### 2.4 Verificação de perfil (selfie)
Para confirmar que as fotos do seu perfil correspondem a você, oferecemos um processo de verificação: você envia uma selfie, que é comparada manualmente (por um analista humano, não por reconhecimento facial automatizado) às fotos do seu perfil.

- A selfie é armazenada em um caminho privado e **excluída automaticamente após 7 dias**
- Nenhuma verificação de documento de identidade é realizada — apenas comparação visual da selfie
- É registrada a versão do termo de consentimento aceito no momento do envio

### 2.5 Mensagens e chat
Conversas trocadas com outros usuários (texto, imagens, áudio, vídeo, localização compartilhada, figurinhas) são armazenadas em nossos servidores (Firebase Firestore) para permitir o funcionamento do chat, histórico de conversa e recursos de segurança (denúncia/bloqueio).

- **Não aplicamos moderação automatizada por IA sobre o conteúdo das mensagens trocadas entre usuários.** Mensagens só são revisadas manualmente em caso de denúncia.
- Isso é diferente do recurso "Conselheiro" (seção 2.6), que é uma conversa com uma IA, não com outro usuário.

### 2.6 Conselheiro de IA
O FypMatch oferece um recurso opcional de chat com um assistente de IA para orientação sobre relacionamentos. Antes de enviar seu texto ao provedor de IA:

- Removemos automaticamente e-mail, telefone, nome, foto e identificador de usuário do texto (redação/sanitização)
- Seu identificador é substituído por uma referência pseudonimizada, gerada especificamente para o provedor de IA — o provedor nunca recebe seu nome, e-mail, foto ou UID reais

*(Medida de minimização de dados documentada internamente como "AI Privacy Boundary".)*

### 2.7 Dados de dispositivo e uso
- Identificador de instalação/token de notificação push (Firebase Cloud Messaging)
- Dados de diagnóstico e falhas (Firebase Crashlytics)
- Dados de uso/analytics (Firebase Analytics)
- Dados de exibição de anúncios recompensados (Google AdMob), quando você opta por assistir anúncios para ganhar créditos

---

## 3. Como usamos seus dados

| Finalidade | Dados envolvidos | Base legal (LGPD) |
|---|---|---|
| Criar e manter sua conta | Nome, e-mail, foto de perfil | Execução de contrato (art. 7º, V) |
| Algoritmo de correspondência (matching) | Orientação sexual, localização, preferências, interesses | Consentimento (art. 7º, I / art. 11) |
| Verificação de perfil e segurança | Selfie | Consentimento explícito (art. 11) |
| Funcionamento do chat | Mensagens, mídia | Execução de contrato |
| Recurso de IA (Conselheiro) | Texto sanitizado + ID pseudonimizado | Consentimento |
| Anúncios recompensados | Dados de exibição de anúncio | Consentimento |
| Segurança, prevenção a fraude, denúncias | Perfil, mensagens denunciadas, histórico de bloqueio | Legítimo interesse / cumprimento legal |
| Diagnóstico técnico | Logs de falha (Crashlytics) | Legítimo interesse |
| Comunicações do app (push) | Token FCM | Execução de contrato |

---

## 4. Com quem compartilhamos dados

Não vendemos seus dados pessoais. Compartilhamos dados apenas com:

- **Google Firebase** (Auth, Firestore, Storage, Cloud Functions, Cloud Messaging, Crashlytics, Analytics) — infraestrutura de backend, hospedada na região América do Sul (`southamerica-east1`)
- **Google AdMob** — exibição de anúncios recompensados (opcional, você escolhe assistir)
- **Provedor de IA do Conselheiro** (via gateway próprio) — recebe apenas texto sanitizado e ID pseudonimizado, nunca dados diretamente identificáveis
- **Outros usuários** — informações do seu perfil público (nome, idade, fotos, bio, interesses) são visíveis a outros usuários do app conforme suas preferências de descoberta
- **Autoridades**, quando exigido por lei ou ordem judicial

⚠️ *Advogado deve confirmar se há Data Processing Agreement (DPA) formal com Google/Firebase e com o provedor de IA, exigido pela LGPD para operadores de dados.*

**Pagamentos:** atualmente o FypMatch **não processa cobranças reais** — o catálogo de assinaturas/créditos está em fase de implementação. Esta política será atualizada quando o Google Play Billing for integrado.

---

## 5. Retenção e exclusão de dados

- **Selfies de verificação**: excluídas automaticamente após 7 dias
- **Dados de perfil e mensagens**: mantidos enquanto sua conta estiver ativa
- **Exclusão de conta**: você pode solicitar a exclusão da sua conta e dos dados associados a qualquer momento, pelo app ou pelo contato informado nesta política. A exclusão é auditável e processada por nossa equipe.

⚠️ *Confirmar com a equipe técnica o prazo real de exclusão completa (inclusive backups) e se mensagens trocadas com outros usuários são removidas do lado deles também.*

---

## 6. Seus direitos (LGPD art. 18)

Você tem direito a:
- Confirmar a existência de tratamento de dados
- Acessar seus dados
- Corrigir dados incompletos, inexatos ou desatualizados
- Solicitar anonimização, bloqueio ou eliminação de dados desnecessários
- Solicitar portabilidade dos dados
- Revogar o consentimento a qualquer momento
- Solicitar informações sobre com quem compartilhamos seus dados
- Se opor a tratamento realizado com base em legítimo interesse

Para exercer esses direitos, entre em contato pelo e-mail informado no topo desta política.

---

## 7. Segurança

Adotamos medidas técnicas e organizacionais para proteger seus dados, incluindo armazenamento em caminhos privados por usuário, regras de acesso do Firestore/Storage restritas por autenticação, e verificação manual (não automatizada) para o processo de checagem de perfil.

⚠️ *Recomenda-se anexar aqui um resumo não técnico das regras de segurança do Firestore/Storage, revisado por quem mantém o backend.*

---

## 8. Transferência internacional de dados

Seus dados podem ser processados em servidores do Google Cloud/Firebase. ⚠️ *Confirmar quais regiões são efetivamente usadas além de `southamerica-east1` (ex.: Crashlytics/Analytics/AdMob podem processar dados fora do Brasil) e incluir cláusula de transferência internacional conforme LGPD art. 33.*

---

## 9. Idade mínima

O FypMatch é destinado exclusivamente a pessoas com 18 anos ou mais. Não coletamos intencionalmente dados de menores de idade. Se identificarmos uma conta de menor de idade, ela será removida.

---

## 10. Alterações nesta política

Podemos atualizar esta política periodicamente. Mudanças relevantes serão comunicadas no app antes de entrarem em vigor.

---

## Checklist de conformidade

| Item | Status | Observação |
|---|---|---|
| LGPD — base legal por finalidade | ⚠️ Rascunho | Revisar tabela da seção 3 com jurídico |
| LGPD — consentimento destacado p/ dados sensíveis | ⚠️ Pendente | Confirmar texto real de onboarding no app |
| LGPD — DPO nomeado | ⚠️ Pendente | Preencher nome/e-mail |
| LGPD — DPA com Firebase/Google e provedor de IA | ⚠️ Pendente | Verificar se existe contrato assinado |
| Google Play — Data Safety form alinhado à política | ⚠️ Pendente | Preencher no Play Console usando esta política como base |
| Idade mínima declarada (18+) | ✅ Coberto | Confirmado no código (`UserProfile.age >= 18`) |
| Exclusão de conta | ✅ Parcialmente coberto | Fluxo existe; confirmar prazo/backups |

## Cláusulas que exigem revisão jurídica antes de publicar

| Cláusula | Por quê | Prioridade |
|---|---|---|
| Base legal para dados sensíveis (orientação sexual, religião, biometria) | LGPD exige consentimento específico e destacado — texto de onboarding precisa ser auditado | Alta |
| DPO e canal de contato | Obrigatório por lei (art. 41), hoje é placeholder | Alta |
| Transferência internacional | Depende de quais serviços Google processam dados fora do Brasil | Média |
| Cláusula de pagamento/assinatura | Billing real ainda não implementado — política precisa refletir o estado real no lançamento | Alta (antes de ativar billing) |
| Retenção de mensagens após exclusão de conta | Não confirmado tecnicamente | Média |

## Checklist de implementação (fora do texto da política)

- [ ] Publicar esta política em uma URL pública estável (ex.: `fypmatch.com.br/privacidade`) — obrigatório para o Play Console
- [ ] Vincular a URL no formulário "Data Safety" do Google Play Console
- [ ] Adicionar link para a política na tela de onboarding/cadastro do app
- [ ] Adicionar link para a política nas configurações do app
- [ ] Revisão por advogado especializado em LGPD antes da publicação
- [ ] Confirmar processo formal de atendimento a solicitações de titulares de dados (art. 18)
