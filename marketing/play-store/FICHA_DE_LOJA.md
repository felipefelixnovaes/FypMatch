# Ficha de Loja — Google Play Console

> Rascunho de copy para a seção "Store listing" do Play Console. Ajustar tom conforme decisão de GTM (lançamento aberto vs. convite-only) antes de publicar.

## Assets gerados

- `icon_512.png` — ícone 512×512, full bleed, composto a partir do gradiente e coração da marca (`app/src/main/res/drawable/ic_launcher_background.xml` + `ic_brand_heart_white.png`). Pronto para upload.
- `feature_graphic_1024x500.png` — banner da ficha de loja. Pronto para upload.
- `gen_feature.py` — script usado para gerar o banner (útil se quiser trocar o texto/tagline depois).

## ⚠️ Screenshots — pendente, precisa de captura real

Não gerei screenshots porque a Play Store exige capturas reais da interface do app (mockups fabricados sem rodar o app violariam as diretrizes de conteúdo enganoso da própria Play Store). O projeto já tem uma `ScreenshotCatalogActivity.kt` em `app/src/debug/java/.../ScreenshotCatalogActivity.kt` — provavelmente criada exatamente para gerar screenshots de loja a partir de dados de preview. Sugestão de próximo passo:

1. Rodar o app (debug) no emulador/dispositivo em resolução de referência (ex.: Pixel 6, 1080×2400)
2. Usar a `ScreenshotCatalogActivity` (ou navegar manualmente) para capturar: tela de descoberta/swipe, chat, perfil, mapa de compatibilidade, Central de Segurança
3. Mínimo 2 screenshots, recomendado 4–8, formato PNG/JPEG, 16:9 ou 9:16
4. Posso ajudar a tirar as capturas via preview_start + browser tools se você quiser rodar o app agora

---

## Descrição curta (máx. 80 caracteres)

```
Conexões reais: matches por compatibilidade, chat seguro, perfis verificados
```
(78 caracteres)

## Descrição longa (máx. 4000 caracteres)

```
FypMatch é um app de relacionamento feito para quem quer conexões de verdade — não só um match a mais.

💜 COMPATIBILIDADE DE VERDADE
Nosso algoritmo cruza interesses, valores, intenção de relacionamento e estilo de vida para sugerir pessoas com quem você realmente combina — não só rostos aleatórios.

🔒 SEGURANÇA EM PRIMEIRO LUGAR
- Verificação de perfil por selfie, conferida manualmente pela nossa equipe
- Central de Segurança com bloqueio, denúncia e orientações para encontros seguros
- Exclusão de conta auditável, sob seu controle

💬 CHAT COM PROPÓSITO
Converse com seus matches em um ambiente pensado para reduzir spam e perfis falsos.

🧭 CONSELHEIRO DE IA
Precisa de uma opinião sobre a conversa ou o encontro? O Conselheiro do FypMatch te dá orientação personalizada, com privacidade: seus dados pessoais nunca são enviados à IA sem passar por um filtro de proteção.

🗺️ MAPA DE CONEXÃO
Descubra visualmente sua compatibilidade com cada match, além do "match ou não match" simples.

FypMatch é exclusivo para maiores de 18 anos.

Baixe agora e comece a construir conexões que fazem sentido.
```

⚠️ *Revisar antes de publicar: confirmar se todas as features citadas (Conselheiro de IA, Mapa de Conexão) já estão habilitadas na versão que será submetida — não anunciar recurso que ainda está atrás de flag/beta.*

## Categoria sugerida no Play Console
Estilo de vida > Namoro (Dating)

## Classificação de conteúdo
18+ (Mature/Dating content) — usar o Content Rating questionnaire do Play Console; app já reforça idade mínima no cadastro.
