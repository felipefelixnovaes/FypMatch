'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAppSelector, useAppDispatch } from '@/hooks/redux'
import { selectAuth, checkAuthStatus } from '@/store/slices/authSlice'
import { Box, Container, Typography, Button, Card, CardContent, Grid, Fade } from '@mui/material'
import { motion } from 'framer-motion'
import FavoriteIcon from '@mui/icons-material/Favorite'
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome'
import SecurityIcon from '@mui/icons-material/Security'
import GroupIcon from '@mui/icons-material/Group'

export default function HomePage() {
  const router = useRouter()
  const dispatch = useAppDispatch()
  const { isAuthenticated, loading } = useAppSelector(selectAuth)

  useEffect(() => {
    dispatch(checkAuthStatus())
  }, [dispatch])

  useEffect(() => {
    if (isAuthenticated && !loading) {
      router.push('/app/discovery')
    }
  }, [isAuthenticated, loading, router])

  const handleGetStarted = () => {
    router.push('/auth/login')
  }

  const features = [
    {
      icon: <FavoriteIcon sx={{ fontSize: 48, color: 'primary.main' }} />,
      title: 'Matches Inteligentes',
      description: 'Nossa IA avançada encontra conexões genuínas baseadas em compatibilidade real.'
    },
    {
      icon: <AutoAwesomeIcon sx={{ fontSize: 48, color: 'secondary.main' }} />,
      title: 'IA Inclusiva',
      description: 'Recursos adaptativos para neurodiversidade e assistente neural personalizado.'
    },
    {
      icon: <SecurityIcon sx={{ fontSize: 48, color: 'success.main' }} />,
      title: 'Seguro & Privado',
      description: 'Verificação avançada de perfis e proteção total dos seus dados pessoais.'
    },
    {
      icon: <GroupIcon sx={{ fontSize: 48, color: 'warning.main' }} />,
      title: 'Comunidade Ativa',
      description: 'Milhares de usuários reais procurando por conexões significativas.'
    }
  ]

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
      >
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
        >
          <FavoriteIcon sx={{ fontSize: 64, color: 'primary.main' }} />
        </motion.div>
      </Box>
    )
  }

  return (
    <Box sx={{ minHeight: '100vh', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
      {/* Hero Section */}
      <Container maxWidth="lg" sx={{ pt: 8, pb: 6 }}>
        <Fade in={true} timeout={1000}>
          <Box textAlign="center" mb={6}>
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8 }}
            >
              <Typography
                variant="h1"
                component="h1"
                sx={{
                  fontSize: { xs: '2.5rem', md: '4rem' },
                  fontWeight: 700,
                  color: 'white',
                  mb: 2,
                  textShadow: '0 2px 4px rgba(0,0,0,0.3)'
                }}
              >
                FypMatch
              </Typography>
              
              <Typography
                variant="h4"
                component="h2"
                sx={{
                  fontSize: { xs: '1.2rem', md: '1.8rem' },
                  color: 'rgba(255, 255, 255, 0.9)',
                  mb: 4,
                  fontWeight: 300
                }}
              >
                Encontre conexões genuínas com IA avançada
              </Typography>
              
              <motion.div
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.8, delay: 0.2 }}
              >
                <Button
                  variant="contained"
                  size="large"
                  onClick={handleGetStarted}
                  sx={{
                    px: 6,
                    py: 2,
                    fontSize: '1.2rem',
                    borderRadius: 3,
                    bgcolor: 'white',
                    color: 'primary.main',
                    '&:hover': {
                      bgcolor: 'rgba(255, 255, 255, 0.9)',
                      transform: 'translateY(-2px)',
                    },
                    transition: 'all 0.3s ease'
                  }}
                >
                  Começar Agora
                </Button>
              </motion.div>
            </motion.div>
          </Box>
        </Fade>
      </Container>

      {/* Features Section */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Typography
          variant="h3"
          component="h2"
          textAlign="center"
          sx={{ mb: 6, color: 'white', fontWeight: 600 }}
        >
          Por que escolher FypMatch?
        </Typography>
        
        <Grid container spacing={4}>
          {features.map((feature, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: index * 0.1 }}
              >
                <Card
                  sx={{
                    height: '100%',
                    borderRadius: 3,
                    backdropFilter: 'blur(10px)',
                    bgcolor: 'rgba(255, 255, 255, 0.1)',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    '&:hover': {
                      transform: 'translateY(-8px)',
                      bgcolor: 'rgba(255, 255, 255, 0.15)',
                    },
                    transition: 'all 0.3s ease'
                  }}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <Box mb={2}>
                      {feature.icon}
                    </Box>
                    <Typography
                      variant="h6"
                      component="h3"
                      sx={{ mb: 2, color: 'white', fontWeight: 600 }}
                    >
                      {feature.title}
                    </Typography>
                    <Typography
                      variant="body2"
                      sx={{ color: 'rgba(255, 255, 255, 0.8)', lineHeight: 1.6 }}
                    >
                      {feature.description}
                    </Typography>
                  </CardContent>
                </Card>
              </motion.div>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* CTA Section */}
      <Container maxWidth="md" sx={{ py: 8, textAlign: 'center' }}>
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.8, delay: 0.4 }}
        >
          <Typography
            variant="h4"
            component="h2"
            sx={{ mb: 3, color: 'white', fontWeight: 600 }}
          >
            Pronto para encontrar sua conexão especial?
          </Typography>
          
          <Typography
            variant="body1"
            sx={{ mb: 4, color: 'rgba(255, 255, 255, 0.9)', fontSize: '1.1rem' }}
          >
            Junte-se a milhares de pessoas que já encontraram o amor no FypMatch
          </Typography>
          
          <Button
            variant="contained"
            size="large"
            onClick={handleGetStarted}
            sx={{
              px: 5,
              py: 1.5,
              fontSize: '1.1rem',
              borderRadius: 3,
              bgcolor: 'rgba(255, 255, 255, 0.9)',
              color: 'primary.main',
              '&:hover': {
                bgcolor: 'white',
                transform: 'scale(1.05)',
              },
              transition: 'all 0.3s ease'
            }}
          >
            Começar Gratuitamente
          </Button>
        </motion.div>
      </Container>
    </Box>
  )
}