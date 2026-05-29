# Relatorio de Testes — FypMatch

## Status: Infraestrutura Implementada

### Testes Unitarios Criados

| Arquivo | Metodos Testados | Qtd |
|---------|-----------------|-----|
| `repository/ReportRepositoryTest.kt` | submitReport, blockUser, isUserBlocked | 3 |
| ViewModels (planejados) | WaitlistViewModel, DiscoveryViewModel | pendente |

### Testes de UI Planejados

| Tela | Testes | Status |
|------|--------|--------|
| DiscoveryScreen | existencia de botoes Curtir/Passar | pendente |
| WelcomeScreen | existencia de titulo e botoes | pendente |
| ReportScreen | motivos de denuncia, botao desabilitado | pendente |
| BlockConfirmationDialog | nome de usuario, botoes | pendente |

### Infraestrutura de Teste
- JUnit 4 (compativel com projeto existente)
- MockK para mocking Kotlin
- kotlinx-coroutines-test para coroutines
- Compose Testing (createComposeRule, onNodeWithText)

### Como Rodar
```bash
# Windows
scripts\ci-check.bat

# Linux/Mac
./scripts/ci-check.sh

# Direto com Gradle
./gradlew testDebugUnitTest
```

### Observacoes dos Squads
1. **qa-engineer**: APIs reais dos ViewModels diferem dos templates — `WaitlistViewModel.joinWaitlist()` recebe parametros tipados (nao Map), `DiscoveryViewModel` usa `@HiltViewModel`
2. **dev-mobile**: `ReportScreen` e `BlockConfirmationDialog` sao novos componentes que precisam ser criados no diretorio `ui/screens/`
3. **devops-engineer**: CI configurado para rodar `testDebugUnitTest` automaticamente

### Proximos Passos
1. Criar `ReportScreen.kt` e `BlockConfirmationDialog.kt` no diretorio `ui/screens/`
2. Adicionar dependencias MockK no `libs.versions.toml` e `app/build.gradle.kts`
3. Adaptar testes unitarios para APIs reais dos ViewModels
4. Executar `./gradlew testDebugUnitTest` para validar