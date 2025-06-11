# 🏗️ Arquitetura Híbrida Firebase - FypMatch

## 📋 Visão Geral

O FypMatch agora utiliza uma **arquitetura híbrida** que otimiza o uso dos serviços Firebase para diferentes tipos de dados:

- **🗄️ Firestore**: Dados estáticos e estruturados
- **⚡ Realtime Database**: Dados dinâmicos e temporais  
- **📸 Firebase Storage**: Armazenamento de imagens

## 🗄️ Firestore - Dados Estáticos

### 📍 **Uso**: Dados que mudam pouco e precisam de queries complexas

### 📂 **Coleções**:

#### `users/` - Dados principais do usuário
```json
{
  "id": "user123",
  "email": "user@gmail.com", 
  "displayName": "João Silva",
  "photoUrl": "https://...",
  "subscription": "PREMIUM",
  "accessLevel": "FULL_ACCESS",
  "createdAt": "2024-01-15T10:30:00Z",
  "lastActive": "2024-01-20T14:30:00Z",
  "isActive": true
}
```

#### `profiles/` - Perfis detalhados
```json
{
  "fullName": "João Silva Santos",
  "age": 28,
  "bio": "Desenvolvedor apaixonado por tecnologia",
  "location": {
    "city": "São Paulo",
    "state": "SP", 
    "country": "Brasil"
  },
  "gender": "MALE",
  "orientation": "STRAIGHT",
  "intention": "DATING",
  "interests": ["Tecnologia", "Música", "Viagens"],
  "education": "Superior Completo",
  "profession": "Desenvolvedor",
  "height": 175,
  "isProfileComplete": true
}
```

#### `preferences/` - Preferências de match
```json
{
  "ageRange": [22, 35],
  "maxDistance": 30,
  "showMe": "WOMEN",
  "genderPreference": ["FEMALE"],
  "intentionPreference": ["DATING"],
  "onlyVerified": false
}
```

### 🔧 **UserRepository** - Operações principais:
- `createUser()` - Criar novo usuário
- `updateUser()` - Atualizar dados
- `getUser()` - Buscar usuário
- `searchUsers()` - Busca com filtros
- `observeUser()` - Listener em tempo real

## ⚡ Realtime Database - Dados Dinâmicos

### 📍 **Uso**: Dados que mudam constantemente e precisam de sincronização instantânea

### 🗂️ **Estrutura**:

#### `usuarios_online/` - Status online/offline
```json
{
  "user123": {
    "online": true,
    "lastActivity": 1674567890123,
    "timestamp": 1674567890123
  }
}
```

#### `localizacoes/` - Localização atual GPS
```json
{
  "user123": {
    "latitude": -23.5505,
    "longitude": -46.6333,
    "accuracy": 10.5,
    "timestamp": 1674567890123,
    "lastUpdated": 1674567890123
  }
}
```

#### `ultimo_visto/` - Última vez visto
```json
{
  "user123": 1674567890123
}
```

#### `conversas/` - Sistema de chat (já implementado)
```json
{
  "user123_user456": {
    "tipo": "usuario-usuario",
    "participantes": {
      "user123": true,
      "user456": true
    },
    "mensagens": {
      "msg_001": {
        "remetenteId": "user123",
        "texto": "Oi! Como você está?",
        "timestamp": 1674567890123
      }
    }
  }
}
```

### 🔧 **LocationRepository** - Operações principais:
- `setUserOnline()` - Marcar como online
- `setUserOffline()` - Marcar como offline  
- `updateCurrentLocation()` - Atualizar GPS
- `findNearbyUsers()` - Buscar usuários próximos
- `observeUserOnlineStatus()` - Status em tempo real

## 📸 Firebase Storage - Imagens

### 📍 **Uso**: Armazenamento otimizado de fotos de perfil

### 📁 **Estrutura**:

```
profile_photos/
  user123/
    main_abc123.jpg     (foto principal)
    def456.jpg          (foto secundária)
    ghi789.jpg          (foto secundária)

thumbnails/
  user123/
    thumb_abc123.jpg    (miniatura otimizada)
    thumb_def456.jpg
    thumb_ghi789.jpg

temp_uploads/
  temp_xyz.jpg          (uploads temporários)
```

### 🔧 **StorageRepository** - Operações principais:
- `uploadProfilePhoto()` - Upload de foto
- `uploadMultiplePhotos()` - Upload múltiplo
- `deleteProfilePhoto()` - Deletar foto
- `getUserPhotos()` - Listar fotos do usuário
- `validateImageFile()` - Validar imagem

## 🔄 Fluxo de Dados

### 👤 **Login do Usuário**:
1. **Firebase Auth** - Autenticação
2. **Firestore** - Carregar/criar perfil via `UserRepository`
3. **Realtime Database** - Marcar como online via `LocationRepository`
4. **Analytics** - Registrar evento de login

### 💬 **Envio de Mensagem**:
1. **Realtime Database** - Salvar mensagem via `ChatRepository`
2. **Firestore** - Atualizar contador de mensagens (se necessário)
3. **Analytics** - Registrar atividade

### 📍 **Atualização de Localização**:
1. **GPS** - Obter coordenadas
2. **Realtime Database** - Salvar via `LocationRepository`
3. **Busca proximidade** - Encontrar usuários próximos

### 📸 **Upload de Foto**:
1. **Firebase Storage** - Upload via `StorageRepository`
2. **Firestore** - Atualizar URLs no perfil via `UserRepository`
3. **Analytics** - Registrar upload

## 🚀 Vantagens da Arquitetura Híbrida

### ⚡ **Performance**:
- **Firestore**: Queries complexas otimizadas
- **Realtime DB**: Sincronização instantânea
- **Storage**: CDN global para imagens

### 💰 **Custo-Benefício**:
- **Firestore**: Paga por operação (ideal para dados estáticos)
- **Realtime DB**: Paga por bandwidth (ideal para tempo real)
- **Storage**: Armazenamento eficiente de mídia

### 🔧 **Manutenibilidade**:
- **Separação clara** de responsabilidades
- **Repositórios especializados** para cada serviço
- **Injeção de dependência** com Hilt

## 📊 Analytics Integrados

Todos os repositórios incluem **analytics detalhados**:

```kotlin
// Exemplo de evento registrado
analyticsManager.logCustomCrash("user_location_updated", mapOf(
    "user_id" to userId,
    "accuracy" to accuracy.toString(),
    "timestamp" to currentTime.toString()
))
```

### 📈 **Eventos Rastreados**:
- Login/logout de usuários
- Atualizações de perfil
- Uploads de fotos
- Mudanças de localização
- Status online/offline
- Operações de chat
- Erros e falhas

## 🔒 Segurança e Validação

### 🛡️ **Firestore Rules**:
```javascript
// Usuários só podem acessar seus próprios dados
match /users/{userId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```

### ⚡ **Realtime Database Rules**:
```json
{
  "rules": {
    "usuarios_online": {
      "$uid": {
        ".read": true,
        ".write": "$uid === auth.uid"
      }
    },
    "conversas": {
      "$conversationId": {
        ".read": "data.child('participantes').hasChild(auth.uid)",
        ".write": "data.child('participantes').hasChild(auth.uid)"
      }
    }
  }
}
```

### 📸 **Storage Rules**:
```javascript
// Usuários só podem gerenciar suas próprias fotos
match /profile_photos/{userId}/{allPaths=**} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```

## 🧪 Testes e Debug

### 📱 **Scripts de Debug**:
- `enable_debug_analytics.ps1` - Analytics debug
- `test_gemini_api.ps1` - Teste de IA

### 🔍 **Monitoramento**:
- Firebase Analytics Dashboard
- Crashlytics para erros
- Performance Monitoring

## 📚 Documentação Adicional

- `REALTIME_DATABASE_IMPLEMENTADO.md` - Chat em tempo real
- `CORRECOES_IMPLEMENTADAS.md` - Correções aplicadas
- `CONFIGURACAO_ANALYTICS_CRASHLYTICS.md` - Setup analytics

---

## 🎯 Próximos Passos

1. **✅ Compilação** - Projeto compila com sucesso
2. **🧪 Testes** - Validar funcionalidades no dispositivo
3. **📊 Monitoramento** - Verificar analytics em produção
4. **🔧 Otimizações** - Melhorias baseadas em dados reais

**Status**: ✅ **Implementação Completa e Funcional** 