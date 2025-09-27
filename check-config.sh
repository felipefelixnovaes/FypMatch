#!/bin/bash

# 🔍 FypMatch Build Configuration Checker
# Verifica se a configuração está correta antes de gerar APK

echo "🔍 Verificando configuração do projeto FypMatch..."
echo "=================================================="

# Verificar arquivos essenciais
echo "📁 Verificando arquivos essenciais..."

REQUIRED_FILES=(
    "gradlew"
    "gradle.properties"
    "settings.gradle.kts"
    "build.gradle.kts"
    "app/build.gradle.kts"
    "app/google-services.json"
    "gradle/libs.versions.toml"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (FALTANDO)"
    fi
done

echo ""
echo "🔧 Verificando configurações..."

# Verificar versões do AGP
echo "📊 Versões do Android Gradle Plugin:"
if [ -f "gradle/libs.versions.toml" ]; then
    AGP_VERSION=$(grep "agp = " gradle/libs.versions.toml | cut -d'"' -f2)
    echo "   libs.versions.toml: $AGP_VERSION"
fi

if [ -f "build.gradle.kts" ]; then
    ROOT_AGP_VERSION=$(grep "com.android.tools.build:gradle:" build.gradle.kts | cut -d'"' -f2)
    echo "   build.gradle.kts (root): $ROOT_AGP_VERSION"
fi

# Verificar Java
echo ""
echo "☕ Java Configuration:"
java -version 2>&1 | head -1
if command -v javac &> /dev/null; then
    javac -version 2>&1
fi

# Verificar Android SDK
echo ""
echo "📱 Android SDK:"
if [ -n "$ANDROID_HOME" ]; then
    echo "   ANDROID_HOME: $ANDROID_HOME"
    if [ -d "$ANDROID_HOME" ]; then
        echo "   ✅ Diretório existe"
    else
        echo "   ❌ Diretório não encontrado"
    fi
else
    echo "   ⚠️  ANDROID_HOME não configurado"
fi

# Verificar conectividade
echo ""
echo "🌐 Conectividade:"
if ping -c 1 google.com &> /dev/null; then
    echo "   ✅ Internet disponível"
else
    echo "   ❌ Sem conexão com internet"
fi

# Verificar ADB
echo ""
echo "🔌 Android Debug Bridge:"
if command -v adb &> /dev/null; then
    echo "   ✅ ADB disponível"
    ADB_DEVICES=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
    echo "   📱 Dispositivos conectados: $ADB_DEVICES"
else
    echo "   ❌ ADB não encontrado"
fi

# Verificar Gradle Wrapper
echo ""
echo "🛠️  Gradle Wrapper:"
if [ -f "gradlew" ] && [ -x "gradlew" ]; then
    echo "   ✅ gradlew executável"
    GRADLE_VERSION=$(./gradlew --version 2>/dev/null | grep "Gradle" | head -1)
    if [ -n "$GRADLE_VERSION" ]; then
        echo "   $GRADLE_VERSION"
    fi
else
    echo "   ❌ gradlew não executável"
fi

# Verificar espaço em disco
echo ""
echo "💾 Espaço em disco:"
df -h . | tail -1 | awk '{print "   Disponível: " $4 " (" $5 " usado)"}'

echo ""
echo "📋 Resumo da verificação:"
echo "========================"

# Contar sucessos
SUCCESS_COUNT=0
TOTAL_CHECKS=0

# Contadores baseados nas verificações acima
for file in "${REQUIRED_FILES[@]}"; do
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    if [ -f "$file" ]; then
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    fi
done

echo "   Arquivos essenciais: $SUCCESS_COUNT/$TOTAL_CHECKS"

if command -v java &> /dev/null; then
    echo "   ✅ Java instalado"
else
    echo "   ❌ Java não encontrado"
fi

if ping -c 1 google.com &> /dev/null; then
    echo "   ✅ Conectividade OK"
else
    echo "   ⚠️  Sem internet"
fi

echo ""
if [ $SUCCESS_COUNT -eq $TOTAL_CHECKS ]; then
    echo "🎉 Configuração parece estar correta!"
    echo "   Execute: ./build-apk.sh para gerar os APKs"
else
    echo "⚠️  Alguns problemas encontrados. Verifique os itens marcados com ❌"
fi

echo ""
echo "📚 Para mais informações, consulte: GERAR_APK.md"