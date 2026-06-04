# FypMatch — Guia de Release (Google Play)

## Artefatos de build

| Artefato | Caminho | Uso |
|----------|---------|-----|
| **AAB** (App Bundle) | `app/build/outputs/bundle/release/app-release.aab` | Upload no Google Play Console |
| **APK** (release) | `app/build/outputs/apk/release/app-release.apk` | Teste em device físico |

Gerar novamente:
```bash
./gradlew bundleRelease    # AAB para o Play
./gradlew assembleRelease  # APK para teste
```

## Assinatura (keystore de upload)

- Keystore: `fypmatch-release.jks` (raiz do projeto, **NÃO versionado**)
- Credenciais: `keystore.properties` (**NÃO versionado**)
- Alias: `fypmatch-upload`
- Validade: 10.000 dias

> ⚠️ **CRÍTICO:** faça backup do `fypmatch-release.jks` e da senha em local seguro.
> Se perder, **não será possível atualizar o app** no Google Play (apenas publicar
> um novo app com outro applicationId). Considere ativar o **Play App Signing**.

### Fingerprints do certificado de upload

```
SHA-1:   A7:80:A4:9C:D2:8A:D0:B5:6E:17:7B:9D:D1:6C:7C:FB:28:2C:FB:B5
SHA-256: DB:9E:89:6D:00:D3:78:65:4C:7E:B0:68:31:07:42:88:20:84:80:20:89:0A:E5:42:5F:4C:0C:62:71:ED:21:23
```

## ⚠️ Pendências OBRIGATÓRIAS antes de produção real

O `app/google-services.json` atual é **placeholder** (valores dummy). O app compila
mas Firebase (Auth, Firestore, FCM, Storage, Google Sign-In) **não funciona** até:

1. **Criar projeto Firebase real** (https://console.firebase.google.com) — plano Spark serve para começar
2. **Registrar app Android** com package `com.ideiassertiva.FypMatch`
3. **Adicionar os SHA-1** (upload + Play App Signing) no Firebase Console
4. **Baixar o `google-services.json` real** e substituir o placeholder em `app/`
5. **Configurar o Web Client ID real** do Google Sign-In em `AuthRepository.kt`
   (constante `GOOGLE_WEB_CLIENT_ID`, hoje placeholder)
6. **Habilitar serviços** no Firebase: Authentication (Google), Firestore, Storage, Cloud Messaging
7. **Publicar regras de segurança** do Firestore e Storage (produção)

## Checklist Google Play Console

- [ ] Conta de desenvolvedor Google Play (taxa única US$ 25)
- [ ] Criar app no Play Console (package `com.ideiassertiva.FypMatch`)
- [ ] Ativar **Play App Signing** (recomendado)
- [ ] Upload do `app-release.aab`
- [ ] Ficha da loja: título, descrição, ícone 512×512, feature graphic 1024×500, screenshots
- [ ] Classificação de conteúdo (questionário)
- [ ] Política de privacidade (URL pública obrigatória)
- [ ] Declaração de segurança de dados (Data Safety)
- [ ] Público-alvo e conteúdo (app de namoro → 18+)
- [ ] Testes internos → fechados → produção
