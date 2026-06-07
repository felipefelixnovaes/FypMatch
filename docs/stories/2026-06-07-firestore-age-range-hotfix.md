# Story: Hotfix Firestore ageRange no login Google

## Objetivo

Corrigir o crash no aparelho beta ao fazer login com Google quando o documento `users/{userId}` possui `preferences.ageRange` salvo em formato legado como string.

## Escopo

- Trocar a persistencia de idade preferida em `UserPreferences` para campos Firestore-safe `minAge` e `maxAge`.
- Manter `ageRange` como propriedade calculada para compatibilidade interna do app.
- Ignorar campos extras em documentos antigos de `User`, `UserProfile` e `UserPreferences`.
- Blindar leituras `toObject(User::class.java)` com mapper tolerante e fallback de usuario minimo.
- Adicionar teste de regressao com `CustomClassMapper` reproduzindo o payload legado.
- Publicar beta `1.0.3 (4)` no Firebase App Distribution.

## Checklist

- [x] Crash reproduzido em teste com `preferences.ageRange` string.
- [x] `UserPreferences` persiste `minAge` e `maxAge` em vez de `IntRange`.
- [x] `ageRange` nao volta a ser serializado para Firestore.
- [x] Leituras de usuario via Auth, User e Discovery repositories passam por mapper tolerante.
- [x] Versao Android atualizada para `1.0.3 (4)`.
- [x] APK release publicado no grupo `beta-fypmatch`.

## Validacao

- `./gradlew.bat testDebugUnitTest` passou.
- `./gradlew.bat assembleRelease` passou.
- Build release executou `:app:uploadCrashlyticsMappingFileRelease`.
- `firebase appdistribution:distribute` publicou `1.0.3 (4)` com sucesso.

## File List

- `app/src/main/java/com/ideiassertiva/FypMatch/model/User.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/FirestoreUserMapper.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/AuthRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/UserRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/DiscoveryRepository.kt`
- `app/src/test/java/com/ideiassertiva/FypMatch/model/UserPreferencesFirestoreTest.kt`
- `app/build.gradle.kts`
- `docs/stories/2026-06-07-firestore-age-range-hotfix.md`
