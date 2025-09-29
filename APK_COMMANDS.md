# 📱 APK Quick Commands - FypMatch

Este arquivo contém comandos rápidos para gerar APKs do FypMatch.

## 🚀 Comandos Principais

### Debug APK (Para testes)
```bash
./gradlew assembleDebug
```
**Arquivo gerado:** `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (Para distribuição)
```bash
./gradlew assembleRelease
```
**Arquivo gerado:** `app/build/outputs/apk/release/app-release-unsigned.apk`

### AAB para Play Store
```bash
./gradlew bundleRelease
```
**Arquivo gerado:** `app/build/outputs/bundle/release/app-release.aab`

## 🛠️ Scripts Automatizados

### Linux/Mac
```bash
# Verificar configuração
./check-config.sh

# Gerar APKs automaticamente
./build-apk.sh
```

### Windows
```batch
# Gerar APKs automaticamente
build-apk.bat
```

## 🔧 Comandos de Limpeza

```bash
# Limpar completamente
./gradlew clean

# Limpar + Build + Instalar
./gradlew clean assembleDebug installDebug
```

## 📱 Instalação

```bash
# Instalar via Gradle
./gradlew installDebug

# Instalar via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 📊 Informações do APK

```bash
# Ver informações do APK
aapt dump badging app/build/outputs/apk/debug/app-debug.apk

# Ver tamanho
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

## 🔍 Debug e Logs

```bash
# Ver logs em tempo real
adb logcat | grep "FypMatch"

# Limpar dados do app
adb shell pm clear com.ideiassertiva.FypMatch
```

---

**💡 Dica:** Use `./check-config.sh` primeiro para verificar se tudo está configurado corretamente antes de gerar os APKs.