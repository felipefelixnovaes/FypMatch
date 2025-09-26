# FypMatch iOS

Este diretório contém o aplicativo iOS nativo do FypMatch desenvolvido em SwiftUI.

## Estrutura Planejada

```
ios/
├── FypMatch.xcodeproj          # Projeto Xcode
├── FypMatch/
│   ├── App/
│   │   ├── FypMatchApp.swift   # App principal
│   │   └── ContentView.swift   # View root
│   ├── Models/
│   │   ├── User.swift
│   │   ├── Match.swift
│   │   ├── Message.swift
│   │   └── Conversation.swift
│   ├── Views/
│   │   ├── Authentication/
│   │   ├── Discovery/
│   │   ├── Chat/
│   │   ├── Profile/
│   │   └── Components/
│   ├── ViewModels/
│   │   ├── AuthViewModel.swift
│   │   ├── DiscoveryViewModel.swift
│   │   ├── ChatViewModel.swift
│   │   └── ProfileViewModel.swift
│   ├── Services/
│   │   ├── FirebaseService.swift
│   │   ├── HealthKitService.swift
│   │   ├── NotificationService.swift
│   │   └── NetworkService.swift
│   └── Resources/
│       ├── Assets.xcassets
│       ├── Info.plist
│       └── GoogleService-Info.plist
└── Tests/
    ├── FypMatchTests/
    └── FypMatchUITests/
```

## Tecnologias

- **SwiftUI** - Interface nativa iOS
- **Combine** - Reactive programming
- **Firebase SDK** - Backend integration
- **HealthKit** - Health data integration
- **UserNotifications** - Push notifications
- **Core Data** - Local caching

## Próximos Passos

1. [ ] Configurar projeto Xcode
2. [ ] Implementar autenticação Firebase
3. [ ] Portar componentes principais do Android
4. [ ] Implementar recursos específicos do iOS
5. [ ] Testes e otimização
6. [ ] Submissão para App Store