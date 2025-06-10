# 🔑 **Configuração Google Sign-In - FypMatch**

## 📋 **Fingerprints para Adicionar no Firebase Console**

### **🐛 Debug (já configurado):**
- **SHA-1**: `D8:D6:FB:8D:3D:FF:DF:E5:F2:14:03:7E:2B:CE:6E:BF:4D:60:E6:D3`

### **🚀 Release (NOVO - precisa adicionar):**
- **SHA-1**: `B9:AC:49:1D:00:F7:30:EB:85:50:61:7C:ED:7A:CE:A1:52:F9:B1:48`
- **SHA-256**: `F5:9B:C8:33:3F:20:A0:5D:59:18:02:2A:9F:AA:DC:E1:30:B6:37:C7:D5:2C:8B:BE:47:FF:04:35:2F:94:BB:C1`

---

## 🔧 **PASSO A PASSO - Firebase Console**

### **⚠️ CORREÇÃO URGENTE - Package Name**
**PROBLEMA**: Firebase está com `com.ideiassertiva.fypmatch` mas o código usa `com.ideiassertiva.FypMatch`

**SOLUÇÃO**:
1. Delete o app Android atual no Firebase Console
2. Adicione novo app Android com package name: `com.ideiassertiva.FypMatch`
3. Re-adicione todos os fingerprints abaixo

### **1️⃣ Adicionar Fingerprints (após correção do package)**
1. Acesse [console.firebase.google.com](https://console.firebase.google.com)
2. Selecione o projeto **FypMatch**
3. Vá em **⚙️ Configurações do Projeto** > **Geral**
4. Role até **"Seus aplicativos"**
5. Clique no app **Android (com.ideiassertiva.FypMatch)**
6. Clique **"Adicionar fingerprint"**
7. Cole o **SHA-1 de release**: `B9:AC:49:1D:00:F7:30:EB:85:50:61:7C:ED:7A:CE:A1:52:F9:B1:48`
8. Clique **"Adicionar fingerprint"** novamente
9. Cole o **SHA-256 de release**: `F5:9B:C8:33:3F:20:A0:5D:59:18:02:2A:9F:AA:DC:E1:30:B6:37:C7:D5:2C:8B:BE:47:FF:04:35:2F:94:BB:C1`

### **2️⃣ Habilitar Google Authentication**
1. No Firebase Console, vá em **🔐 Authentication**
2. Clique **"Começar"** se ainda não estiver habilitado
3. Vá em **"Sign-in method"**
4. Clique em **"Google"**
5. **Habilite** o Google Sign-In
6. **Configure o email de suporte** (seu email)
7. Clique **"Salvar"**

### **3️⃣ Obter Web Client ID**
1. Vá em **⚙️ Configurações do Projeto** > **Geral**
2. Role até **"Configuração do SDK"**
3. Copie o **Web client ID** (não o Android client ID)
4. Formato: `XXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com`

### **4️⃣ Baixar novo google-services.json**
1. Clique **"Baixar google-services.json"**
2. Substitua o arquivo `app/google-services.json` atual

---

## 🔧 **CONFIGURAÇÕES FINAIS NO CÓDIGO**

### **Após obter o Web Client ID, atualizar:**
📂 `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/AuthRepository.kt`

**Linha 47**: Substituir o placeholder pelo Web Client ID real:
```kotlin
.requestIdToken("SEU_WEB_CLIENT_ID_AQUI.apps.googleusercontent.com")
```

---

## ✅ **VERIFICAÇÃO**

### **Como saber se funcionou:**
1. ✅ `oauth_client` no `google-services.json` não estará mais vazio
2. ✅ Google Sign-In funcionará tanto no debug quanto no release
3. ✅ Autenticação será bem-sucedida

### **Teste após configuração:**
```bash
# Debug APK
./gradlew app:assembleDebug

# Release APK  
./gradlew app:assembleRelease
```

---

## 🚨 **IMPORTANTE**

- ⚠️ **NÃO** commit o Web Client ID real no código
- 🔒 Use environment variables ou strings resources para produção
- 🧪 Teste AMBOS os builds (debug e release) após configuração

---

## 📱 **Informações do Projeto**

- **Project ID**: `fypmatch-bd2cc`
- **Package Name**: `com.ideiassertiva.FypMatch` 
- **App ID**: `1:15026077760:android:11c58623a6cb8f9a665fa5` 