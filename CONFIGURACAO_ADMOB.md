# 🎯 **Configuração AdMob - FypMatch**

## 📱 **Estratégia Implementada**

### **🎯 Anúncios Intersticiais pós-Swipe**
- **Frequência**: A cada 3 swipes para usuários básicos
- **Tempo**: Mínimo 30 segundos entre anúncios  
- **Público**: Somente usuários **FREE** (não premium)
- **Posição**: Após dar swipe em perfis na discovery

---

## 🔧 **Google AdMob Console - Configuração**

### **1️⃣ Nomes Sugeridos para Blocos de Anúncios**

#### **🎯 Intersticial Principal (Recomendado)**
```
Nome: "Intersticial pós-Swipe - FypMatch Brasil"
Descrição: "Anúncio intersticial exibido para usuários básicos após dar swipe em perfis na tela principal de descoberta"
Formato: Intersticial
Plataforma: Android
```

#### **🎯 Banner Discovery (Futuro)**
```
Nome: "Banner Discovery - FypMatch Brasil" 
Descrição: "Banner exibido na tela de descoberta para usuários básicos entre swipes de perfis"
Formato: Banner 320x50
Plataforma: Android
```

#### **🎯 Recompensado Super Like (Futuro)**
```
Nome: "Recompensado Super Like - FypMatch Brasil"
Descrição: "Anúncio recompensado que oferece Super Likes extras para usuários básicos"
Formato: Vídeo Recompensado
Plataforma: Android
```

---

## 📋 **Passo a Passo - Google AdMob**

### **1️⃣ Criar Conta AdMob**
1. Acesse [admob.google.com](https://admob.google.com)
2. **Entre** com conta Google do projeto
3. **Vincule** à conta Google Ads (se houver)

### **2️⃣ Adicionar App Android**
1. **Clique** "Adicionar app"
2. **Selecione** "Android"
3. **Nome**: FypMatch
4. **Package Name**: `com.ideiassertiva.FypMatch`
5. **Clique** "Adicionar"

### **3️⃣ Criar Bloco de Anúncios**
1. **Vá** no app criado
2. **Clique** "Adicionar unidade de anúncio"
3. **Selecione** "Intersticial"
4. **Nome**: Use a sugestão acima
5. **Copie** o ID da unidade de anúncio

### **4️⃣ IDs Configurados ✅**
- **App ID**: `ca-app-pub-9657321458227740~9100657445`
- **Intersticial ID**: `ca-app-pub-9657321458227740/7105049493`

---

## 🛠️ **Configuração no Código**

### **1️⃣ AndroidManifest.xml - ✅ CONFIGURADO**
```xml
<!-- Dentro de <application> -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-9657321458227740~9100657445"/>
```

### **2️⃣ AdMobRepository.kt - ✅ CONFIGURADO**
```kotlin
// ID real do bloco intersticial FypMatch Brasil
private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9657321458227740/7105049493"
```

---

## 📊 **Configurações Recomendadas**

### **🎯 Controle de Frequência**
- ✅ **3 swipes** = 1 anúncio  
- ✅ **30 segundos** mínimo entre anúncios
- ✅ Somente usuários **FREE**
- ✅ Pré-carregamento inteligente

### **💰 Monetização Estratégica**
- **Discovery**: Anúncios após swipes
- **Matches**: Banner discreto (futuro)
- **Chat**: Sem anúncios (experiência premium)
- **Profile**: Banner opcional (futuro)

### **👥 Segmentação**
- **Usuários Básicos**: Veem anúncios normalmente
- **Usuários Premium**: Sem anúncios
- **Novos Usuários**: Primeiros 10 swipes sem anúncios

---

## 🚀 **Funcionalidades Implementadas**

### **✅ AdMobRepository**
- Inicialização automática do SDK
- Carregamento inteligente de anúncios
- Controle de frequência por usuário
- Callbacks completos de sucesso/erro
- Estatísticas em tempo real

### **✅ Integração Discovery**
- Contador de swipes por usuário
- Verificação de status premium
- Exibição automática após threshold
- Reset inteligente após anúncio

### **✅ UX Otimizada**
- Sem interrupção da experiência
- Timing respeitoso entre anúncios
- Pré-carregamento para fluidez
- Fallback em caso de erro

---

## 🔍 **Próximos Passos**

1. **Configure** AdMob Console seguindo este guia
2. **Copie** os IDs reais do AdMob
3. **Substitua** no `AdMobRepository.kt`
4. **Adicione** App ID no `AndroidManifest.xml`
5. **Teste** com anúncios reais
6. **Monitore** métricas no AdMob Console

---

## 📈 **Métricas para Acompanhar**

- **eCPM** (receita por mil impressões)
- **CTR** (taxa de cliques)
- **Fill Rate** (taxa de preenchimento)
- **Impressões diárias**
- **Receita por usuário**

**🎯 Meta**: Equilibrar monetização sem prejudicar experiência do usuário! 