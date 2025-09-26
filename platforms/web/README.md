# FypMatch Web Application

Este diretório contém a aplicação web PWA do FypMatch desenvolvida em React/Next.js.

## Estrutura Planejada

```
web/
├── package.json
├── next.config.js
├── public/
│   ├── manifest.json
│   ├── sw.js                   # Service Worker
│   └── icons/
├── src/
│   ├── pages/
│   │   ├── index.tsx           # Landing page
│   │   ├── _app.tsx
│   │   ├── _document.tsx
│   │   └── app/
│   │       ├── discovery.tsx
│   │       ├── matches.tsx
│   │       ├── chat.tsx
│   │       └── profile.tsx
│   ├── components/
│   │   ├── Auth/
│   │   ├── SwipeCard/
│   │   ├── Chat/
│   │   ├── Profile/
│   │   └── Common/
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useSwipe.ts
│   │   ├── useChat.ts
│   │   └── useFirebase.ts
│   ├── services/
│   │   ├── firebase.ts
│   │   ├── api.ts
│   │   ├── pwa.ts
│   │   └── storage.ts
│   ├── store/
│   │   ├── slices/
│   │   └── index.ts
│   ├── styles/
│   │   ├── globals.css
│   │   └── components/
│   └── types/
│       ├── user.ts
│       ├── match.ts
│       └── chat.ts
└── docs/
    └── api.md
```

## Stack Tecnológico

- **Next.js 13** - React framework com App Router
- **TypeScript** - Type safety
- **Material-UI v5** - Component library
- **Redux Toolkit** - State management
- **Firebase Web SDK** - Backend integration
- **Workbox** - PWA capabilities
- **Framer Motion** - Animations

## Recursos PWA

- 📱 **Instalável** - Add to Home Screen
- 🔔 **Push Notifications** - Web Push API
- 🔄 **Offline Support** - Service Worker caching
- 🚀 **Fast Loading** - Code splitting e lazy loading

## Próximos Passos

1. [ ] Configurar projeto Next.js
2. [ ] Implementar autenticação web
3. [ ] Criar componentes de UI responsivos
4. [ ] Implementar PWA capabilities
5. [ ] Otimizar performance
6. [ ] Deploy e CDN setup