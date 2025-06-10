# 🧪 Teste de Crashlytics - Documentação Oficial Firebase

## ✅ **Configuração Atualizada Conforme Documentação Oficial**

Baseado na [documentação oficial do Firebase Crashlytics](https://firebase.google.com/docs/crashlytics/get-started?platform=android&hl=pt-br#add-sdk), nossa configuração foi atualizada:

### **1. Plugin Atualizado para Versão Oficial**
```kotlin
// build.gradle.kts (projeto)
id("com.google.firebase.crashlytics") version "3.0.4" apply false // ✅ Atualizado
```

### **2. Dependências Configuradas**
```kotlin
// app/build.gradle.kts
plugins {
    id("com.google.firebase.crashlytics") // ✅ Adicionado
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics") // ✅ Adicionado
    implementation("com.google.firebase:firebase-analytics") // ✅ Configurado
}
```

---

## 🧪 **Etapa 3: Teste de Crash Conforme Documentação**

### **Código Oficial do Firebase**

A documentação oficial recomenda este código exato para testar:

```kotlin
val crashButton = Button(this)
crashButton.text = "Test Crash"
crashButton.setOnClickListener {
    throw RuntimeException("Test Crash") // Force a crash
}
addContentView(crashButton, ViewGroup.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
))
```

### **Implementação no FypMatch**

Criamos o `CrashlyticsTestHelper.kt` que implementa exatamente o código oficial:

```kotlin
// Uso no MainActivity (apenas DEBUG)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Adicionar botão de teste apenas em DEBUG
        CrashlyticsTestHelper.addTestCrashButton(this)
    }
}
```

---

## 📋 **Procedimento de Teste Oficial**

### **Passo 1: Compilar e Executar**
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### **Passo 2: Forçar Crash de Teste**
1. ✅ Abra o app no dispositivo/emulador
2. ✅ Pressione o botão "Test Crash"
3. ✅ O app irá crashar (comportamento esperado)
4. ✅ Reinicie o app para enviar relatório

### **Passo 3: Verificar no Console Firebase**
- **URL:** https://console.firebase.google.com/project/fypmatch-8ac3c/crashlytics
- **Tempo:** Até 5 minutos para aparecer
- **Debug:** Se não aparecer, ative logs de depuração

---

## 🔍 **Tipos de Teste Disponíveis**

### **1. Crash Fatal (Documentação Oficial)**
```kotlin
CrashlyticsTestHelper.addTestCrashButton(activity)
// Resultado: App fecha e envia relatório
```

### **2. Exceção Não Fatal**
```kotlin
CrashlyticsTestHelper.testNonFatalException()
// Resultado: App continua funcionando, mas erro é reportado
```

### **3. Logs Customizados**
```kotlin
CrashlyticsTestHelper.testCustomLogs()
// Resultado: Logs personalizados aparecem no console
```

### **4. Informações de Usuário**
```kotlin
CrashlyticsTestHelper.setTestUserInfo()
// Resultado: Crashes são associados ao usuário de teste
```

---

## 📊 **Verificação no Console Firebase**

### **Dashboard Principal**
- **URL:** https://console.firebase.google.com/project/fypmatch-8ac3c/crashlytics
- **Métricas:** Crash-free users, crashes por versão
- **Relatórios:** Stack traces detalhados

### **O que Verificar**
1. ✅ **Crash aparece** na lista de issues
2. ✅ **Stack trace** mostra "RuntimeException: Test Crash"
3. ✅ **Device info** está correto
4. ✅ **App version** está correta
5. ✅ **Custom keys** aparecem se configurados

---

## 🔧 **Depuração se Não Funcionar**

### **Habilitar Logs de Debug**
```bash
adb shell setprop log.tag.FirebaseCrashlytics DEBUG
adb logcat -s FirebaseCrashlytics
```

### **Verificar Configuração**
1. ✅ `google-services.json` no lugar correto
2. ✅ Plugin aplicado no app/build.gradle.kts
3. ✅ Internet disponível no dispositivo
4. ✅ App não está em modo offline

### **Forçar Envio Manual**
```kotlin
// Em caso de problemas, forçar envio
FirebaseCrashlytics.getInstance().sendUnsentReports()
```

---

## 🎯 **Métricas de Sucesso**

### **Configuração Correta**
- ✅ Crash aparece no console em < 5 minutos
- ✅ Stack trace está legível
- ✅ Informações de dispositivo corretas
- ✅ Versão do app identificada

### **Produção Ready**
- ✅ Crashes reais sendo capturados
- ✅ ANRs sendo reportados
- ✅ Custom keys funcionando
- ✅ User IDs sendo associados

---

## 📚 **Recursos Adicionais**

### **Documentação Oficial**
- [Getting Started](https://firebase.google.com/docs/crashlytics/get-started?platform=android&hl=pt-br)
- [Customize Reports](https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android)
- [Test Implementation](https://firebase.google.com/docs/crashlytics/test-implementation?platform=android)

### **Próximos Passos**
1. ✅ Remover código de teste após validação
2. ✅ Implementar tracking em fluxos reais
3. ✅ Configurar alertas para crashes críticos
4. ✅ Monitorar crash-free rate target > 99%

---

**✅ Crashlytics configurado conforme documentação oficial do Firebase!** 

**Dashboard:** https://console.firebase.google.com/project/fypmatch-8ac3c/crashlytics 