import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import { ThemeProvider } from '@mui/material/styles'
import CssBaseline from '@mui/material/CssBaseline'
import { Provider } from 'react-redux'
import { Toaster } from 'react-hot-toast'
import { theme } from '@/config/theme'
import { store } from '@/store'
import './globals.css'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'FypMatch - Encontre Conexões Genuínas',
  description: 'O aplicativo de relacionamentos que conecta corações usando IA avançada e recursos inclusivos.',
  keywords: ['relacionamento', 'namoro', 'conexões', 'match', 'IA', 'inclusivo'],
  authors: [{ name: 'FypMatch Team' }],
  creator: 'FypMatch',
  publisher: 'FypMatch',
  
  // PWA Configuration
  manifest: '/manifest.json',
  themeColor: '#2196F3',
  
  // Social Media
  openGraph: {
    type: 'website',
    locale: 'pt_BR',
    url: 'https://fypmatch.app',
    title: 'FypMatch - Encontre Conexões Genuínas',
    description: 'O aplicativo de relacionamentos que conecta corações usando IA avançada e recursos inclusivos.',
    siteName: 'FypMatch',
    images: [
      {
        url: '/og-image.png',
        width: 1200,
        height: 630,
        alt: 'FypMatch - Encontre Conexões Genuínas',
      },
    ],
  },
  
  twitter: {
    card: 'summary_large_image',
    title: 'FypMatch - Encontre Conexões Genuínas',
    description: 'O aplicativo de relacionamentos que conecta corações usando IA avançada e recursos inclusivos.',
    images: ['/og-image.png'],
  },
  
  // Mobile App
  appleWebApp: {
    capable: true,
    statusBarStyle: 'default',
    title: 'FypMatch',
  },
  
  // Verification
  verification: {
    google: 'your-google-verification-code',
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="pt-BR">
      <body className={inter.className}>
        <Provider store={store}>
          <ThemeProvider theme={theme}>
            <CssBaseline />
            {children}
            <Toaster
              position="top-center"
              toastOptions={{
                duration: 4000,
                style: {
                  background: '#333',
                  color: '#fff',
                },
              }}
            />
          </ThemeProvider>
        </Provider>
      </body>
    </html>
  )
}