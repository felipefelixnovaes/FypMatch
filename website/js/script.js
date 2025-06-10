// ===== MOBILE NAVIGATION =====
document.addEventListener('DOMContentLoaded', function() {
    const navToggle = document.getElementById('nav-toggle');
    const navMenu = document.getElementById('nav-menu');
    const navLinks = document.querySelectorAll('.nav-link');

    // Toggle mobile menu
    navToggle.addEventListener('click', function() {
        navMenu.classList.toggle('active');
        
        // Animate hamburger
        const bars = navToggle.querySelectorAll('.bar');
        bars.forEach((bar, index) => {
            bar.style.transform = navMenu.classList.contains('active') 
                ? `rotate(${index === 0 ? '45' : index === 1 ? '0' : '-45'}deg) translate(${index === 1 ? '100px' : '0'}, ${index === 0 ? '6px' : index === 2 ? '-6px' : '0'})`
                : 'none';
            bar.style.opacity = navMenu.classList.contains('active') && index === 1 ? '0' : '1';
        });
    });

    // Close mobile menu when clicking on a link
    navLinks.forEach(link => {
        link.addEventListener('click', function() {
            navMenu.classList.remove('active');
            
            // Reset hamburger
            const bars = navToggle.querySelectorAll('.bar');
            bars.forEach(bar => {
                bar.style.transform = 'none';
                bar.style.opacity = '1';
            });
        });
    });

    // Close mobile menu when clicking outside
    document.addEventListener('click', function(e) {
        if (!navToggle.contains(e.target) && !navMenu.contains(e.target)) {
            navMenu.classList.remove('active');
            
            // Reset hamburger
            const bars = navToggle.querySelectorAll('.bar');
            bars.forEach(bar => {
                bar.style.transform = 'none';
                bar.style.opacity = '1';
            });
        }
    });
});

// ===== SMOOTH SCROLLING =====
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// ===== NAVBAR SCROLL EFFECT =====
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar');
    const scrollY = window.scrollY;
    
    if (scrollY > 50) {
        navbar.style.background = 'rgba(255, 255, 255, 0.98)';
        navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
    } else {
        navbar.style.background = 'rgba(255, 255, 255, 0.95)';
        navbar.style.boxShadow = 'none';
    }
});

// ===== INTERSECTION OBSERVER FOR ANIMATIONS =====
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe elements for animations
document.addEventListener('DOMContentLoaded', function() {
    const animatedElements = document.querySelectorAll('.feature-card, .pricing-card, .contact-item');
    
    animatedElements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
});

// ===== CONTACT FORM HANDLING =====
document.getElementById('contact-form').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    const formObject = {};
    formData.forEach((value, key) => {
        formObject[key] = value;
    });
    
    // Show loading state
    const submitButton = this.querySelector('button[type="submit"]');
    const originalText = submitButton.innerHTML;
    submitButton.innerHTML = '<span class="loading-spinner"></span> Enviando...';
    submitButton.disabled = true;
    
    // Simulate form submission (replace with actual endpoint)
    setTimeout(() => {
        // Success message
        showNotification('✅ Mensagem enviada com sucesso! Entraremos em contato em breve.', 'success');
        
        // Reset form
        this.reset();
        
        // Reset button
        submitButton.innerHTML = originalText;
        submitButton.disabled = false;
    }, 2000);
});

// ===== NOTIFICATION SYSTEM =====
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <span class="notification-message">${message}</span>
            <button class="notification-close">&times;</button>
        </div>
    `;
    
    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#10B981' : type === 'error' ? '#EF4444' : '#3B82F6'};
        color: white;
        padding: 16px 20px;
        border-radius: 12px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        z-index: 10000;
        transform: translateX(400px);
        transition: transform 0.3s ease;
        max-width: 400px;
    `;
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Close button
    notification.querySelector('.notification-close').addEventListener('click', () => {
        closeNotification(notification);
    });
    
    // Auto close after 5 seconds
    setTimeout(() => {
        closeNotification(notification);
    }, 5000);
}

function closeNotification(notification) {
    notification.style.transform = 'translateX(400px)';
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 300);
}

// ===== STATS COUNTER ANIMATION =====
function animateCounter(element, target) {
    let count = 0;
    const increment = target / 100;
    const timer = setInterval(() => {
        count += increment;
        if (count >= target) {
            element.textContent = target;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(count);
        }
    }, 20);
}

// Animate stats when they come into view
const statsObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const statsNumbers = entry.target.querySelectorAll('.stat-number');
            statsNumbers.forEach(stat => {
                const text = stat.textContent;
                const number = parseInt(text.replace(/[^\d]/g, ''));
                if (number) {
                    stat.textContent = '0';
                    animateCounter(stat, number);
                }
            });
            statsObserver.unobserve(entry.target);
        }
    });
}, observerOptions);

document.addEventListener('DOMContentLoaded', function() {
    const heroStats = document.querySelector('.hero-stats');
    if (heroStats) {
        statsObserver.observe(heroStats);
    }
});

// ===== FLOATING HEARTS ANIMATION =====
function createFloatingHeart() {
    const heart = document.createElement('div');
    heart.className = 'floating-heart';
    heart.textContent = ['💕', '💖', '💗', '💘', '💝'][Math.floor(Math.random() * 5)];
    heart.style.cssText = `
        position: fixed;
        bottom: -50px;
        left: ${Math.random() * window.innerWidth}px;
        font-size: ${Math.random() * 20 + 15}px;
        z-index: 1;
        pointer-events: none;
        animation: floatUp 6s linear forwards;
        opacity: 0.3;
    `;
    
    document.body.appendChild(heart);
    
    setTimeout(() => {
        if (heart.parentNode) {
            heart.parentNode.removeChild(heart);
        }
    }, 6000);
}

// Add floating hearts CSS
const floatingHeartsCSS = `
    @keyframes floatUp {
        0% {
            transform: translateY(0) rotate(0deg);
            opacity: 0.3;
        }
        50% {
            opacity: 0.8;
        }
        100% {
            transform: translateY(-100vh) rotate(360deg);
            opacity: 0;
        }
    }
`;

const style = document.createElement('style');
style.textContent = floatingHeartsCSS;
document.head.appendChild(style);

// Create floating hearts periodically
setInterval(createFloatingHeart, 3000);

// ===== PRICING TOGGLE =====
function initPricingToggle() {
    const toggleButton = document.createElement('div');
    toggleButton.className = 'pricing-toggle';
    toggleButton.innerHTML = `
        <div class="toggle-container">
            <span class="toggle-label ${!window.isYearly ? 'active' : ''}">Mensal</span>
            <div class="toggle-switch">
                <input type="checkbox" id="pricing-toggle" ${window.isYearly ? 'checked' : ''}>
                <label for="pricing-toggle" class="toggle-slider"></label>
            </div>
            <span class="toggle-label ${window.isYearly ? 'active' : ''}">Anual <span class="discount-badge">-20%</span></span>
        </div>
    `;
    
    const pricingHeader = document.querySelector('.pricing .section-header');
    if (pricingHeader) {
        pricingHeader.appendChild(toggleButton);
    }
    
    // Add toggle styles
    const toggleCSS = `
        .pricing-toggle {
            margin-top: 32px;
        }
        .toggle-container {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 16px;
        }
        .toggle-label {
            font-weight: 500;
            color: #666;
            transition: color 0.3s ease;
        }
        .toggle-label.active {
            color: #E91E63;
        }
        .toggle-switch {
            position: relative;
        }
        .toggle-switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }
        .toggle-slider {
            position: relative;
            display: block;
            width: 50px;
            height: 24px;
            background: #ccc;
            border-radius: 24px;
            cursor: pointer;
            transition: background 0.3s ease;
        }
        .toggle-slider:before {
            content: '';
            position: absolute;
            top: 2px;
            left: 2px;
            width: 20px;
            height: 20px;
            background: white;
            border-radius: 50%;
            transition: transform 0.3s ease;
        }
        input:checked + .toggle-slider {
            background: #E91E63;
        }
        input:checked + .toggle-slider:before {
            transform: translateX(26px);
        }
        .discount-badge {
            background: #10B981;
            color: white;
            padding: 2px 6px;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 600;
        }
    `;
    
    const toggleStyle = document.createElement('style');
    toggleStyle.textContent = toggleCSS;
    document.head.appendChild(toggleStyle);
    
    // Handle toggle
    document.getElementById('pricing-toggle').addEventListener('change', function() {
        window.isYearly = this.checked;
        updatePricing();
        
        // Update active labels
        document.querySelectorAll('.toggle-label').forEach(label => {
            label.classList.toggle('active');
        });
    });
}

function updatePricing() {
    const prices = {
        premium: { monthly: 19.90, yearly: 15.92 },
        vip: { monthly: 39.90, yearly: 31.92 }
    };
    
    document.querySelectorAll('.pricing-card').forEach(card => {
        const titleElement = card.querySelector('h3');
        const amountElement = card.querySelector('.amount');
        
        if (titleElement && amountElement) {
            const plan = titleElement.textContent.toLowerCase();
            if (prices[plan]) {
                const price = window.isYearly ? prices[plan].yearly : prices[plan].monthly;
                amountElement.textContent = price.toFixed(2).replace('.', ',');
                
                const periodElement = card.querySelector('.period');
                if (periodElement) {
                    periodElement.textContent = window.isYearly ? '/mês*' : '/mês';
                }
            }
        }
    });
    
    // Add yearly discount note
    const pricingSection = document.querySelector('.pricing');
    let discountNote = pricingSection.querySelector('.yearly-note');
    
    if (window.isYearly && !discountNote) {
        discountNote = document.createElement('p');
        discountNote.className = 'yearly-note';
        discountNote.innerHTML = '<small>* Valores com desconto de 20% no plano anual, cobrado R$ 191,04 (Premium) ou R$ 383,04 (VIP) por ano.</small>';
        discountNote.style.cssText = 'text-align: center; color: #666; margin-top: 20px; font-size: 14px;';
        pricingSection.appendChild(discountNote);
    } else if (!window.isYearly && discountNote) {
        discountNote.remove();
    }
}

// Initialize pricing toggle when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.isYearly = false;
    initPricingToggle();
});

// ===== LAZY LOADING FOR IMAGES =====
document.addEventListener('DOMContentLoaded', function() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                imageObserver.unobserve(img);
            }
        });
    });
    
    images.forEach(img => {
        imageObserver.observe(img);
    });
});

// ===== SCROLL TO TOP BUTTON =====
function createScrollToTopButton() {
    const button = document.createElement('button');
    button.innerHTML = '↑';
    button.className = 'scroll-to-top';
    button.style.cssText = `
        position: fixed;
        bottom: 30px;
        right: 30px;
        width: 50px;
        height: 50px;
        border: none;
        border-radius: 50%;
        background: linear-gradient(135deg, #E91E63 0%, #9C27B0 50%, #673AB7 100%);
        color: white;
        font-size: 20px;
        font-weight: bold;
        cursor: pointer;
        z-index: 1000;
        transition: all 0.3s ease;
        opacity: 0;
        transform: translateY(20px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    `;
    
    document.body.appendChild(button);
    
    // Show/hide button based on scroll position
    window.addEventListener('scroll', () => {
        if (window.scrollY > 300) {
            button.style.opacity = '1';
            button.style.transform = 'translateY(0)';
        } else {
            button.style.opacity = '0';
            button.style.transform = 'translateY(20px)';
        }
    });
    
    // Scroll to top when clicked
    button.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
    
    // Hover effect
    button.addEventListener('mouseenter', () => {
        button.style.transform = 'translateY(0) scale(1.1)';
    });
    
    button.addEventListener('mouseleave', () => {
        button.style.transform = 'translateY(0) scale(1)';
    });
}

// Initialize scroll to top button
document.addEventListener('DOMContentLoaded', createScrollToTopButton);

// ===== PERFORMANCE OPTIMIZATION =====
// Preload critical resources
function preloadResources() {
    const criticalImages = [
        'images/logo.png',
        'images/app-screenshot-1.png'
    ];
    
    criticalImages.forEach(src => {
        const link = document.createElement('link');
        link.rel = 'preload';
        link.as = 'image';
        link.href = src;
        document.head.appendChild(link);
    });
}

// Initialize preloading
document.addEventListener('DOMContentLoaded', preloadResources);

// ===== ERROR HANDLING =====
window.addEventListener('error', function(e) {
    console.error('Erro na página:', e.error);
    // Optionally send error to analytics
});

// ===== ANALYTICS EVENTS =====
function trackEvent(eventName, properties = {}) {
    // Google Analytics 4 event tracking
    if (typeof gtag !== 'undefined') {
        gtag('event', eventName, properties);
    }
    
    // Console log for development
    console.log(`Event tracked: ${eventName}`, properties);
}

// Track important interactions
document.addEventListener('DOMContentLoaded', function() {
    // Track download button clicks
    document.querySelectorAll('.download-btn, .btn-primary').forEach(btn => {
        btn.addEventListener('click', function(e) {
            const buttonText = this.textContent.trim();
            trackEvent('button_click', {
                button_text: buttonText,
                button_location: this.closest('section')?.id || 'unknown'
            });
        });
    });
    
    // Track form submission
    document.getElementById('contact-form').addEventListener('submit', function() {
        trackEvent('form_submit', {
            form_type: 'contact'
        });
    });
    
    // Track section views
    const sections = document.querySelectorAll('section[id]');
    const sectionObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                trackEvent('section_view', {
                    section_id: entry.target.id
                });
            }
        });
    }, { threshold: 0.5 });
    
    sections.forEach(section => {
        sectionObserver.observe(section);
    });
});

console.log('🚀 FypMatch Website loaded successfully!'); 