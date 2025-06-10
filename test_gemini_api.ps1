# 🧪 Teste da API Gemini
$apiKey = "AIzaSyAsUX8dj3_OKuHWQlEsBEGa0d3mWFqat2E"
$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    contents = @(
        @{
            parts = @(
                @{
                    text = "Explain how AI works in a few words"
                }
            )
        }
    )
} | ConvertTo-Json -Depth 10

Write-Host "🧪 Testando API do Gemini..." -ForegroundColor Yellow
Write-Host "URL: $url" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri $url -Method POST -Headers $headers -Body $body
    Write-Host "✅ API funcionando!" -ForegroundColor Green
    Write-Host "Resposta: $($response.candidates[0].content.parts[0].text)" -ForegroundColor White
} catch {
    Write-Host "❌ Erro na API:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
} 