# Story: Perfil Complementar FypMatch via IA pessoal

## Objetivo

Permitir que o usuario copie um prompt para sua IA pessoal, cole no FypMatch o Perfil Complementar gerado e use esse contexto como camada auxiliar para compatibilidade e Conselheira IA.

## Escopo

- Campo `complementaryProfile` no cadastro do usuario.
- Prompt oficial para IA externa do usuario.
- Parser do JSON `fypmatch_complementary_profile` com fallback para texto bruto.
- Tela Android para copiar prompt, importar resposta, revisar status e apagar o perfil.
- Integracao com Conselheira IA por contexto sanitizado.
- Ajuste leve no score de compatibilidade quando houver perfil complementar.

## Checklist

- [x] Modelo e helpers do perfil complementar mantidos com defaults Firestore.
- [x] Parser estruturado + fallback implementado.
- [x] Persistencia no `users/{userId}.complementaryProfile`.
- [x] Tela criada e conectada em Configuracoes.
- [x] Conselheira IA passa a receber contexto complementar.
- [x] Compatibilidade considera sinais complementares com peso leve.
- [x] Testes unitarios do parser adicionados.
- [x] Rodar quality gates finais.

## Validacao

- `./gradlew.bat testDebugUnitTest` passou.
- `./gradlew.bat lintDebug` falhou com erros preexistentes fora desta story; primeiro erro em `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/ChatRepository.kt` por uso de `java.time.LocalDateTime.now()` com `minSdk 24`.
- `npm test` no monorepo falhou em teste preexistente `resolvePaths: correct path`.
- `npm run lint` e `npm run typecheck` nao existem no `package.json` do monorepo; o app Android tambem nao possui `package.json` proprio.

## File List

- `app/src/main/java/com/ideiassertiva/FypMatch/model/User.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/model/ComplementaryProfile.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/model/ComplementaryProfileParser.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/model/AICounselor.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/model/CompatibilityML.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/UserRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/AICounselorRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/DiscoveryRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/AICounselorViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/ComplementaryProfileViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/ComplementaryProfileScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/SettingsScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/navigation/FypMatchNavigation.kt`
- `app/src/test/java/com/ideiassertiva/FypMatch/model/ComplementaryProfileParserTest.kt`
- `app/build.gradle.kts`
- `docs/stories/2026-06-07-complementary-profile.md`
