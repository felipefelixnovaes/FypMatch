# Story: Hotfix salvar perfil e creditos por anuncio

## Objetivo

Corrigir dois bloqueios observados no beta: perfil inicial preenchido continuava sendo tratado como incompleto e o botao de assistir anuncio nao liberava creditos utilizaveis pela Conselheira IA.

## Escopo

- Ajustar a regra de perfil completo para nao exigir foto enquanto upload de foto nao esta disponivel na tela inicial.
- Salvar o perfil inicial com merge parcial em `users/{userId}.profile`, preservando email, acesso e flags do usuario.
- Marcar `profile.isProfileComplete` ao salvar criacao/edicao de perfil.
- Conectar `AdsScreen` ao `AdsRepository` real via `AdsViewModel`.
- Fazer `AICounselorRepository` usar o mesmo `AdsRepository` singleton injetado pelo Hilt.
- Adicionar testes de regressao para completude de perfil e recompensa diaria de anuncios.

## Checklist

- [x] Perfil completo sem foto passa na regra atual do app.
- [x] Perfil com selecao obrigatoria ausente permanece incompleto.
- [x] Save inicial do perfil usa `SetOptions.merge()`.
- [x] Tela de anuncios recompensa o mesmo repositorio usado pela IA.
- [x] Limite diario de anuncios permanece testado.
- [x] Beta `1.0.4 (5)` publicado no Firebase App Distribution.

## Validacao

- `./gradlew.bat testDebugUnitTest --tests "com.ideiassertiva.FypMatch.model.UserProfileCompletionTest"` passou.
- `./gradlew.bat testDebugUnitTest --tests "com.ideiassertiva.FypMatch.repository.AdsRepositoryTest"` passou.
- `./gradlew.bat testDebugUnitTest` passou.
- `./gradlew.bat assembleRelease` passou.
- Build release executou `:app:uploadCrashlyticsMappingFileRelease`.
- `firebase appdistribution:distribute` publicou `1.0.4 (5)` com sucesso.

## File List

- `app/src/main/java/com/ideiassertiva/FypMatch/model/User.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/UserRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/AICounselorRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/di/AppModule.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/ProfileViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/ProfileEditViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/AdsViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/AdsScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/model/ComplementaryProfileParser.kt`
- `app/src/test/java/com/ideiassertiva/FypMatch/model/UserProfileCompletionTest.kt`
- `app/src/test/java/com/ideiassertiva/FypMatch/repository/AdsRepositoryTest.kt`
- `app/build.gradle.kts`
- `docs/stories/2026-06-07-profile-save-ads-hotfix.md`
