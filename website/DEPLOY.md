# 🚀 Guia de Deploy - FypMatch Website

## Pré-requisitos

### 1. Instalar Firebase CLI
```bash
npm install -g firebase-tools
```

### 2. Login no Firebase
```bash
firebase login
```

### 3. Configurar Projeto
```bash
# Navegue para a pasta website
cd website

# Inicialize o Firebase (se não feito)
firebase init hosting
```

## 📋 Checklist Pré-Deploy

### Arquivos Obrigatórios
- ✅ `index.html` - Página principal
- ✅ `app-ads.txt` - Verificação AdMob
- ✅ `privacy.html` - Política de privacidade
- ✅ `terms.html` - Termos de uso
- ✅ `robots.txt` - SEO
- ✅ `sitemap.xml` - Mapa do site
- ✅ `firebase.json` - Configuração hosting

### Imagens Necessárias
- ✅ `images/logo.png` - Logo oficial
- ⚠️ `images/app-screenshot-1.png` - Screenshot do app
- ⚠️ `images/app-screenshots-multiple.png` - Múltiplos screenshots
- ⚠️ `images/og-image.jpg` - Imagem Open Graph
- ⚠️ `images/google-play-badge.png` - Badge Google Play
- ⚠️ `images/app-store-badge.png` - Badge App Store

## 🔧 Deploy para Produção

### Passo 1: Teste Local
```bash
# Teste local
firebase serve --only hosting
```
Acesse: http://localhost:5000

### Passo 2: Deploy
```bash
# Deploy para produção
firebase deploy --only hosting
```

### Passo 3: Configurar Domínio
1. No Firebase Console → Hosting
2. Clique em "Add custom domain"
3. Digite: `www.fypmatch.com`
4. Siga as instruções DNS
5. Aguarde verificação SSL

## 🔍 Verificações Pós-Deploy

### 1. Teste app-ads.txt
```bash
curl https://www.fypmatch.com/app-ads.txt
```
Deve retornar:
```
google.com, pub-9657321458227740, DIRECT, f08c47fec0942fa0
```

### 2. Teste SEO
- ✅ Meta tags carregando
- ✅ Open Graph funcionando
- ✅ Sitemap acessível
- ✅ Robots.txt válido

### 3. Teste Performance
- ✅ Página carrega < 3s
- ✅ Mobile friendly
- ✅ SSL ativo
- ✅ CDN funcionando

### 4. Teste Funcionalidades
- ✅ Navegação mobile
- ✅ Formulário de contato
- ✅ Links de download
- ✅ Botões interativos

## 📱 Configuração Google Play Store

### Passo 1: Adicionar Website
1. Google Play Console
2. Configurações → Dados do desenvolvedor
3. Website: `https://www.fypmatch.com`
4. Salvar

### Passo 2: Verificar AdMob
1. AdMob Console
2. Apps → Seu app
3. Configurações → Verificação
4. Verificar app-ads.txt

## 🎯 Testes de Verificação

### Google Play Store
```bash
# Teste se crawler consegue acessar
curl -A "Googlebot" https://www.fypmatch.com/
curl -A "Googlebot" https://www.fypmatch.com/app-ads.txt
```

### AdMob Publisher
```bash
# Teste verificação AdMob
curl https://www.fypmatch.com/app-ads.txt
```

## 🛠️ Troubleshooting

### Problema: app-ads.txt não encontrado
```bash
# Verificar se arquivo existe
ls -la website/app-ads.txt

# Verificar configuração Firebase
cat website/firebase.json
```

### Problema: Domínio não verifica
1. Verificar registros DNS
2. Aguardar propagação (até 24h)
3. Verificar configuração Firebase

### Problema: SSL não ativo
1. Aguardar verificação domínio
2. Verificar registros DNS
3. Contatar suporte Firebase

## 📊 Monitoramento

### Analytics
- Google Analytics configurado
- Eventos de conversão
- Tracking de downloads

### Performance
- Core Web Vitals
- Lighthouse score
- GTmetrix reports

### SEO
- Google Search Console
- Posicionamento keywords
- Crawl errors

## 🔄 Atualizações Futuras

### Imagens Obrigatórias
1. Tirar screenshots do app
2. Criar imagem Open Graph
3. Baixar badges oficiais das stores
4. Otimizar imagens para web

### Melhorias Opcionais
- Adicionar testimonials
- Criar página de blog
- Integrar chat online
- A/B testing

## 📞 Suporte

### Contatos
- **Desenvolvedor**: Ideias Assertiva
- **Email**: contato@fypmatch.com
- **Suporte**: suporte@fypmatch.com

### Recursos
- [Firebase Hosting Docs](https://firebase.google.com/docs/hosting)
- [AdMob app-ads.txt](https://support.google.com/admob/answer/9363762)
- [Google Play Developer](https://support.google.com/googleplay/android-developer)

---

**Status**: ✅ Pronto para deploy  
**Última atualização**: Janeiro 2025 