# Relatório de Testes de UI/UX — FypMatch

**Data:** 2026-07-27
**Ambiente:** Emulador Android (Pixel API 34), build debug, Firebase de produção
**Método:** Instalação real do app via `adb`, navegação manual por toque sintético (`adb shell input`), captura de tela em cada etapa, leitura de `logcat` para diagnosticar causas raiz
**Contas de teste usadas:**
- `teste.uiux.claude001@fypmatch-test.qa` (Mulher)
- `teste.uiux.claude002@fypmatch-test.qa` (Não-binário/Gay)

> Screenshots referenciados estão em [`docs/qa/screenshots/`](screenshots/).

---

## Resumo executivo

Nesta rodada de testes, o app foi instalado e executado de verdade (não apenas revisão de código). Foram encontrados e corrigidos **6 bugs confirmados** (crash de abertura, acentuação, layout, contraste, dados de perfil, item duplicado), e foi identificado **1 bug de performance sério ainda não corrigido** (travamento de main thread na tela de Editar Perfil), que impediu a conclusão do teste de match/chat entre duas contas nesta sessão.

| # | Problema | Severidade | Status |
|---|---|---|---|
| 1 | Crash no primeiro launch (fonte bloqueante) | Crítico (P0) | ✅ Corrigido e verificado |
| 2 | Acentuação ausente no tutorial de onboarding | Médio | ✅ Corrigido e verificado |
| 3 | Layout com vão vazio na tela de boas-vindas | Médio | ✅ Corrigido e verificado |
| 4 | Contraste (texto invisível) no estado vazio da descoberta | Alto (acessibilidade) | ✅ Corrigido e verificado |
| 5 | Perfil aparecia em branco após cadastro | Alto (UX crítica) | ✅ Corrigido e verificado |
| 6 | "Central de ajuda" duplicada no menu | Baixo | ✅ Corrigido |
| 7 | **Tela de Editar Perfil trava (main thread) após uso dos dropdowns** | **Alto (bloqueia conclusão de perfil)** | ⚠️ **Identificado, não corrigido** |
| 8 | Tema visual não persiste entre reinícios do app | Médio | ⚠️ Identificado, não investigado |
| 9 | Dropdowns de Gênero/Orientação exigem toque duplo pra abrir | Baixo/Médio | ⚠️ Identificado, não investigado |

---

## 1. Crash no primeiro launch — CORRIGIDO ✅

**Sintoma:** app fechava sozinho ("FypMatch parou") imediatamente após o splash, em 100% das tentativas no emulador.

![Crash antes da correção](screenshots/01_crash_antes_da_correcao.png)

**Causa raiz:** toda a tipografia do app dependia de uma única fonte (`Plus Jakarta Sans`) carregada via `Font(R.font.*)` bloqueante, através do Google Play Services Fonts Provider. Sem esse provider disponível (emulador sem Play Store completo, ou qualquer device sem GMS/rede na primeira carga), a chamada lançava `Resources.NotFoundException` e derrubava o app inteiro, pois **toda tela usa `Typography`**.

**Correção:** migração para a API assíncrona (`androidx.compose.ui.text.googlefonts`), que já estava nas dependências do projeto mas não era usada. Ela degrada com segurança para a fonte padrão do sistema em vez de lançar exceção.

**Arquivo:** `app/src/main/java/.../ui/theme/Type.kt`

![Fonte e acentos corrigidos](screenshots/02_fonte_e_acentos_corrigidos.png)

---

## 2. Acentuação ausente no tutorial de onboarding — CORRIGIDO ✅

Dezenas de strings sem acento em todo o tutorial (9 telas): "intencao", "Nao", "Proximo", "Configuracoes", "verificacao", "confianca", "questionarios", etc. Reescrito o arquivo completo de conteúdo do tutorial e mais duas strings na tela de onboarding.

**Arquivos:** `model/OnboardingTutorial.kt`, `ui/screens/OnboardingScreen.kt`, `ui/screens/SettingsScreen.kt`

---

## 3. Layout com vão vazio na tela de boas-vindas — CORRIGIDO ✅

Um `Spacer(Modifier.weight(1f))` mal posicionado empurrava todo o espaço livre para um único vão enorme entre o carrossel e os botões de CTA, em telas altas (2400px).

**Correção:** carrossel + indicadores agora ficam centralizados dentro do espaço disponível (`Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center)`), distribuindo o espaço de forma equilibrada.

![Layout do welcome corrigido](screenshots/03_welcome_layout_corrigido.png)

**Arquivo:** `ui/screens/WelcomeScreen.kt`

---

## 4. Contraste — texto invisível no estado vazio da descoberta — CORRIGIDO ✅

O título "Não há mais pessoas por aqui!" não definia `color` explícita e herdava o padrão preto do Compose (`LocalContentColor` default), já que a tela roda direto num `Box` sem `Surface` por baixo. Ficava ilegível no tema escuro.

**Correção:** `color = MaterialTheme.colorScheme.onSurface` explícito.

![Contraste do discovery corrigido](screenshots/04_discovery_contraste_corrigido.png)

**Arquivo:** `ui/screens/DiscoveryScreen.kt`

---

## 5. Perfil aparecia em branco após cadastro — CORRIGIDO E VALIDADO ✅

**Sintoma:** todo usuário novo, ao abrir "Editar Perfil" logo após criar conta, via nome, idade e gênero em branco — mesmo tendo acabado de preencher esses dados no cadastro.

**Causa raiz:** `RegisterViewModel.register()` gravava um mapa solto no Firestore (`displayName`, `age`, `gender` soltos), sem o campo `profile` aninhado que `UserProfile`/`ProfileEditViewModel` esperam. `loadCurrentUser()` funcionava perfeitamente — o problema era só a gravação.

**Correção:** o cadastro agora grava um objeto `User` completo com `profile = UserProfile(fullName, age, gender)`.

**Validação:** criei uma segunda conta de teste (`claude002`) e confirmei visualmente que "Nome completo" e "Idade" vêm preenchidos corretamente na primeira abertura do Editar Perfil.

![Perfil correto pós-cadastro](screenshots/06_perfil_dados_pos_cadastro_ok.png)

**Arquivo:** `ui/viewmodel/RegisterViewModel.kt`

---

## 6. "Central de ajuda" duplicada — CORRIGIDO ✅

Um `ListItem` sem `onClick` (código morto) duplicava visualmente a entrada "Central de ajuda" logo acima do card funcional que realmente abre o link.

![Duplicidade antes da correção](screenshots/10_central_de_ajuda_duplicada_antes.png)

**Arquivo:** `ui/screens/SettingsScreen.kt`

---

## 7. ⚠️ Tela de Editar Perfil trava (main thread) — NÃO CORRIGIDO

Este é o achado mais sério da sessão e **bloqueou o teste de match/chat entre as duas contas**.

**Sintoma observado:**
1. Ao interagir repetidamente com os dropdowns de "Gênero" e "Orientação" na tela de Editar Perfil, cada dropdown passou a exigir um toque "de despertar" antes de abrir de verdade (primeiro toque reabre o dropdown anterior; segundo toque funciona).
2. Após algumas interações, a tela **parou de responder a qualquer toque** — inclusive o botão "Salvar" no topo (que não tem relação nenhuma com os dropdowns) parou de reagir.

![Tela travada, Salvar não responde](screenshots/08_tela_travada_salvar_nao_responde.png)

**Diagnóstico:** confirmei via `adb logcat` que não é artefato do emulador/adb — há evidência real de sobrecarga do main thread:

```
Choreographer: Skipped 338 frames!  The application may be doing too much work on its main thread.
Choreographer: Skipped 441 frames!  The application may be doing too much work on its main thread.
```

Frames pulados nessa magnitude (300-440 frames ≈ 5-7 segundos de bloqueio) tornam a tela genuinamente não-responsiva, sem crash e sem diálogo de ANR visível. Um `force-stop` + reabertura do app resolve completamente (confirma que não é corrupção permanente, e sim acúmulo de trabalho síncrono no main thread ao longo da sessão).

![App recuperado após restart](screenshots/09_apos_restart_app_recuperado.png)

**Impacto:** se isso acontecer com um usuário real completando o perfil (que é obrigatório para aparecer na Descoberta — ver seção "Perfil completo" abaixo), ele fica preso na tela sem conseguir salvar, sem entender o motivo, e pode sair do app frustrado.

**Não investiguei a causa exata** (não tive tempo de fazer profiling/CPU trace nesta sessão), mas as pistas mais prováveis para a próxima investigação:
- Recomposições excessivas disparadas pelos `DropdownMenuField` de Gênero/Orientação/Intenção (os três juntos, cada um com `remember { mutableStateOf(false) }` próprio — conferir se algo força recomposição do formulário inteiro a cada mudança de estado)
- Alguma leitura/gravação síncrona no Firestore ou no `SharedPreferences` disparada a cada seleção de dropdown, rodando na main thread
- Vazamento de `LaunchedEffect`/coroutine acumulando trabalho a cada abertura de dropdown

**Recomendação:** rodar profiler (Android Studio Profiler / `systrace`) especificamente na tela `ProfileEditScreen` interagindo repetidamente com os três dropdowns, antes de tentar corrigir às cegas.

---

## 8. ⚠️ Tema não persiste entre reinícios do app

Notei, sem buscar ativamente, que o tema do app trocou de "Noturno" (configurado antes) para "Claro" sozinho após um restart do processo. Não investiguei a fundo (pode ser um bug real de persistência de preferência, ou comportamento intencional ligado ao tema do sistema/emulador). Vale uma investigação futura dedicada.

![Tema claro inesperado](screenshots/05_tema_claro_inesperado.png)

---

## 9. Regra de "perfil completo" — não é bug, mas trava testes

Descoberto ao investigar por que as duas contas de teste não apareciam uma para a outra na Descoberta: `UserProfile.hasRequiredProfileFields()` exige `fullName`, `age >= 18`, `bio`/`aboutMe`, `city`, `gender`, `orientation` e `intention` todos preenchidos. Isso é uma regra de produto correta (não mostrar perfis incompletos), mas combinada com o bug #7 (trava ao preencher orientação/intenção), ficou difícil completar um perfil de teste do zero via UI nesta sessão.

**Consequência prática:** não consegui validar match + chat entre as duas contas nesta rodada. Fica pendente para a próxima sessão, idealmente só depois do bug #7 ser corrigido (ou usando um workaround: gravar os campos de perfil direto no Firestore Console para os testes, contornando a UI).

---

## Pendências para a próxima rodada

1. **Prioridade alta:** investigar e corrigir o travamento de main thread na `ProfileEditScreen` (bug #7) — é o que mais bloqueia testes adicionais e provavelmente afeta usuários reais.
2. Investigar a persistência do tema (bug #8).
3. Investigar por que os dropdowns de Gênero/Orientação exigem toque duplo (bug #9) — pode ser sintoma do mesmo problema de performance do bug #7, ou uma causa relacionada.
4. Depois de resolver o acima: retomar teste de match + chat entre duas contas, Loja, Central de Segurança e Verificação de foto (não testados nesta sessão).
