# 🔥 Configuração Completa do Firebase

## ✅ **Status**: Firebase CLI instalado com sucesso!

---

## 📋 **Próximos Passos para Deploy**

### 1. **Login no Firebase** (Necessário)
```bash
# Fazer login no Firebase (abrirá o browser)
firebase login

# Se der problema, use:
firebase login --no-localhost
```

### 2. **Inicializar Projeto**
```bash
# Já estamos na pasta website
firebase init hosting
```

**Respostas recomendadas:**
- **Use existing project**: Escolha seu projeto Firebase
- **Public directory**: `.` (ponto - pasta atual)
- **Single-page app**: `No`
- **Automatic builds**: `No`
- **Overwrite index.html**: `No`

### 3. **Deploy para Produção**
```bash
firebase deploy --only hosting
```

---

## 🔧 **Configuração do Firebase SDK**

### ✅ **SDK Já Adicionado**
O Firebase SDK foi adicionado ao `index.html` com os seguintes serviços:
- **Analytics**: Métricas de uso
- **Performance**: Monitoramento de performance
- **Pronto para**: Auth, Firestore, Storage

### 🔑 **Configurar suas Chaves**
No arquivo `index.html`, substitua as configurações:

```javascript
const firebaseConfig = {
    apiKey: "SUA_API_KEY_AQUI",
    authDomain: "seu-projeto.firebaseapp.com", 
    projectId: "seu-projeto-id",
    storageBucket: "seu-projeto.appspot.com",
    messagingSenderId: "123456789",
    appId: "1:123456789:web:abcdef",
    measurementId: "G-XXXXXXXXXX"
};
```

### 📍 **Onde Encontrar as Chaves**
1. Firebase Console → Seu projeto
2. Configurações do projeto (ícone engrenagem)
3. Geral → Seus apps → Configuração do SDK

---

## 🌐 **Configuração do Domínio**

### 1. **Deploy Inicial**
```bash
firebase deploy --only hosting
```
Você receberá um URL temporário: `https://seu-projeto.web.app`

### 2. **Adicionar Domínio Personalizado**
1. Firebase Console → Hosting
2. "Add custom domain"
3. Digite: `www.fypmatch.com`
4. Siga as instruções de DNS
5. Aguarde verificação SSL (até 24h)

### 3. **Configuração DNS**
Adicione os registros DNS conforme instruções do Firebase:
- **Tipo**: A ou CNAME
- **Nome**: www
- **Valor**: Conforme Firebase instruir

---

## 📱 **Integração com Google Play Store**

### ✅ **app-ads.txt Configurado**
- ✅ Arquivo: `/app-ads.txt`
- ✅ Publisher ID: `pub-9657321458227740`
- ✅ URL final: `https://www.fypmatch.com/app-ads.txt`

### 📋 **Checklist Verificação**
Após o deploy, teste:
```bash
# Testar app-ads.txt
curl https://www.fypmatch.com/app-ads.txt

# Testar página principal
curl https://www.fypmatch.com/

# Testar políticas
curl https://www.fypmatch.com/privacy
curl https://www.fypmatch.com/terms
```

---

## 🛠️ **Comandos Úteis**

### **Deploy e Hosting**
```bash
# Login
firebase login

# Inicializar (primeira vez)
firebase init hosting

# Deploy
firebase deploy --only hosting

# Testar local
firebase serve --only hosting

# Ver projetos
firebase projects:list

# Mudar projeto
firebase use nome-do-projeto
```

### **Logs e Debug**
```bash
# Ver logs de hosting
firebase hosting:channel:list

# Debug mode
firebase deploy --debug

# Ver configuração atual
firebase projects:list
cat firebase.json
```

---

## 📊 **Serviços Firebase Disponíveis**

### ✅ **Já Configurados**
- **Hosting**: Para o website
- **Analytics**: Métricas de uso
- **Performance**: Monitoramento

### 🔄 **Para Futuro** (Opcional)
- **Authentication**: Login de usuários
- **Firestore**: Banco de dados
- **Storage**: Upload de arquivos
- **Functions**: Serverless functions
- **Messaging**: Push notifications

---

## 🚨 **Troubleshooting**

### **Problema: firebase command not found**
```bash
# Reinstalar CLI
npm install -g firebase-tools

# Verificar PATH
echo $PATH
```

### **Problema: Permissões**
```bash
# No Windows como administrador
npm install -g firebase-tools

# No Mac/Linux
sudo npm install -g firebase-tools
```

### **Problema: Login falha**
```bash
# Tentar modo sem localhost
firebase login --no-localhost

# Ou reinteractive
firebase login --reauth
```

### **Problema: Deploy falha**
```bash
# Verificar projeto
firebase projects:list
firebase use nome-correto-projeto

# Deploy com debug
firebase deploy --debug --only hosting
```

---

## 📞 **Próximos Passos**

1. **Fazer Login**:
   ```bash
   firebase login
   ```

2. **Configurar Projeto**:
   ```bash
   firebase init hosting
   ```

3. **Obter Chaves Firebase**:
   - Firebase Console → Projeto → Configurações
   - Copiar configuração para `index.html`

4. **Deploy**:
   ```bash
   firebase deploy --only hosting
   ```

5. **Configurar Domínio**:
   - Adicionar `www.fypmatch.com` no console
   - Configurar DNS

6. **Verificar Google Play**:
   - Adicionar URL no Play Console
   - Aguardar verificação AdMob

---

## 🎯 **Status Final**

- ✅ **Firebase CLI**: Instalado v14.6.0
- ✅ **Website**: Pronto para deploy
- ✅ **SDK**: Configurado no HTML
- ✅ **app-ads.txt**: Configurado
- ⏳ **Deploy**: Pendente login
- ⏳ **Domínio**: Pendente configuração

**Próximo passo**: `firebase login` e depois `firebase init hosting`

---

*Guia criado para FypMatch - Janeiro 2025* 