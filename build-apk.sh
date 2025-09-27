#!/bin/bash

# 🚀 FypMatch APK Build Script
# Gera APKs debug e release automaticamente

set -e  # Parar em caso de erro

echo "🚀 Iniciando build do FypMatch APK..."
echo "======================================"

# Verificar se está no diretório correto
if [ ! -f "gradlew" ]; then
    echo "❌ Erro: Execute este script na raiz do projeto (onde está o gradlew)"
    exit 1
fi

# Verificar conectividade
echo "🌐 Verificando conectividade..."
if ! ping -c 1 google.com &> /dev/null; then
    echo "⚠️  Aviso: Sem conexão com internet. Build pode falhar."
    read -p "Deseja continuar mesmo assim? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Limpar projeto
echo "🧹 Limpando projeto..."
./gradlew clean

# Build debug
echo "🔨 Gerando APK debug..."
./gradlew assembleDebug

# Verificar se debug foi criado com sucesso
DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$DEBUG_APK" ]; then
    echo "✅ APK Debug gerado com sucesso!"
    echo "📱 Localização: $DEBUG_APK"
    echo "📊 Tamanho: $(ls -lh "$DEBUG_APK" | awk '{print $5}')"
else
    echo "❌ Falha ao gerar APK Debug"
    exit 1
fi

# Build release
echo "📦 Gerando APK release..."
./gradlew assembleRelease

# Verificar se release foi criado com sucesso
RELEASE_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
if [ -f "$RELEASE_APK" ]; then
    echo "✅ APK Release gerado com sucesso!"
    echo "📱 Localização: $RELEASE_APK"
    echo "📊 Tamanho: $(ls -lh "$RELEASE_APK" | awk '{print $5}')"
else
    echo "❌ Falha ao gerar APK Release"
    exit 1
fi

# Opcionalmente gerar AAB para Play Store
read -p "🏪 Deseja gerar AAB para Google Play Store? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📦 Gerando Android App Bundle (AAB)..."
    ./gradlew bundleRelease
    
    AAB_FILE="app/build/outputs/bundle/release/app-release.aab"
    if [ -f "$AAB_FILE" ]; then
        echo "✅ AAB gerado com sucesso!"
        echo "📱 Localização: $AAB_FILE"
        echo "📊 Tamanho: $(ls -lh "$AAB_FILE" | awk '{print $5}')"
    else
        echo "❌ Falha ao gerar AAB"
    fi
fi

# Resumo final
echo ""
echo "🏆 BUILD CONCLUÍDO COM SUCESSO!"
echo "================================="
echo "📱 APK Debug: $DEBUG_APK"
echo "📱 APK Release: $RELEASE_APK"
if [ -f "$AAB_FILE" ]; then
    echo "📱 AAB Release: $AAB_FILE"
fi

echo ""
echo "📋 Próximos passos:"
echo "1. Testar o APK em um dispositivo Android"
echo "2. Para instalar: adb install $DEBUG_APK"
echo "3. Para produção, configure signing no build.gradle"
echo "4. Para Play Store, use o arquivo AAB"

# Oferecer para instalar automaticamente se ADB estiver disponível
if command -v adb &> /dev/null; then
    echo ""
    read -p "📱 Deseja instalar o APK debug automaticamente? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "📲 Instalando APK..."
        adb install -r "$DEBUG_APK"
        echo "✅ APK instalado com sucesso!"
    fi
fi

echo ""
echo "✨ Script concluído! APKs prontos para uso."