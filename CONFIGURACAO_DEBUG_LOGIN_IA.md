# 🔧 Guia de Debug: Login Google + Firebase AI Logic

## 🚨 Problemas Identificados:
1. **Permissões de internet** estavam faltando (✅ CORRIGIDO)
2. **APIs do Firebase** podem não estar ativadas
3. **Gemini Developer API** precisa ser configurada

---

## 🌐 PARTE 1: Ativar APIs no Firebase Console

### 1️⃣ Acesse o Firebase Console:
```
https://console.firebase.google.com/project/fypmatch-8ac3c
```

### 2️⃣ Ative as APIs necessárias:

#### **🔐 Para Login Google:**
1. Vá em **Authentication** → **Sign-in method**
2. Ative o provedor **Google**
3. Adicione o domínio do seu app (se necessário)

#### **🤖 Para Firebase AI Logic:**
1. No menu lateral, clique em **AI Logic**
2. Clique em **Get started**
3. Escolha **Gemini Developer API** (gratuito para começar)
4. Siga o setup guiado
5. **IMPORTANTE**: Anote a **API Key** gerada

---

## 🔑 PARTE 2: Configurar Gemini Developer API

### 1️⃣ Acesse o Google AI Studio:
```
https://aistudio.google.com/
```

### 2️⃣ Gere uma API Key:
1. Clique em **Get API Key**
2. Selecione o projeto **fypmatch-8ac3c**
3. Copie a **API Key** gerada

### 3️⃣ Configure no Firebase:
1. Volte para o Firebase Console
2. Vá em **AI Logic** → **Settings**
3. Cole a **API Key** do Gemini Developer

---

## 📱 PARTE 3: Configurações do Android

### ✅ Permissões (JÁ CORRIGIDAS):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 🔍 Verificar SHA-1 no Firebase:
1. No Firebase Console → **Project Settings**
2. Na aba **General**, na seção **Your apps**
3. Clique no app Android
4. Verifique se o **SHA-1** está correto:
   ```
   Debug: b9ac491d00f730eb8550617ced7acea152f9b148
   Release: d8d6fb8d3dffdfe5f214037e2bce6ebf4d60e6d3
   ```

---

## 🐛 PARTE 4: Debug e Teste

### 1️⃣ Testar Build:
```bash
./gradlew assembleDebug
```

### 2️⃣ Instalar APK:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3️⃣ Ver Logs em Tempo Real:
```bash
adb logcat | findstr -i "fypmatch"
```

---

## ⚡ SOLUÇÃO RÁPIDA:

Se ainda não funcionar, execute estes passos **EXATAMENTE**:

1. **Firebase Console** → **AI Logic** → **Enable Gemini Developer API**
2. **Google AI Studio** → **Generate API Key** → Copie
3. **Firebase Console** → **AI Logic** → **Settings** → Cole a API Key
4. **Compile novamente**: `./gradlew clean assembleDebug`
5. **Teste login** e **chat IA**

---

## 📞 Status das Configurações:

| Componente | Status | Ação |
|------------|--------|------|
| ✅ Permissões Internet | Corrigido | - |
| ⚠️ APIs Firebase | Verificar | Ativar no console |
| ⚠️ Gemini API Key | Configurar | Google AI Studio |
| ✅ google-services.json | OK | - |
| ✅ SHA-1 Certificates | OK | - |

---

## 🎯 Depois de seguir todos os passos:

1. Compile: `./gradlew assembleDebug`
2. Instale o APK
3. Teste login Google
4. Teste chat com IA
5. Reporte o resultado! 