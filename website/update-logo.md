# 🎨 Atualização da Logo FypMatch

## Nova Logo
- **Design:** Coração vermelho com check branco + texto "FypMatch"
- **Cores:** "Fyp" em vermelho (#E91E63), "Match" em roxo (#9C27B0)
- **Estilo:** Moderno, limpo, profissional

## Instruções para Substituição

### 1. Upload da Nova Logo
1. Salve a imagem fornecida como `logo-fypmatch.png`
2. Redimensione para 400x120px (mantendo proporção)
3. Formato PNG com fundo transparente
4. Substitua o arquivo `website/images/logo.png`

### 2. Arquivos que Referenciam a Logo

**HTML Files:**
- `index.html` (linha 28, 123, 620)
- `privacy.html` (linha 15, 167)
- `terms.html` (linha 15, 224)
- `cookies.html` (linha 15, 234)

**Config Files:**
- `manifest.json` (linhas 14, 20, 42, 54)

### 3. Dimensões por Contexto

**Navbar:** 40x12px (height: 40px)
**Footer:** 120x36px (height: 120px)
**PWA Icons:** 192x192px, 512x512px (quadrado)

### 4. Deploy
```bash
firebase deploy --only hosting
```

## Próximos Passos
1. Substitua manualmente o arquivo `images/logo.png`
2. Execute: `firebase deploy --only hosting`
3. Teste em https://fypmatch-8ac3c.web.app

---

**A nova logo vai dar uma identidade visual muito mais profissional ao FypMatch!** 🚀 