# Script para habilitar Firebase Analytics Debug Mode
# Execute este script para debugar no dispositivo real

Write-Host "🔧 Habilitando Firebase Analytics Debug Mode para FypMatch..." -ForegroundColor Green

# Verificar se ADB está disponível
try {
    adb version | Out-Null
    Write-Host "✅ ADB encontrado" -ForegroundColor Green
} catch {
    Write-Host "❌ ADB não encontrado. Instale o Android SDK Platform Tools." -ForegroundColor Red
    exit 1
}

# Verificar se dispositivo está conectado
$devices = adb devices
if ($devices -match "device$") {
    Write-Host "✅ Dispositivo Android conectado" -ForegroundColor Green
} else {
    Write-Host "❌ Nenhum dispositivo Android conectado. Conecte um dispositivo via USB ou WiFi." -ForegroundColor Red
    exit 1
}

# Habilitar debug mode
Write-Host "🎯 Habilitando Analytics Debug Mode..." -ForegroundColor Yellow
adb shell setprop debug.firebase.analytics.app com.ideiassertiva.FypMatch

# Habilitar verbose logging
Write-Host "📝 Habilitando Verbose Logging..." -ForegroundColor Yellow
adb shell setprop log.tag.FA VERBOSE
adb shell setprop log.tag.FA-SVC VERBOSE

Write-Host ""
Write-Host "🎉 Firebase Analytics Debug Mode ATIVADO!" -ForegroundColor Green
Write-Host ""
Write-Host "📱 Agora faça o seguinte:" -ForegroundColor Cyan
Write-Host "1. Abra o app FypMatch no dispositivo"
Write-Host "2. Faça algumas ações (login, navegação, etc.)"
Write-Host "3. Vá para Firebase Console > Analytics > DebugView"
Write-Host "4. Selecione seu dispositivo na lista"
Write-Host ""
Write-Host "🔍 Para ver logs em tempo real:" -ForegroundColor Yellow
Write-Host "adb logcat -s FA FA-SVC"
Write-Host ""
Write-Host "🛑 Para desabilitar debug mode:" -ForegroundColor Red
Write-Host "adb shell setprop debug.firebase.analytics.app ."
Write-Host ""

# Opcional: Abrir logcat automaticamente
$openLogcat = Read-Host "Deseja abrir o logcat para ver eventos em tempo real? (s/n)"
if ($openLogcat -eq "s" -or $openLogcat -eq "S") {
    Write-Host "📊 Abrindo logcat... Pressione Ctrl+C para parar" -ForegroundColor Green
    adb logcat -s FA FA-SVC
} 