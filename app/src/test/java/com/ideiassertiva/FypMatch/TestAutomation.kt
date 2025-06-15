package com.ideiassertiva.FypMatch

import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🤖 Sistema de Automação de Testes FypMatch
 * 
 * Executa testes automaticamente e gera relatórios em múltiplos formatos:
 * - Relatório em texto simples
 * - Relatório HTML interativo
 * - Relatório JSON para integração
 * - Notificações de falhas
 */
object TestAutomation {
    
    private const val TAG = "🤖 TestAutomation"
    private const val REPORTS_DIR = "test-reports"
    
    /**
     * Executa todos os testes com relatórios automáticos
     */
    suspend fun runAutomatedTests(): TestExecutionReport = withContext(Dispatchers.IO) {
        println("$TAG - Iniciando execução automatizada de testes...")
        
        val startTime = System.currentTimeMillis()
        
        try {
            // Criar diretório de relatórios
            createReportsDirectory()
            
            // Executar testes
            val report = TestRunner.runAllTests()
            
            // Gerar relatórios em paralelo
            val reportJobs = listOf(
                async { generateTextReport(report) },
                async { generateHtmlReport(report) },
                async { generateJsonReport(report) }
            )
            
            // Aguardar conclusão de todos os relatórios
            reportJobs.awaitAll()
            
            // Verificar se há falhas e notificar
            if (report.failedTests > 0) {
                notifyTestFailures(report)
            }
            
            val totalTime = System.currentTimeMillis() - startTime
            println("$TAG - Automação concluída em ${totalTime}ms")
            
            report
            
        } catch (e: Exception) {
            println("❌ Erro na automação de testes: ${e.message}")
            e.printStackTrace()
            
            TestExecutionReport(
                totalTests = 0,
                passedTests = 0,
                failedTests = 1,
                successRate = 0.0,
                executionTimeMs = System.currentTimeMillis() - startTime,
                categories = listOf(TestCategoryResult(
                    category = "Erro de Automação",
                    tests = listOf("❌ ${e.message}"),
                    passed = 0,
                    failed = 1
                ))
            )
        }
    }
    
    /**
     * Executa testes de uma categoria específica
     */
    suspend fun runCategoryTests(category: String): List<String> = withContext(Dispatchers.IO) {
        println("$TAG - Executando testes da categoria: $category")
        
        try {
            val results = TestRunner.runCategoryTests(category)
            
            // Gerar relatório específico da categoria
            generateCategoryReport(category, results)
            
            results
        } catch (e: Exception) {
            println("❌ Erro ao executar testes da categoria $category: ${e.message}")
            listOf("❌ Erro: ${e.message}")
        }
    }
    
    /**
     * Cria diretório para relatórios se não existir
     */
    private fun createReportsDirectory() {
        val reportsDir = File(REPORTS_DIR)
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
            println("📁 Diretório de relatórios criado: ${reportsDir.absolutePath}")
        }
    }
    
    /**
     * Gera relatório em formato texto
     */
    private suspend fun generateTextReport(report: TestExecutionReport) = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "$REPORTS_DIR/test-report-$timestamp.txt"
            
            val content = buildString {
                appendLine("🧪 RELATÓRIO DE TESTES FYPMATCH")
                appendLine("=" * 50)
                appendLine("📅 Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
                appendLine("⏱️ Tempo de execução: ${report.executionTimeMs}ms")
                appendLine("📊 Total de testes: ${report.totalTests}")
                appendLine("✅ Testes aprovados: ${report.passedTests}")
                appendLine("❌ Testes falharam: ${report.failedTests}")
                appendLine("📈 Taxa de sucesso: ${"%.1f".format(report.successRate)}%")
                appendLine("=" * 50)
                appendLine()
                
                report.categories.forEach { category ->
                    appendLine("📂 ${category.category}")
                    appendLine("-" * 30)
                    category.tests.forEach { test ->
                        appendLine("  $test")
                    }
                    appendLine()
                }
                
                appendLine("=" * 50)
                if (report.failedTests > 0) {
                    appendLine("❌ ATENÇÃO: ${report.failedTests} teste(s) falharam!")
                } else {
                    appendLine("🎉 TODOS OS TESTES PASSARAM!")
                }
            }
            
            File(fileName).writeText(content)
            println("📄 Relatório texto gerado: $fileName")
            
        } catch (e: Exception) {
            println("❌ Erro ao gerar relatório texto: ${e.message}")
        }
    }
    
    /**
     * Gera relatório em formato HTML
     */
    private suspend fun generateHtmlReport(report: TestExecutionReport) = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "$REPORTS_DIR/test-report-$timestamp.html"
            
            val statusColor = if (report.failedTests == 0) "#4CAF50" else "#F44336"
            val statusText = if (report.failedTests == 0) "SUCESSO" else "FALHAS DETECTADAS"
            
            val content = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Relatório de Testes - FypMatch</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; background: #f5f5f5; }
                        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                        .header { text-align: center; margin-bottom: 30px; }
                        .status { padding: 15px; border-radius: 5px; color: white; font-weight: bold; margin: 20px 0; }
                        .metrics { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 30px 0; }
                        .metric { background: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; }
                        .metric-value { font-size: 2em; font-weight: bold; color: #333; }
                        .metric-label { color: #666; margin-top: 5px; }
                        .category { margin: 30px 0; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                        .category-header { font-size: 1.2em; font-weight: bold; margin-bottom: 15px; }
                        .test-item { padding: 8px 0; border-bottom: 1px solid #eee; }
                        .test-passed { color: #4CAF50; }
                        .test-failed { color: #F44336; }
                        .progress-bar { width: 100%; height: 20px; background: #ddd; border-radius: 10px; overflow: hidden; margin: 20px 0; }
                        .progress-fill { height: 100%; background: linear-gradient(90deg, #4CAF50, #8BC34A); transition: width 0.3s ease; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🧪 Relatório de Testes - FypMatch</h1>
                            <p>📅 ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}</p>
                        </div>
                        
                        <div class="status" style="background-color: $statusColor;">
                            $statusText
                        </div>
                        
                        <div class="metrics">
                            <div class="metric">
                                <div class="metric-value">${report.totalTests}</div>
                                <div class="metric-label">Total de Testes</div>
                            </div>
                            <div class="metric">
                                <div class="metric-value" style="color: #4CAF50;">${report.passedTests}</div>
                                <div class="metric-label">Aprovados</div>
                            </div>
                            <div class="metric">
                                <div class="metric-value" style="color: #F44336;">${report.failedTests}</div>
                                <div class="metric-label">Falharam</div>
                            </div>
                            <div class="metric">
                                <div class="metric-value">${"%.1f".format(report.successRate)}%</div>
                                <div class="metric-label">Taxa de Sucesso</div>
                            </div>
                        </div>
                        
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${report.successRate}%;"></div>
                        </div>
                        
                        ${report.categories.joinToString("") { category ->
                            """
                            <div class="category">
                                <div class="category-header">📂 ${category.category}</div>
                                ${category.tests.joinToString("") { test ->
                                    val cssClass = if (test.startsWith("✅")) "test-passed" else "test-failed"
                                    """<div class="test-item $cssClass">$test</div>"""
                                }}
                            </div>
                            """
                        }}
                        
                        <div style="margin-top: 30px; text-align: center; color: #666;">
                            ⏱️ Tempo de execução: ${report.executionTimeMs}ms
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()
            
            File(fileName).writeText(content)
            println("🌐 Relatório HTML gerado: $fileName")
            
        } catch (e: Exception) {
            println("❌ Erro ao gerar relatório HTML: ${e.message}")
        }
    }
    
    /**
     * Gera relatório em formato JSON
     */
    private suspend fun generateJsonReport(report: TestExecutionReport) = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "$REPORTS_DIR/test-report-$timestamp.json"
            
            val content = """
                {
                  "timestamp": "${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())}",
                  "summary": {
                    "totalTests": ${report.totalTests},
                    "passedTests": ${report.passedTests},
                    "failedTests": ${report.failedTests},
                    "successRate": ${report.successRate},
                    "executionTimeMs": ${report.executionTimeMs}
                  },
                  "categories": [
                    ${report.categories.joinToString(",\n    ") { category ->
                        """
                        {
                          "name": "${category.category}",
                          "passed": ${category.passed},
                          "failed": ${category.failed},
                          "tests": [
                            ${category.tests.joinToString(",\n            ") { "\"$it\"" }}
                          ]
                        }
                        """.trimIndent()
                    }}
                  ]
                }
            """.trimIndent()
            
            File(fileName).writeText(content)
            println("📋 Relatório JSON gerado: $fileName")
            
        } catch (e: Exception) {
            println("❌ Erro ao gerar relatório JSON: ${e.message}")
        }
    }
    
    /**
     * Gera relatório específico para uma categoria
     */
    private suspend fun generateCategoryReport(category: String, results: List<String>) = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "$REPORTS_DIR/category-$category-$timestamp.txt"
            
            val passed = results.count { it.startsWith("✅") }
            val failed = results.count { it.startsWith("❌") }
            val total = passed + failed
            val successRate = if (total > 0) (passed.toDouble() / total * 100) else 0.0
            
            val content = buildString {
                appendLine("📂 RELATÓRIO DE CATEGORIA: $category")
                appendLine("=" * 40)
                appendLine("📅 Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
                appendLine("📊 Total: $total")
                appendLine("✅ Passou: $passed")
                appendLine("❌ Falhou: $failed")
                appendLine("📈 Taxa de sucesso: ${"%.1f".format(successRate)}%")
                appendLine("=" * 40)
                appendLine()
                
                results.forEach { result ->
                    appendLine(result)
                }
            }
            
            File(fileName).writeText(content)
            println("📂 Relatório de categoria gerado: $fileName")
            
        } catch (e: Exception) {
            println("❌ Erro ao gerar relatório de categoria: ${e.message}")
        }
    }
    
    /**
     * Notifica sobre falhas nos testes
     */
    private fun notifyTestFailures(report: TestExecutionReport) {
        println("\n" + "⚠️ " * 20)
        println("🚨 ALERTA: FALHAS DETECTADAS NOS TESTES!")
        println("⚠️ " * 20)
        println("❌ ${report.failedTests} de ${report.totalTests} testes falharam")
        println("📉 Taxa de sucesso: ${"%.1f".format(report.successRate)}%")
        
        report.categories.filter { it.failed > 0 }.forEach { category ->
            println("\n📂 ${category.category}: ${category.failed} falha(s)")
            category.tests.filter { it.startsWith("❌") }.forEach { test ->
                println("   $test")
            }
        }
        
        println("\n💡 Verifique os relatórios detalhados em: $REPORTS_DIR/")
        println("⚠️ " * 20)
    }
    
    /**
     * Operador de extensão para repetir strings
     */
    private operator fun String.times(n: Int): String = this.repeat(n)
}

// Classes de dados definidas em TestRunner.kt para evitar duplicação

/**
 * 🎯 Função principal para executar testes
 */
suspend fun main() {
    val report = TestAutomation.runAutomatedTests()
    
    if (report.failedTests > 0) {
        System.exit(1) // Falha para CI/CD
    }
} 