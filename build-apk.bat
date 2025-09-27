@echo off
setlocal enabledelayedexpansion

REM 🚀 FypMatch APK Build Script for Windows
REM Gera APKs debug e release automaticamente

echo 🚀 Iniciando build do FypMatch APK...
echo ======================================

REM Verificar se está no diretório correto
if not exist "gradlew.bat" (
    echo ❌ Erro: Execute este script na raiz do projeto ^(onde está o gradlew.bat^)
    pause
    exit /b 1
)

REM Verificar conectividade
echo 🌐 Verificando conectividade...
ping -n 1 google.com >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Aviso: Sem conexão com internet. Build pode falhar.
    set /p "continuar=Deseja continuar mesmo assim? (y/N): "
    if /i not "!continuar!"=="y" (
        exit /b 1
    )
)

REM Limpar projeto
echo 🧹 Limpando projeto...
call gradlew.bat clean
if errorlevel 1 (
    echo ❌ Falha na limpeza do projeto
    pause
    exit /b 1
)

REM Build debug
echo 🔨 Gerando APK debug...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ❌ Falha ao gerar APK Debug
    pause
    exit /b 1
)

REM Verificar se debug foi criado com sucesso
set "DEBUG_APK=app\build\outputs\apk\debug\app-debug.apk"
if exist "%DEBUG_APK%" (
    echo ✅ APK Debug gerado com sucesso!
    echo 📱 Localização: %DEBUG_APK%
    for %%A in ("%DEBUG_APK%") do echo 📊 Tamanho: %%~zA bytes
) else (
    echo ❌ Falha ao gerar APK Debug
    pause
    exit /b 1
)

REM Build release
echo 📦 Gerando APK release...
call gradlew.bat assembleRelease
if errorlevel 1 (
    echo ❌ Falha ao gerar APK Release
    pause
    exit /b 1
)

REM Verificar se release foi criado com sucesso
set "RELEASE_APK=app\build\outputs\apk\release\app-release-unsigned.apk"
if exist "%RELEASE_APK%" (
    echo ✅ APK Release gerado com sucesso!
    echo 📱 Localização: %RELEASE_APK%
    for %%A in ("%RELEASE_APK%") do echo 📊 Tamanho: %%~zA bytes
) else (
    echo ❌ Falha ao gerar APK Release
    pause
    exit /b 1
)

REM Opcionalmente gerar AAB para Play Store
set /p "gerar_aab=🏪 Deseja gerar AAB para Google Play Store? (y/N): "
if /i "!gerar_aab!"=="y" (
    echo 📦 Gerando Android App Bundle ^(AAB^)...
    call gradlew.bat bundleRelease
    
    set "AAB_FILE=app\build\outputs\bundle\release\app-release.aab"
    if exist "!AAB_FILE!" (
        echo ✅ AAB gerado com sucesso!
        echo 📱 Localização: !AAB_FILE!
        for %%A in ("!AAB_FILE!") do echo 📊 Tamanho: %%~zA bytes
    ) else (
        echo ❌ Falha ao gerar AAB
    )
)

REM Resumo final
echo.
echo 🏆 BUILD CONCLUÍDO COM SUCESSO!
echo =================================
echo 📱 APK Debug: %DEBUG_APK%
echo 📱 APK Release: %RELEASE_APK%
if exist "%AAB_FILE%" (
    echo 📱 AAB Release: %AAB_FILE%
)

echo.
echo 📋 Próximos passos:
echo 1. Testar o APK em um dispositivo Android
echo 2. Para instalar: adb install "%DEBUG_APK%"
echo 3. Para produção, configure signing no build.gradle
echo 4. Para Play Store, use o arquivo AAB

REM Oferecer para instalar automaticamente se ADB estiver disponível
where adb >nul 2>&1
if not errorlevel 1 (
    echo.
    set /p "instalar=📱 Deseja instalar o APK debug automaticamente? (y/N): "
    if /i "!instalar!"=="y" (
        echo 📲 Instalando APK...
        adb install -r "%DEBUG_APK%"
        if not errorlevel 1 (
            echo ✅ APK instalado com sucesso!
        ) else (
            echo ❌ Falha na instalação do APK
        )
    )
)

echo.
echo ✨ Script concluído! APKs prontos para uso.
pause