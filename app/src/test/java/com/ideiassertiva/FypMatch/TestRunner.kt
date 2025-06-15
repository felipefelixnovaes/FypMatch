package com.ideiassertiva.FypMatch

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * 🧪 TestRunner - Suíte Principal de Testes FypMatch
 * 
 * Executa todos os testes automatizados do projeto:
 * - Testes de Autenticação (AuthRepository)
 * - Testes de Perfil (ProfileViewModel)
 * - Testes de Localização (LocationViewModel)
 * - Testes de Cálculo de Distância (DistanceCalculation)
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    com.ideiassertiva.FypMatch.auth.AuthRepositoryTest::class,
    com.ideiassertiva.FypMatch.profile.ProfileViewModelTest::class,
    com.ideiassertiva.FypMatch.location.LocationViewModelTest::class,
    com.ideiassertiva.FypMatch.distance.DistanceCalculationTest::class
)
class TestRunner {
    
    companion object {
        const val TAG = "🧪 FypMatch TestRunner"
        
        /**
         * Executa todos os testes e retorna um relatório resumido
         */
        @JvmStatic
        fun runAllTests(): TestExecutionReport {
            val startTime = System.currentTimeMillis()
            val results = mutableListOf<TestCategoryResult>()
            
            try {
                println("$TAG - Iniciando execução de todos os testes...")
                
                // === TESTES DE AUTENTICAÇÃO ===
                println("\n🔐 Executando testes de Autenticação...")
                val authResults = com.ideiassertiva.FypMatch.auth.AuthRepositoryTest.runAllAuthTests()
                results.add(TestCategoryResult(
                    category = "Autenticação",
                    tests = authResults,
                    passed = authResults.count { it.startsWith("✅") },
                    failed = authResults.count { it.startsWith("❌") }
                ))
                
                // === TESTES DE PERFIL ===
                println("\n👤 Executando testes de Perfil...")
                val profileResults = com.ideiassertiva.FypMatch.profile.ProfileViewModelTest.runAllProfileTests()
                results.add(TestCategoryResult(
                    category = "Perfil",
                    tests = profileResults,
                    passed = profileResults.count { it.startsWith("✅") },
                    failed = profileResults.count { it.startsWith("❌") }
                ))
                
                // === TESTES DE LOCALIZAÇÃO ===
                println("\n📍 Executando testes de Localização...")
                val locationResults = com.ideiassertiva.FypMatch.location.LocationViewModelTest.runAllLocationTests()
                results.add(TestCategoryResult(
                    category = "Localização",
                    tests = locationResults,
                    passed = locationResults.count { it.startsWith("✅") },
                    failed = locationResults.count { it.startsWith("❌") }
                ))
                
                // === TESTES DE CÁLCULO DE DISTÂNCIA ===
                println("\n📐 Executando testes de Cálculo de Distância...")
                val distanceResults = com.ideiassertiva.FypMatch.distance.DistanceCalculationTest.runAllDistanceTests()
                results.add(TestCategoryResult(
                    category = "Cálculo de Distância",
                    tests = distanceResults,
                    passed = distanceResults.count { it.startsWith("✅") },
                    failed = distanceResults.count { it.startsWith("❌") }
                ))
                
                val endTime = System.currentTimeMillis()
                val executionTime = endTime - startTime
                
                val totalPassed = results.sumOf { it.passed }
                val totalFailed = results.sumOf { it.failed }
                val totalTests = totalPassed + totalFailed
                val successRate = if (totalTests > 0) (totalPassed.toDouble() / totalTests * 100) else 0.0
                
                println("\n" + "=".repeat(60))
                println("📊 RELATÓRIO FINAL DE TESTES")
                println("=".repeat(60))
                println("⏱️  Tempo de execução: ${executionTime}ms")
                println("📈 Total de testes: $totalTests")
                println("✅ Testes aprovados: $totalPassed")
                println("❌ Testes falharam: $totalFailed")
                println("📊 Taxa de sucesso: ${String.format("%.1f", successRate)}%")
                println("=".repeat(60))
                
                results.forEach { category ->
                    println("\n📂 ${category.category}:")
                    category.tests.forEach { test ->
                        println("   $test")
                    }
                }
                
                return TestExecutionReport(
                    totalTests = totalTests,
                    passedTests = totalPassed,
                    failedTests = totalFailed,
                    successRate = successRate,
                    executionTimeMs = executionTime,
                    categories = results
                )
                
            } catch (e: Exception) {
                println("❌ Erro durante execução dos testes: ${e.message}")
                e.printStackTrace()
                
                return TestExecutionReport(
                    totalTests = 0,
                    passedTests = 0,
                    failedTests = 1,
                    successRate = 0.0,
                    executionTimeMs = System.currentTimeMillis() - startTime,
                    categories = listOf(TestCategoryResult(
                        category = "Erro de Execução",
                        tests = listOf("❌ ${e.message}"),
                        passed = 0,
                        failed = 1
                    ))
                )
            }
        }
        
        /**
         * Executa apenas testes de uma categoria específica
         */
        @JvmStatic
        fun runCategoryTests(category: String): List<String> {
            return when (category.lowercase()) {
                "auth", "autenticacao" -> {
                    try {
                        com.ideiassertiva.FypMatch.auth.AuthRepositoryTest.runAllAuthTests()
                    } catch (e: Exception) {
                        listOf("❌ Erro nos testes de Auth: ${e.message}")
                    }
                }
                "profile", "perfil" -> {
                    try {
                        com.ideiassertiva.FypMatch.profile.ProfileViewModelTest.runAllProfileTests()
                    } catch (e: Exception) {
                        listOf("❌ Erro nos testes de Profile: ${e.message}")
                    }
                }
                "location", "localizacao" -> {
                    try {
                        com.ideiassertiva.FypMatch.location.LocationViewModelTest.runAllLocationTests()
                    } catch (e: Exception) {
                        listOf("❌ Erro nos testes de Location: ${e.message}")
                    }
                }
                "distance", "distancia" -> {
                    try {
                        com.ideiassertiva.FypMatch.distance.DistanceCalculationTest.runAllDistanceTests()
                    } catch (e: Exception) {
                        listOf("❌ Erro nos testes de Distance: ${e.message}")
                    }
                }
                else -> listOf("❌ Categoria '$category' não encontrada")
            }
        }
        
        /**
         * Retorna informações sobre as categorias de teste disponíveis
         */
        @JvmStatic
        fun getAvailableCategories(): List<String> {
            return listOf(
                "🔐 Autenticação (auth) - Testes de login, registro e gerenciamento de usuários",
                "👤 Perfil (profile) - Testes de criação, edição e validação de perfis",
                "📍 Localização (location) - Testes de GPS, permissões e proximidade",
                "📐 Distância (distance) - Testes de cálculos de distância e precisão"
            )
        }
    }
}

/**
 * Modelo de dados para relatório de execução de testes
 */
data class TestExecutionReport(
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val successRate: Double,
    val executionTimeMs: Long,
    val categories: List<TestCategoryResult>
)

/**
 * Modelo de dados para resultado de uma categoria de testes
 */
data class TestCategoryResult(
    val category: String,
    val tests: List<String>,
    val passed: Int,
    val failed: Int
) 