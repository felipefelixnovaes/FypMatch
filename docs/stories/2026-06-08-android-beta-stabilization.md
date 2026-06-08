# Story: Estabilização Android beta duas contas

## Objetivo

Fechar o ciclo Android para testar duas contas reais no Firebase Tester, garantindo que curtidas, matches, chat, missões e créditos por anúncio usem contratos reais do app.

## Escopo

- Manter iOS, web e UI v2 mock fora deste ciclo.
- Consolidar curtidas recebidas/enviadas reais no Firestore.
- Fazer a tela de detalhes agir sobre o usuário aberto, não sobre o card corrente do Discovery.
- Abrir chat real por `conversationId` após match.
- Manter `conversations.participantIds` e `messages.participantIds` como contrato de autorização.
- Integrar rewarded ad via Google Mobile Ads SDK com IDs de teste configuráveis.
- Rotular a Conselheira IA como beta/local enquanto não houver endpoint remoto real.

## Checklist

- [x] Versão Android avançada para `1.0.15 (20)`.
- [x] Curtidas recebidas e enviadas carregam do Firestore.
- [x] `UserDetailsScreen` executa swipe no `userId` aberto.
- [x] Match criado em detalhes abre `EnhancedChat`.
- [x] Chat usa conversa estável entre dois participantes para evitar duplicação.
- [x] UI de erro do chat aceita mensagens longas sem quebrar layout.
- [x] Rewarded ad só credita após callback de recompensa.
- [x] Conselheira IA informa modo beta/local.
- [x] Diretórios mock `*_v2` excluídos da compilação Kotlin do beta Android.

## Validação

- [x] `.\gradlew.bat testDebugUnitTest`
- [x] `.\gradlew.bat lintDebug`
- [x] `.\gradlew.bat assembleRelease`
- Publicar no Firebase App Distribution após os gates.

## Notas técnicas

- Google Mobile Ads integrado com `play-services-ads:23.6.0`, mantendo compatibilidade com Kotlin `1.9.22`/Compose compiler `1.5.9` do app.
- IDs AdMob ficam configuráveis por `FYPMATCH_ADMOB_APP_ID` e `FYPMATCH_ADMOB_REWARDED_AD_UNIT_ID`; sem override, o beta usa IDs oficiais de teste.
- `assembleRelease` executou `uploadCrashlyticsMappingFileRelease`.

## Release notes

- Corrige curtidas recebidas/enviadas e matches reais para duas contas.
- Corrige ações no detalhe de usuário para curtir/pass/super like a pessoa correta.
- Evita conversas duplicadas e mantém chat autorizado por `participantIds`.
- Integra anúncio recompensado real com AdMob em modo teste.
- Melhora exibição de erros longos no chat.
- Mantém a Conselheira IA rotulada como experiência beta/local.

## File List

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/ideiassertiva/FypMatch/FypMatchApplication.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/AdsRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/AICounselorRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/DiscoveryRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/FirebaseChatRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/RewardedAdGateway.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/di/AppModule.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/navigation/FypMatchNavigation.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/AdsScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/AICounselorScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/EnhancedChatScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/LikesScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/UserDetailsScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/util/ContextExt.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/AdsViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/AICounselorViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/LikesViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/UserDetailsViewModel.kt`
- `app/src/test/java/com/ideiassertiva/FypMatch/repository/AdsRepositoryTest.kt`
- `app/src/test/java/com/ideiassertiva/FypMatch/repository/FirebaseChatRepositoryContractTest.kt`
