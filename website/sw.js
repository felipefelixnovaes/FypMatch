// Service Worker for FypMatch Website - Performance Optimization
const CACHE_NAME = 'fypmatch-v2.7.0';
const STATIC_CACHE = 'fypmatch-static-v2.7.0';

// Assets to cache immediately
const CRITICAL_ASSETS = [
  '/',
  '/index.html',
  '/css/critical.css',
  '/images/logo.png',
  '/app-ads.txt',
  '/robots.txt'
];

// Assets to cache on demand
const DYNAMIC_ASSETS = [
  '/css/style.css',
  '/js/script.js',
  '/privacy',
  '/terms',
  '/cookies'
];

// Install event - cache critical assets
self.addEventListener('install', event => {
  console.log('🔧 Service Worker installing...');
  
  event.waitUntil(
    caches.open(STATIC_CACHE)
      .then(cache => {
        console.log('📦 Caching critical assets');
        return cache.addAll(CRITICAL_ASSETS);
      })
      .then(() => {
        console.log('✅ Critical assets cached');
        return self.skipWaiting();
      })
      .catch(err => console.error('❌ Cache failed:', err))
  );
});

// Activate event - clean old caches
self.addEventListener('activate', event => {
  console.log('🔄 Service Worker activating...');
  
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== STATIC_CACHE && cacheName !== CACHE_NAME) {
            console.log('🗑️ Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    }).then(() => {
      console.log('✅ Service Worker activated');
      return self.clients.claim();
    })
  );
});

// Fetch event - serve from cache, fallback to network
self.addEventListener('fetch', event => {
  const { request } = event;
  
  // Skip non-GET requests
  if (request.method !== 'GET') return;
  
  // Skip external requests
  if (!request.url.startsWith(self.location.origin)) return;
  
  event.respondWith(
    caches.match(request)
      .then(cachedResponse => {
        // Return cached version if available
        if (cachedResponse) {
          console.log('📦 Serving from cache:', request.url);
          return cachedResponse;
        }
        
        // Otherwise fetch from network
        console.log('🌐 Fetching from network:', request.url);
        return fetch(request)
          .then(response => {
            // Don't cache non-successful responses
            if (!response || response.status !== 200 || response.type !== 'basic') {
              return response;
            }
            
            // Clone response for cache
            const responseToCache = response.clone();
            
            // Cache dynamic assets
            if (shouldCache(request.url)) {
              caches.open(CACHE_NAME)
                .then(cache => {
                  console.log('💾 Caching:', request.url);
                  cache.put(request, responseToCache);
                });
            }
            
            return response;
          })
          .catch(err => {
            console.error('❌ Network fetch failed:', err);
            
            // Return offline fallback for HTML pages
            if (request.headers.get('accept').includes('text/html')) {
              return new Response(`
                <!DOCTYPE html>
                <html>
                <head>
                  <title>FypMatch - Offline</title>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <style>
                    body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                    .offline { max-width: 400px; margin: 0 auto; }
                    .emoji { font-size: 4em; margin-bottom: 20px; }
                    h1 { color: #E91E63; }
                    p { color: #666; }
                    .btn { background: #E91E63; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 20px; }
                  </style>
                </head>
                <body>
                  <div class="offline">
                    <div class="emoji">💔</div>
                    <h1>Você está offline</h1>
                    <p>Não foi possível carregar esta página. Verifique sua conexão com a internet.</p>
                    <a href="/" class="btn">Tentar novamente</a>
                  </div>
                </body>
                </html>
              `, {
                headers: {
                  'Content-Type': 'text/html',
                  'Cache-Control': 'no-cache'
                }
              });
            }
            
            throw err;
          });
      })
  );
});

// Helper function to determine if URL should be cached
function shouldCache(url) {
  // Cache CSS, JS, images, and HTML pages
  return url.includes('.css') || 
         url.includes('.js') || 
         url.includes('.png') || 
         url.includes('.jpg') || 
         url.includes('.webp') ||
         url.includes('.svg') ||
         url.includes('/privacy') ||
         url.includes('/terms') ||
         url.includes('/cookies') ||
         url.endsWith('/');
}

// Background sync for analytics
self.addEventListener('sync', event => {
  if (event.tag === 'analytics-sync') {
    event.waitUntil(
      // Sync any pending analytics data
      console.log('📊 Syncing analytics data...')
    );
  }
});

// Push notifications (for future use)
self.addEventListener('push', event => {
  const options = {
    body: event.data ? event.data.text() : 'Nova atualização do FypMatch!',
    icon: '/images/logo.png',
    badge: '/images/logo.png',
    data: {
      url: '/'
    }
  };
  
  event.waitUntil(
    self.registration.showNotification('FypMatch', options)
  );
});

// Handle notification clicks
self.addEventListener('notificationclick', event => {
  event.notification.close();
  
  event.waitUntil(
    clients.openWindow(event.notification.data.url || '/')
  );
}); 