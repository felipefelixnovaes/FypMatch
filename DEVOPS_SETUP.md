# DevOps Setup — FypMatch

## Status da Seguranca

| Item | Antes | Depois |
|------|-------|--------|
| google-services.json | Exposto no repo | Gitignored, injetado via secret |
| Keystore | Inexistente | Script de geracao + GitHub Secrets |
| CI/CD | Zero | GitHub Actions com lint/test/build |
| ProGuard | Vazio | Regras configuradas |
| Signing Config | Inexistente | Placeholders para secrets |

## Como comecar

1. Execute `scripts/setup-firebase-config.bat` (Windows) ou `.sh` (Linux/Mac)
2. Execute `scripts/ci-check.bat` para validar localmente
3. Configure os GitHub Secrets conforme `.github/README-CI.md`
4. Push para main para acionar CI

## Comandos uteis

```bash
# CI local
./scripts/ci-check.sh        # Linux/Mac
scripts/ci-check.bat         # Windows

# Firebase config
./scripts/setup-firebase-config.sh
scripts/setup-firebase-config.bat
```

## Pipeline
- **Push/PR**: lint + unit tests + assembleDebug
- **Tag v***: assembleRelease + sign + upload artifact