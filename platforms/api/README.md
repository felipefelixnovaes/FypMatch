# FypMatch Public API

Este diretório contém a API pública RESTful do FypMatch para integrações externas.

## Estrutura Planejada

```
api/
├── package.json
├── tsconfig.json
├── Dockerfile
├── src/
│   ├── app.ts                  # Express app
│   ├── server.ts               # Server setup
│   ├── routes/
│   │   ├── auth.ts
│   │   ├── users.ts
│   │   ├── discovery.ts
│   │   ├── matches.ts
│   │   ├── chat.ts
│   │   ├── affiliates.ts
│   │   └── webhooks.ts
│   ├── controllers/
│   │   ├── AuthController.ts
│   │   ├── UserController.ts
│   │   ├── DiscoveryController.ts
│   │   ├── MatchController.ts
│   │   ├── ChatController.ts
│   │   └── AffiliateController.ts
│   ├── middleware/
│   │   ├── auth.ts
│   │   ├── rateLimit.ts
│   │   ├── validation.ts
│   │   └── logging.ts
│   ├── services/
│   │   ├── firebase.ts
│   │   ├── redis.ts
│   │   ├── webhook.ts
│   │   └── payment.ts
│   ├── models/
│   │   ├── User.ts
│   │   ├── Match.ts
│   │   ├── Message.ts
│   │   └── Affiliate.ts
│   ├── types/
│   │   ├── api.ts
│   │   └── requests.ts
│   └── utils/
│       ├── validation.ts
│       ├── crypto.ts
│       └── logger.ts
├── docs/
│   ├── openapi.yaml            # API specification
│   └── README.md
└── tests/
    ├── integration/
    └── unit/
```

## Stack Tecnológico

- **Node.js 18** - Runtime
- **Express 4** - Web framework
- **TypeScript** - Type safety
- **Firebase Admin SDK** - Database integration
- **Redis** - Rate limiting e caching
- **JWT** - Authentication
- **Swagger/OpenAPI 3.0** - Documentation
- **Jest** - Testing

## Endpoints Principais

### Autenticação
- `POST /api/v1/auth/login` - Login com API key
- `POST /api/v1/auth/refresh` - Refresh token

### Usuários
- `GET /api/v1/users/profile` - Obter perfil
- `PUT /api/v1/users/profile` - Atualizar perfil
- `GET /api/v1/users/{id}` - Perfil público

### Discovery
- `GET /api/v1/discovery/cards` - Cards para swipe
- `POST /api/v1/discovery/swipe` - Registrar swipe

### Matches
- `GET /api/v1/matches` - Lista de matches
- `GET /api/v1/matches/{id}` - Match específico

### Chat
- `GET /api/v1/chat/conversations` - Conversas
- `POST /api/v1/chat/message` - Enviar mensagem
- `GET /api/v1/chat/{id}/messages` - Histórico

### Afiliados
- `POST /api/v1/affiliates/register` - Registrar afiliado
- `GET /api/v1/affiliates/stats` - Estatísticas
- `GET /api/v1/affiliates/earnings` - Ganhos

### Webhooks
- `POST /webhooks/match` - Novo match
- `POST /webhooks/message` - Nova mensagem
- `POST /webhooks/subscription` - Mudança de plano

## Rate Limiting

- **Free Tier**: 100 requests/hora
- **Básico**: 1,000 requests/hora
- **Premium**: 10,000 requests/hora
- **Empresarial**: 100,000 requests/hora

## Próximos Passos

1. [ ] Configurar projeto Node.js/Express
2. [ ] Implementar autenticação JWT
3. [ ] Criar endpoints principais
4. [ ] Implementar rate limiting
5. [ ] Documentação OpenAPI
6. [ ] Testes e deploy