// Firebase SDK Configuration
import { initializeApp } from 'firebase/app';
import { getAnalytics } from 'firebase/analytics';
import { getFirestore } from 'firebase/firestore';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';
import { getPerformance } from 'firebase/performance';

// Configuração do Firebase - FypMatch
const firebaseConfig = {
  apiKey: "AIzaSyD_VEm6yh-kDtVBwZ-ISsK6woK4RIp6oec",
  authDomain: "fypmatch-8ac3c.firebaseapp.com",
  projectId: "fypmatch-8ac3c",
  storageBucket: "fypmatch-8ac3c.firebasestorage.app",
  messagingSenderId: "98859676437",
  appId: "1:98859676437:web:2cc88e7ad1f47d0925af1c",
  measurementId: "G-YV1P5N7TGZ"
};

// Inicializar Firebase
const app = initializeApp(firebaseConfig);

// Inicializar serviços
const analytics = getAnalytics(app);
const db = getFirestore(app);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();
const perf = getPerformance(app);

// Exportar para uso em outros arquivos
export { 
  app, 
  analytics, 
  db, 
  auth, 
  provider, 
  perf 
};

// Configuração adicional
console.log('🔥 Firebase inicializado com sucesso!');

// Analytics personalizado para landing page
if (typeof gtag !== 'undefined') {
  // Configurar eventos customizados
  window.trackFirebaseEvent = function(eventName, parameters) {
    gtag('event', eventName, parameters);
  };
} 