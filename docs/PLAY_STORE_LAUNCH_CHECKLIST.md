# Checklist de Lançamento — Google Play Store (FypMatch)

> Gerado em 2026-07-26 a partir de auditoria do repositório (docs/CURRENT_STATUS.md, docs/AI_HANDOFF.md, build.gradle.kts, keystore.properties, modelos de dados). Ver também [POLITICA_DE_PRIVACIDADE.md](legal/POLITICA_DE_PRIVACIDADE.md).

**Status atual:** beta fechado via Firebase App Distribution (v1.0.48 / build 53). Nunca foi submetido ao Google Play Console.

---

## 🔴 Bloqueadores críticos (impedem submissão)

- [ ] **Política de privacidade pública** — rascunho pronto em [`docs/legal/POLITICA_DE_PRIVACIDADE.md`](legal/POLITICA_DE_PRIVACIDADE.md), falta: revisão jurídica, preencher DPO/CNPJ, publicar em URL pública estável, linkar no app
- [ ] **Conta de desenvolvedor Google Play** — criar (taxa única US$ 25)
- [ ] **Google Play Billing real** — hoje a loja de assinaturas/créditos é só UI, sem cobrança nem verificação de compra (`docs/CURRENT_STATUS.md`). Vender assinatura sem o billing oficial do Play viola a política de pagamentos deles. Bloqueador se o app for publicado com preços visíveis e sem cobrança funcional.
- [ ] **Data Safety form** no Play Console — preencher usando a política de privacidade como base (dados sensíveis: orientação sexual, geolocalização, biometria/selfie, religião, saúde)
- [ ] **Content Rating questionnaire** + declaração de público 18+

## 🟡 Necessário antes da submissão

- [ ] Criar o app no Play Console + configurar Play App Signing
- [ ] Assets de loja: ícone 512×512, feature graphic 1024×500, screenshots (mín. 2, recomendado 4-8) — nada disso existe no repo hoje
- [ ] Descrição curta/longa da ficha de loja (pt-BR, considerar en-US se for expandir)
- [ ] Confirmar texto de consentimento no onboarding cobre cada categoria de dado sensível separadamente (orientação sexual, geolocalização, biometria, religião) — ver seção 2.2 da política
- [ ] Definir e documentar prazo real de exclusão de dados após remoção de conta (inclusive backups e mensagens do outro lado da conversa)
- [ ] QA manual completo end-to-end antes de gerar a release candidate (checklist já existe em `docs/release-runbook.md`, mas é focado em beta/App Distribution — adaptar para release de produção)
- [ ] Rodar `testDebugUnitTest`, `lintDebug`, `assembleRelease` limpos (havia logs de build quebrado no repo — `compile_error*.log` — confirmar que não refletem estado atual)
- [ ] Organizar o worktree: `docs/AI_HANDOFF.md` reporta mudanças não commitadas e arquivos `.orig`/`.patch`/`.rej` soltos

## 🟢 Decisão de produto/negócio (não é código, mas trava o lançamento)

- [ ] **Definir se o lançamento será aberto (Play Store pública) ou continua convite-only** — hoje há sistema de código de acesso antecipado ativo (`CODIGOS_ACESSO_ANTECIPADO.md`) e estratégia de GTM por cidade/waitlist (`marketing/lancamento_estrategia.md`). Isso é incompatível por padrão com publicação aberta global do Play, a menos que se use listagem "unlisted" + distribuição fechada, ou release faseado por país/região configurado manualmente no Console.
- [ ] **Realinhar prioridade da equipe** — foco atual documentado é iOS/SwiftUI (`docs/AI_HANDOFF.md`); publicar no Android/Play não é a tarefa ativa. Precisa de decisão explícita para redirecionar esforço.
- [ ] Confirmar se o app será lançado com monetização ativa (billing real) ou como MVP gratuito primeiro, e ajustar prioridade técnica de acordo

## ✅ Já pronto

- [x] Keystore de assinatura de release configurado (`keystore.properties` + `app/build.gradle.kts`)
- [x] Firebase de produção real configurado (`google-services.json`, projeto `fypmatch-8ac3c`)
- [x] targetSdk 35 — em conformidade com a política atual do Google Play
- [x] Pipeline de build com ProGuard/R8, Crashlytics, CI básico (GitHub Actions)
- [x] Recursos de segurança relevantes para app de relacionamento: Central de Segurança, exclusão de conta auditável, trava de idade mínima 18 anos
- [x] Minimização de dados no recurso de IA (Conselheiro) — pseudonimização e redação de PII antes de enviar a provedores externos (`docs/adr/2026-06-13-ai-privacy-boundary.md`)

---

## Ordem recomendada de execução

1. Decisão de negócio: aberto vs. convite-only (define se os itens de billing/Play Console são urgentes agora ou depois)
2. Política de privacidade → revisão jurídica → publicar URL
3. Conta de desenvolvedor Google Play + criação do app no Console
4. Data Safety form + Content Rating (usam a política como insumo)
5. Assets de loja (ícone, screenshots, feature graphic)
6. Billing real (se monetização estiver no escopo do lançamento inicial)
7. QA manual completo + build de release limpo
8. Submissão para teste interno/fechado no Play Console → produção
