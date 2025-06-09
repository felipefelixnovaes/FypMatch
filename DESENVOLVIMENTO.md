# 🚀 FypMatch - Guia de Desenvolvimento

## 💻 **CONFIGURAÇÃO DO AMBIENTE**

### **Pré-requisitos**
- **Android Studio**: Hedgehog ou superior
- **JDK**: 11 ou superior
- **Android SDK**: API 24-35
- **Git**: Para controle de versão
- **Firebase CLI**: Para configuração do backend

### **Setup Inicial**
```bash
# 1. Clone o repositório
git clone [repository-url]
cd FypMatch

# 2. Abra no Android Studio
# File -> Open -> Selecione a pasta FypMatch

# 3. Aguarde o Gradle Sync
# O Android Studio fará o sync automático das dependências

# 4. Configure o Firebase
# Adicione o google-services.json ao diretório app/

# 5. Execute o projeto
# Run -> Run 'app' ou Shift+F10
```

---

## 🏗️ **ESTRUTURA DO PROJETO**

### **Organização de Pastas**
```
app/src/main/java/com/example/FypMatch/
├── data/repository/          # Camada de dados
├── model/                    # Modelos de domínio
├── ui/
│   ├── screens/             # Telas Compose
│   ├── navigation/          # Sistema de navegação
│   ├── theme/               # Tema Material Design 3
│   └── viewmodel/           # ViewModels
└── util/                    # Utilitários e extensões
```

### **Convenções de Nomenclatura**
- **Arquivos**: PascalCase (ex: `WelcomeScreen.kt`)
- **Classes**: PascalCase (ex: `class WaitlistViewModel`)
- **Funções**: camelCase (ex: `fun validateEmail()`)
- **Variáveis**: camelCase (ex: `val currentUser`)
- **Constantes**: UPPER_SNAKE_CASE (ex: `const val MAX_AGE`)

---

## 🔧 **COMANDOS ÚTEIS**

### **Build e Instalação**
```bash
# Limpar o projeto
./gradlew clean

# Compilar debug
./gradlew assembleDebug

# Instalar no dispositivo/emulador
./gradlew installDebug

# Build completo
./gradlew clean assembleDebug installDebug

# Gerar APK de release
./gradlew assembleRelease
```

### **Debugging**
```bash
# Ver logs em tempo real
adb logcat | grep "FypMatch"

# Instalar APK manualmente
adb install app/build/outputs/apk/debug/app-debug.apk

# Limpar dados do app
adb shell pm clear com.ideiassertiva.FypMatch
```

---

## 🎯 **DESENVOLVIMENTO DE FUNCIONALIDADES**

### **Adicionando Nova Tela**

1. **Criar a Screen**
```kotlin
// ui/screens/NovaScreen.kt
@Composable
fun NovaScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Nova Funcionalidade")
    }
}
```

2. **Criar o ViewModel**
```kotlin
// ui/viewmodel/NovaViewModel.kt
class NovaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NovaUiState())
    val uiState: StateFlow<NovaUiState> = _uiState.asStateFlow()
}

data class NovaUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
```

3. **Adicionar à Navegação**
```kotlin
// ui/navigation/AppNavigation.kt
object Nova : Screen("nova")

// No NavHost
composable(Screen.Nova.route) {
    NovaScreen(navController)
}
```

### **Criando Novo Repository**

```kotlin
// data/repository/NovoRepository.kt
class NovoRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun buscarDados(): Result<List<Modelo>> {
        return try {
            val snapshot = firestore.collection("colecao").get().await()
            val dados = snapshot.documents.mapNotNull { /* mapeamento */ }
            Result.success(dados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 🎨 **DESIGN SYSTEM**

### **Usando Cores do Tema**
```kotlin
@Composable
fun ComponentePersonalizado() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = "Texto",
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
```

### **Aplicando Gradientes**
```kotlin
Box(
    modifier = Modifier
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE91E63), // Pink
                    Color(0xFF9C27B0)  // Purple
                )
            )
        )
)
```

---

## 🔌 **INTEGRAÇÃO FIREBASE**

### **Configuração Inicial**
1. Criar projeto no Firebase Console
2. Adicionar app Android
3. Baixar `google-services.json`
4. Colocar em `app/google-services.json`

### **Firestore Operations**
```kotlin
// Salvar dados
suspend fun salvarUsuario(user: User) {
    firestore.collection("users")
        .document(user.id)
        .set(user)
        .await()
}

// Buscar dados
suspend fun buscarUsuario(id: String): User? {
    return firestore.collection("users")
        .document(id)
        .get()
        .await()
        .toObject<User>()
}
```

---

## 🚀 **DEPLOYMENT**

### **Build de Release**
```bash
# Gerar APK assinado
./gradlew assembleRelease

# Gerar AAB para Play Store
./gradlew bundleRelease
```

### **Checklist de Release**
- [ ] Versão atualizada no build.gradle
- [ ] Changelog atualizado
- [ ] Testes de regressão executados
- [ ] APK testado em diferentes dispositivos
- [ ] Configurações de produção verificadas

---

## 📋 **CONVENÇÕES DE CÓDIGO**

### **Kotlin Style Guide**
- Use `val` em vez de `var` sempre que possível
- Prefira funções de extensão para utilitários
- Use `sealed class` para estados finitos
- Implemente `data class` para modelos simples

### **Compose Best Practices**
- Separe stateful de stateless composables
- Use `remember` para estados locais
- Passe callbacks para ações do usuário
- Otimize recomposições com `derivedStateOf`

---

## 🔄 **WORKFLOW DE CONTRIBUIÇÃO**

### **Git Flow**
```bash
# 1. Criar feature branch
git checkout -b feature/nova-funcionalidade

# 2. Desenvolver e commitar
git add .
git commit -m "feat: adiciona nova funcionalidade"

# 3. Push da branch
git push origin feature/nova-funcionalidade

# 4. Criar Pull Request
# Via GitHub/GitLab interface

# 5. Merge após review
git checkout main
git pull origin main
```

### **Convenção de Commits**
- `feat:` Nova funcionalidade
- `fix:` Correção de bug
- `docs:` Documentação
- `style:` Formatação
- `refactor:` Refatoração
- `test:` Testes
- `chore:` Tarefas de manutenção

---

**🚀 Ambiente de desenvolvimento completo e organizado para o FypMatch!** 