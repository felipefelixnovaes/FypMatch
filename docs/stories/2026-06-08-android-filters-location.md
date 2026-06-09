# Story: Filtros e localização Android beta

## Objetivo

Completar duas lacunas do beta Android: filtros reais no Discovery e escolha de cidade com lista, localização atual e modo viagem.

## Escopo

- Expor a tela de filtros dentro do menu/configurações do usuário, preservando o topo do Discovery com os dois acessos da esquerda.
- Aplicar filtros reais ao deck de cards: idade, distância, gênero, intenção, verificação, atividade recente, fotos, altura, fumo, bebida, filhos e religião.
- Persistir filtros em `users/{userId}.preferences`.
- Calcular distância por cidade/coordenadas em vez de usar valor aleatório.
- Permitir selecionar cidade digitando e escolhendo em uma lista local.
- Permitir usar localização atual via permissão Android.
- Permitir modo viagem com destino diferente da localização atual.

## Checklist

- [x] `SearchFilters` convertido para/de `UserPreferences`.
- [x] Versão Android avançada para `1.0.17 (22)`.
- [x] `DiscoveryRepository` aplica filtros antes de montar cards.
- [x] Distância do card usa cidade/coordenadas resolvidas.
- [x] Tela de filtros acessível por Configurações > Filtros de descoberta.
- [x] Discovery preserva apenas os dois botões da esquerda: editar perfil e menu/configurações.
- [x] Sliders de idade e altura alteram mínimo e máximo.
- [x] Filtros persistem no Firestore ao aplicar.
- [x] Perfil permite escolher cidade por lista ou localização atual.
- [x] Modo viagem usa destino escolhido como âncora da busca.
- [x] Mocks `*_v2` continuam excluídos da compilação Kotlin.

## Validação

- [x] `.\gradlew.bat testDebugUnitTest`
- [x] `.\gradlew.bat lintDebug`
- [x] `.\gradlew.bat assembleRelease`
- [x] Firebase App Distribution `1.0.17 (22)` para testers.

Release tester: https://appdistribution.firebase.google.com/testerapps/1:98859676437:android:b01d8961659b5f3125af1c/releases/6t1ups4g3dvk0

## Release notes

- Move filtros para o menu/configurações do usuário, sem adicionar botão extra no Discovery.
- Faz filtros realmente alterarem os cards exibidos.
- Adiciona seletor de cidade com busca, lista e localização atual.
- Adiciona modo viagem para buscar perfis em outra cidade.
- Corrige distância exibida no card para usar localização resolvida.

## File List

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/BrazilLocationCatalog.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/DeviceLocationResolver.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/DiscoveryFilters.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/data/repository/DiscoveryRepository.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/model/SearchFilters.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/model/User.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/components/LocationPickerField.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/navigation/FypMatchNavigation.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/AdvancedFiltersScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/DiscoveryScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/ProfileEditScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/screens/ProfileScreen.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/FiltersViewModel.kt`
- `app/src/main/java/com/ideiassertiva/FypMatch/ui/viewmodel/ProfileViewModel.kt`
- `app/src/test/java/com/ideiassertiva/FypMatch/data/DiscoveryFiltersTest.kt`
- `app/src/test/java/com/ideiassertiva/FypMatch/model/UserPreferencesFirestoreTest.kt`
