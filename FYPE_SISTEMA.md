# 💕 Fype - Sistema de Conselheira de Relacionamentos com IA

## 🎯 **Visão Geral**

A **Fype** é a conselheira pessoal de relacionamentos do FypMatch, uma IA especializada que ajuda usuários com conselhos sobre amor, encontros e relacionamentos. O sistema combina anúncios recompensados com inteligência artificial avançada para criar uma experiência monetizada e valiosa.

---

## 🚀 **Funcionalidades Implementadas**

### **💬 Interface de Chat**
- **Design gradient**: Rosa (#E91E63) → Roxo (#9C27B0) → Violeta (#673AB7)
- **Bolhas de mensagem**: Diferenciação visual entre usuário e Fype
- **Avatar personalizado**: Ícone Face para representar a Fype
- **Indicador de digitação**: "Fype está digitando..." com animação
- **Timestamps**: Horário de cada mensagem
- **Mensagem de boas-vindas**: Apresentação automática da conselheira

### **💰 Sistema de Créditos**
- **Custo por mensagem**: 1 crédito = 1 mensagem enviada
- **Recompensas por anúncio**: 3 créditos por vídeo assistido
- **Contador em tempo real**: Exibição no header da tela
- **Estados condicionais**: Tela muda conforme disponibilidade de créditos

### **📺 Anúncios Recompensados**
- **Integração AdMob**: Google Mobile Ads SDK
- **ID do App**: `ca-app-pub-9657321458227740~9100657445`
- **ID Rewarded**: `ca-app-pub-9657321458227740/9078839667`
- **Estados de carregamento**: Feedback visual durante operações
- **Dialog de recompensa**: Celebração ao ganhar créditos

### **🧠 Firebase AI Logic**
- **Modelo**: Gemini 2.0 Flash (mais avançado do Google)
- **Contexto especializado**: Prompts focados em relacionamentos
- **Personalidade da Fype**: Carinhosa, empática e experiente
- **Fallback inteligente**: Respostas de emergência em caso de erro
- **Detecção contextual**: Análise de mensagens para respostas adequadas

---

## 📱 **Arquitetura Técnica**

### **Arquivos Principais**
```
ui/screens/FypeScreen.kt           - Interface principal da Fype
ui/viewmodel/CreditsViewModel.kt   - Gerenciamento de créditos
ui/viewmodel/ChatViewModel.kt      - Estados do chat (expandido)
data/repository/RewardedAdsRepository.kt - Anúncios AdMob
data/repository/GeminiRepository.kt      - Firebase AI Logic
```

### **Fluxo de Dados**
1. **Usuário acessa Fype** → Verificação de créditos
2. **Se sem créditos** → Tela de anúncios recompensados
3. **Usuário assiste vídeo** → Recebe 3 créditos + dialog
4. **Com créditos** → Área de chat habilitada
5. **Usuário envia mensagem** → Desconta 1 crédito
6. **IA processa** → Resposta contextual da Fype

### **Estados da Interface**
- **NeedCreditsScreen**: Quando usuário não tem créditos
- **ChatScreen**: Quando usuário pode conversar
- **LoadingState**: Durante operações (anúncios, IA)
- **RewardDialog**: Celebração ao ganhar créditos
- **TypingIndicator**: Enquanto Fype "digita"

---

## 🎯 **Especialidades da Fype**

### **Áreas de Aconselhamento**
1. **Relacionamentos Gerais**
   - Dicas para melhorar conexões
   - Resolução de conflitos
   - Comunicação afetiva

2. **Ansiedade em Encontros**
   - Técnicas de relaxamento
   - Preparação mental
   - Estratégias de confiança

3. **Primeiros Encontros**
   - Preparação prática
   - Tópicos de conversa
   - Etiqueta de encontros

4. **Autoestima**
   - Exercícios de confiança
   - Autoconhecimento
   - Amor próprio

5. **Comunicação Digital**
   - Apps de relacionamento
   - Mensagens eficazes
   - Interpretação de sinais

---

## 💰 **Modelo de Monetização**

### **Sistema de Créditos por Tipo de Usuário**
| Tipo | Créditos Iniciais | Reposição | Anúncios |
|------|------------------|-----------|----------|
| **FREE** | 0 | Manual (anúncios) | 3 créditos/vídeo |
| **PREMIUM** | 10/dia | Automática (00h) | Opcional |
| **VIP** | 25/dia | Automática (00h) | Desnecessário |

### **Configuração AdMob**
- **Formato**: Anúncios recompensados de vídeo
- **Recompensa**: 3 créditos por visualização completa
- **Limite**: Configurável por usuário/dia
- **Plataformas**: Android (iOS preparado)

---

## 🔧 **Configuração e Setup**

### **Firebase Console**
1. Ativar Firebase AI Logic
2. Configurar Gemini Developer API
3. Adicionar chave de API (não no código)

### **AdMob Console**
1. Configurar app com ID: `ca-app-pub-9657321458227740~9100657445`
2. Criar anúncio recompensado: `ca-app-pub-9657321458227740/9078839667`
3. Ativar test ads para desenvolvimento

### **Dependencies Gradle**
```kotlin
implementation("com.google.firebase:firebase-bom:33.14.0")
implementation("com.google.firebase:firebase-ai")
implementation("com.google.android.gms:play-services-ads:23.6.0")
```

---

## 📊 **Métricas e KPIs**

### **Objetivos de Produto**
- **Engajamento**: 60% dos usuários testam a Fype
- **Retenção**: 40% retornam para segunda conversa
- **Monetização**: 25% assistem anúncios regularmente
- **Satisfação**: 4.5+ estrelas em feedback

### **Métricas Técnicas**
- **Taxa de sucesso IA**: >95% de respostas válidas
- **Tempo de resposta**: <3 segundos médio
- **Carregamento anúncios**: >90% taxa de sucesso
- **Erro rate**: <2% em operações críticas

---

## 🛣️ **Próximos Passos**

### **Melhorias Planejadas**
- [ ] **Persistência de créditos** no Firebase
- [ ] **Histórico de conversas** com a Fype  
- [ ] **Análise de sentimentos** nas mensagens
- [ ] **Recomendações personalizadas** baseadas no histórico
- [ ] **Notificações push** para dicas diárias
- [ ] **Sistema de badges** por conquistas com a Fype

### **Otimizações Técnicas**
- [ ] **Cache local** de respostas frequentes
- [ ] **Compression** de mensagens para economia de dados
- [ ] **Analytics** detalhados de uso
- [ ] **A/B Testing** para diferentes prompts
- [ ] **Rate limiting** inteligente

---

## 🎨 **Design System**

### **Cores da Fype**
- **Primary**: `#E91E63` (Rosa)
- **Secondary**: `#9C27B0` (Roxo)  
- **Accent**: `#673AB7` (Violeta)
- **Background**: Gradient vertical
- **Text**: Branco sobre gradient, preto sobre cards

### **Tipografia**
- **Header**: Bold, 20sp
- **Body**: Regular, 16sp
- **Timestamp**: Light, 10sp
- **Credits**: Bold, 16sp

### **Componentes**
- **MessageBubble**: Cantos arredondados assimétricos
- **CreditsCounter**: Card translúcido no header
- **WelcomeMessage**: Card destacado com avatar
- **TypingIndicator**: Animação de pontinhos

---

## ✅ **Status de Implementação**

- ✅ **Interface completa**: FypeScreen.kt
- ✅ **Sistema de créditos**: CreditsViewModel.kt  
- ✅ **Anúncios recompensados**: RewardedAdsRepository.kt
- ✅ **Chat com IA**: ChatViewModel.kt expandido
- ✅ **Firebase AI Logic**: GeminiRepository.kt atualizado
- ✅ **Navegação**: Integrada no DiscoveryScreen
- ✅ **Injeção de dependência**: AppModule.kt
- ✅ **APK compilado**: app-debug.apk (24.2MB)

**A Fype está pronta para ajudar os usuários do FypMatch! 💕** 