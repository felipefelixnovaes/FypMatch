import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import morgan from 'morgan'
import compression from 'compression'
import rateLimit from 'express-rate-limit'
import swaggerUi from 'swagger-ui-express'
import swaggerJsdoc from 'swagger-jsdoc'
import 'express-async-errors'
import dotenv from 'dotenv'

// Import routes
import authRoutes from './routes/auth'
import userRoutes from './routes/users'
import discoveryRoutes from './routes/discovery'
import matchRoutes from './routes/matches'
import chatRoutes from './routes/chat'
import affiliateRoutes from './routes/affiliates'
import webhookRoutes from './routes/webhooks'

// Import middleware
import { errorHandler } from './middleware/errorHandler'
import { authMiddleware } from './middleware/auth'
import { loggerMiddleware } from './middleware/logger'

// Load environment variables
dotenv.config()

const app = express()
const PORT = process.env.PORT || 3001

// Swagger configuration
const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'FypMatch Public API',
      version: '1.0.0',
      description: 'Public REST API for FypMatch - The Dating App with Advanced AI',
      contact: {
        name: 'FypMatch Team',
        email: 'api@fypmatch.app',
        url: 'https://fypmatch.app'
      },
      license: {
        name: 'MIT',
        url: 'https://opensource.org/licenses/MIT'
      }
    },
    servers: [
      {
        url: process.env.API_BASE_URL || 'http://localhost:3001',
        description: 'FypMatch API Server'
      }
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT'
        },
        apiKey: {
          type: 'apiKey',
          in: 'header',
          name: 'X-API-Key'
        }
      }
    },
    security: [
      {
        bearerAuth: []
      },
      {
        apiKey: []
      }
    ]
  },
  apis: ['./src/routes/*.ts', './src/models/*.ts']
}

const swaggerSpecs = swaggerJsdoc(swaggerOptions)

// Rate limiting configuration
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: (req) => {
    // Different limits based on API tier
    const apiKey = req.headers['x-api-key'] as string
    
    if (!apiKey) return 100 // Free tier
    
    // TODO: Implement API key tier checking
    // For now, return default limits
    return 1000
  },
  message: {
    error: 'Too many requests',
    message: 'Rate limit exceeded. Please try again later.',
    retryAfter: '15 minutes'
  },
  standardHeaders: true,
  legacyHeaders: false
})

// Middleware
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"],
    },
  },
  hsts: {
    maxAge: 31536000,
    includeSubDomains: true,
    preload: true
  }
}))

app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true,
  optionsSuccessStatus: 200
}))

app.use(compression())
app.use(morgan('combined'))
app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true, limit: '10mb' }))
app.use(loggerMiddleware)
app.use(limiter)

// API Documentation
app.use('/docs', swaggerUi.serve, swaggerUi.setup(swaggerSpecs, {
  customCss: '.swagger-ui .topbar { display: none }',
  customSiteTitle: 'FypMatch API Documentation',
  customfavIcon: '/favicon.ico'
}))

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    version: '1.0.0',
    uptime: process.uptime()
  })
})

// API Routes
app.use('/api/v1/auth', authRoutes)
app.use('/api/v1/users', authMiddleware, userRoutes)
app.use('/api/v1/discovery', authMiddleware, discoveryRoutes)
app.use('/api/v1/matches', authMiddleware, matchRoutes)
app.use('/api/v1/chat', authMiddleware, chatRoutes)
app.use('/api/v1/affiliates', authMiddleware, affiliateRoutes)
app.use('/webhooks', webhookRoutes)

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    message: 'Welcome to FypMatch API',
    version: '1.0.0',
    documentation: '/docs',
    health: '/health',
    endpoints: {
      auth: '/api/v1/auth',
      users: '/api/v1/users',
      discovery: '/api/v1/discovery',
      matches: '/api/v1/matches',
      chat: '/api/v1/chat',
      affiliates: '/api/v1/affiliates',
      webhooks: '/webhooks'
    }
  })
})

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Not Found',
    message: `Route ${req.originalUrl} not found`,
    suggestion: 'Check the API documentation at /docs'
  })
})

// Error handler (must be last)
app.use(errorHandler)

// Start server
app.listen(PORT, () => {
  console.log(`🚀 FypMatch API Server running on port ${PORT}`)
  console.log(`📚 API Documentation: http://localhost:${PORT}/docs`)
  console.log(`❤️  Health Check: http://localhost:${PORT}/health`)
  console.log(`🌍 Environment: ${process.env.NODE_ENV || 'development'}`)
})

export default app