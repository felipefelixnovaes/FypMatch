# 📱 Guia Completo para Gerar APK - FypMatch

## 🎯 **Objetivo**
Este guia fornece instruções detalhadas para gerar APKs do aplicativo FypMatch, incluindo versões de debug e release.

---

## ✅ **Pré-requisitos**

### **Ambiente de Desenvolvimento:**
- **Java**: JDK 11 ou superior
- **Android Studio**: Hedgehog (2023.1.1) ou superior
- **Android SDK**: API 24-34 instalado
- **Git**: Para controle de versão
- **Conexão à Internet**: Para download de dependências

### **Configuração Firebase:**
- ✅ Arquivo `google-services.json` já está configurado
- ✅ Firebase projeto configurado
- ✅ Dependências Firebase incluídas

---

## 🔧 **Correções Aplicadas**

### **1. Fix da Versão do Android Gradle Plugin**
```kotlin
// build.gradle.kts (root) - CORRIGIDO
classpath("com.android.tools.build:gradle:8.0.2") // Era 7.4.2
```

### **2. Versões Alinhadas**
- **AGP**: 8.0.2 (em libs.versions.toml e build.gradle.kts)
- **Kotlin**: 1.8.10
- **Compose BOM**: 2023.03.00
- **Firebase BOM**: 32.2.2

---

## 🏗️ **Comandos para Gerar APK**

### **1. Limpar o Projeto**
```bash
# Linux/Mac
./gradlew clean

# Windows
gradlew.bat clean
```

### **2. Gerar APK de Debug**
```bash
# Linux/Mac
./gradlew assembleDebug

# Windows  
gradlew.bat assembleDebug
```

**Localização do APK:**
```
app/build/outputs/apk/debug/app-debug.apk
```

### **3. Gerar APK de Release (Não Assinado)**
```bash
# Linux/Mac
./gradlew assembleRelease

# Windows
gradlew.bat assembleRelease
```

**Localização do APK:**
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### **4. Build Completo (Debug + Instalação)**
```bash
# Linux/Mac
./gradlew clean assembleDebug installDebug

# Windows
gradlew.bat clean assembleDebug installDebug
```

---

## 🔐 **APK Assinado para Produção**

### **1. Criar Keystore (Primeira vez)**
```bash
keytool -genkey -v -keystore fypmatch-release-key.keystore -alias fypmatch -keyalg RSA -keysize 2048 -validity 10000
```

### **2. Configurar Signing no app/build.gradle.kts**
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../fypmatch-release-key.keystore")
            storePassword = "SUA_SENHA_KEYSTORE"
            keyAlias = "fypmatch"
            keyPassword = "SUA_SENHA_KEY"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### **3. Gerar APK Assinado**
```bash
./gradlew assembleRelease
```

---

## 📦 **Gerar AAB para Google Play Store**

### **Para Upload na Play Store:**
```bash
# Gerar Android App Bundle
./gradlew bundleRelease
```

**Localização do AAB:**
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 🧪 **Testes e Validação**

### **1. Instalar APK em Dispositivo/Emulador**
```bash
# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Via Gradle
./gradlew installDebug
```

### **2. Verificar APK**
```bash
# Informações do APK
aapt dump badging app/build/outputs/apk/debug/app-debug.apk

# Tamanho do APK
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

### **3. Logs de Debug**
```bash
# Ver logs em tempo real
adb logcat | grep "FypMatch"

# Limpar dados do app
adb shell pm clear com.ideiassertiva.FypMatch
```

---

## 🚀 **Script de Build Automatizado**

### **build-apk.sh** (Linux/Mac)
```bash
#!/bin/bash
echo "🚀 Iniciando build do FypMatch APK..."

# Limpar projeto
echo "🧹 Limpando projeto..."
./gradlew clean

# Build debug
echo "🔨 Gerando APK debug..."
./gradlew assembleDebug

# Build release
echo "📦 Gerando APK release..."
./gradlew assembleRelease

echo "✅ Build concluído!"
echo "📱 APK Debug: app/build/outputs/apk/debug/app-debug.apk"
echo "📱 APK Release: app/build/outputs/apk/release/app-release-unsigned.apk"

# Mostrar tamanho dos APKs
echo "📊 Tamanho dos APKs:"
ls -lh app/build/outputs/apk/debug/app-debug.apk
ls -lh app/build/outputs/apk/release/app-release-unsigned.apk
```

### **build-apk.bat** (Windows)
```batch
@echo off
echo 🚀 Iniciando build do FypMatch APK...

echo 🧹 Limpando projeto...
call gradlew.bat clean

echo 🔨 Gerando APK debug...
call gradlew.bat assembleDebug

echo 📦 Gerando APK release...
call gradlew.bat assembleRelease

echo ✅ Build concluído!
echo 📱 APK Debug: app\build\outputs\apk\debug\app-debug.apk
echo 📱 APK Release: app\build\outputs\apk\release\app-release-unsigned.apk
```

---

## ⚠️ **Troubleshooting**

### **Problemas Comuns:**

#### **1. Erro de Network/DNS**
```bash
# Se não conseguir baixar dependências
# Verifique conexão com internet
ping dl.google.com
ping repo1.maven.org
```

#### **2. Gradle Daemon Issues**
```bash
# Parar todos os daemons
./gradlew --stop

# Build com stack trace
./gradlew assembleDebug --stacktrace
```

#### **3. Memoria Insuficiente**
```bash
# Aumentar heap size no gradle.properties
org.gradle.jvmargs=-Xmx8192m -Dfile.encoding=UTF-8
```

#### **4. Versão do Java**
```bash
# Verificar versão
java -version
javac -version

# Deve ser Java 11 ou superior
```

---

## 📋 **Checklist de Release**

### **Antes de Gerar APK de Produção:**
- [ ] Versão atualizada em `app/build.gradle.kts` (`versionCode` e `versionName`)
- [ ] Changelog atualizado
- [ ] Testes de regressão executados
- [ ] Firebase configurado para produção
- [ ] Proguard/R8 configurado e testado
- [ ] Keystore de produção configurado
- [ ] Ícones e recursos finalizados
- [ ] Permissões revisadas
- [ ] APK testado em múltiplos dispositivos
- [ ] Performance verificada

### **Após Gerar APK:**
- [ ] APK instalado e testado
- [ ] Funcionalidades críticas validadas
- [ ] Login com Google funcionando
- [ ] Firebase conectado
- [ ] Navegação funcionando
- [ ] UI responsiva em diferentes tamanhos de tela

---

## 🏆 **APK Gerado com Sucesso!**

Quando o build for bem-sucedido, os APKs estarão disponíveis em:
- **Debug**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release**: `app/build/outputs/apk/release/app-release-unsigned.apk`
- **AAB**: `app/build/outputs/bundle/release/app-release.aab`

**Próximos passos:**
1. Testar o APK em dispositivos reais
2. Configurar signing para produção  
3. Upload na Google Play Store (via AAB)
4. Distribuição via Firebase App Distribution para beta testers