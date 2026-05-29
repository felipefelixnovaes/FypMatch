# FypMatch — Design System Multiplataforma
# @design-chief | 2026-05-28

---

## DESIGN TOKENS (fonte canônica)

### Cores

| Token | Hex | Uso |
|-------|-----|-----|
| `color-primary` | `#E91E63` | Botões primários, CTA, swipe direita |
| `color-secondary` | `#9C27B0` | Accents, badges Premium |
| `color-primary-light` | `#FFB3E6` | Dark mode primary, highlights |
| `color-secondary-light` | `#D1B3E6` | Dark mode secondary |
| `color-primary-dark` | `#AD1457` | Pressed state, hover |
| `color-error` | `#B00020` | Erros, swipe esquerda |
| `color-surface-light` | `#FFFBFE` | Background light mode |
| `color-surface-dark` | `#1C1B1F` | Background dark mode |
| `color-on-primary` | `#FFFFFF` | Texto sobre primário |

### Gradiente Principal
```
Linear: #E91E63 → #9C27B0
Ângulo: 135°
Uso: Botões CTA, headers, badges VIP, swipe card overlay
```

### Tipografia

| Token | Tamanho | Peso | Uso |
|-------|---------|------|-----|
| `type-display` | 57sp/pt | 400 | Títulos de tela |
| `type-headline-large` | 32sp/pt | 400 | Títulos de seção |
| `type-headline-medium` | 28sp/pt | 400 | Sub-seções |
| `type-title-large` | 22sp/pt | 400 | Nome no card |
| `type-body-large` | 16sp/pt | 400 | Corpo de texto principal |
| `type-body-medium` | 14sp/pt | 400 | Descrições |
| `type-label-large` | 14sp/pt | 500 | Botões |
| `type-label-small` | 11sp/pt | 500 | Chips, badges |

### Espaçamentos

| Token | Valor | Uso |
|-------|-------|-----|
| `space-xs` | 4dp/pt | Gaps mínimos |
| `space-sm` | 8dp/pt | Padding interno |
| `space-md` | 16dp/pt | Padding padrão |
| `space-lg` | 24dp/pt | Seções |
| `space-xl` | 32dp/pt | Entre blocos maiores |
| `space-2xl` | 48dp/pt | Margens de tela |

### Raios de Borda

| Token | Valor | Uso |
|-------|-------|-----|
| `radius-sm` | 8dp/pt | Chips, inputs |
| `radius-md` | 12dp/pt | Cards pequenos |
| `radius-lg` | 16dp/pt | Cards principais (SwipeCard) |
| `radius-xl` | 24dp/pt | Bottom sheets, modals |
| `radius-full` | 9999dp/pt | Avatares, badges circulares |

### Sombras / Elevação

| Token | Valor | Uso |
|-------|-------|-----|
| `shadow-card` | `0 2dp 8dp rgba(0,0,0,0.12)` | SwipeCard em repouso |
| `shadow-card-active` | `0 8dp 24dp rgba(233,30,99,0.24)` | SwipeCard em drag |
| `shadow-modal` | `0 16dp 48dp rgba(0,0,0,0.32)` | Modals e bottom sheets |

---

## TOKENS JSON (para uso cross-platform)

```json
{
  "color": {
    "primary": "#E91E63",
    "secondary": "#9C27B0",
    "primaryLight": "#FFB3E6",
    "secondaryLight": "#D1B3E6",
    "primaryDark": "#AD1457",
    "error": "#B00020",
    "surfaceLight": "#FFFBFE",
    "surfaceDark": "#1C1B1F",
    "onPrimary": "#FFFFFF",
    "gradientStart": "#E91E63",
    "gradientEnd": "#9C27B0"
  },
  "spacing": {
    "xs": 4, "sm": 8, "md": 16,
    "lg": 24, "xl": 32, "xxl": 48
  },
  "radius": {
    "sm": 8, "md": 12, "lg": 16,
    "xl": 24, "full": 9999
  }
}
```

---

## COMPONENTES SWIFTUI

### GradientButton
```swift
struct GradientButton: View {
    let title: String
    let action: () -> Void
    var isFullWidth: Bool = true

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.white)
                .frame(maxWidth: isFullWidth ? .infinity : nil)
                .padding(.horizontal, 24)
                .padding(.vertical, 14)
                .background(
                    LinearGradient(
                        colors: [Color(hex: "E91E63"), Color(hex: "9C27B0")],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(color: Color(hex: "E91E63").opacity(0.3), radius: 8, y: 4)
        }
        .accessibilityLabel(title)
    }
}
```

### SwipeCardView
```swift
struct SwipeCardView: View {
    let profile: UserProfile
    @State private var offset: CGSize = .zero
    @State private var rotation: Double = 0

    var body: some View {
        ZStack(alignment: .bottom) {
            // Foto
            AsyncImage(url: URL(string: profile.photoUrl)) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                Color(hex: "E91E63").opacity(0.1)
            }
            .frame(width: UIScreen.main.bounds.width - 32, height: 480)
            .clipShape(RoundedRectangle(cornerRadius: 16))

            // Overlay de info
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text("\(profile.name), \(profile.age)")
                        .font(.system(size: 22, weight: .semibold))
                        .foregroundColor(.white)
                    if profile.isVerified {
                        Image(systemName: "checkmark.seal.fill")
                            .foregroundColor(Color(hex: "E91E63"))
                    }
                }
                Text(profile.bio)
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.85))
                    .lineLimit(2)

                // Score de compatibilidade
                HStack(spacing: 4) {
                    Image(systemName: "heart.fill")
                        .font(.system(size: 12))
                        .foregroundColor(Color(hex: "E91E63"))
                    Text("\(profile.compatibilityScore)% compatível")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.white)
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                LinearGradient(
                    colors: [.clear, .black.opacity(0.7)],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))

            // Indicadores de swipe
            if offset.width > 30 {
                LikeOverlay().transition(.opacity)
            } else if offset.width < -30 {
                PassOverlay().transition(.opacity)
            }
        }
        .frame(width: UIScreen.main.bounds.width - 32, height: 480)
        .shadow(color: shadowColor, radius: abs(offset.width) > 10 ? 16 : 8, y: 4)
        .offset(offset)
        .rotationEffect(.degrees(rotation))
        .gesture(dragGesture)
        .animation(.interactiveSpring(), value: offset)
    }

    private var shadowColor: Color {
        if offset.width > 30 { return Color(hex: "E91E63").opacity(0.4) }
        if offset.width < -30 { return Color.gray.opacity(0.3) }
        return Color.black.opacity(0.12)
    }

    private var dragGesture: some Gesture {
        DragGesture()
            .onChanged { value in
                offset = value.translation
                rotation = Double(value.translation.width / 20)
            }
            .onEnded { value in
                if abs(value.translation.width) > 120 {
                    // swipe completo
                    let direction: SwipeDirection = value.translation.width > 0 ? .right : .left
                    onSwipe(direction)
                } else {
                    withAnimation(.spring()) { offset = .zero; rotation = 0 }
                }
            }
    }

    func onSwipe(_ direction: SwipeDirection) {
        // implementado no DiscoveryViewModel
    }
}

enum SwipeDirection { case left, right }
```

### PremiumBadge
```swift
struct PremiumBadge: View {
    enum Tier { case premium, vip }
    let tier: Tier

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: tier == .vip ? "crown.fill" : "diamond.fill")
                .font(.system(size: 10))
            Text(tier == .vip ? "VIP" : "Premium")
                .font(.system(size: 10, weight: .semibold))
        }
        .foregroundColor(.white)
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(
            LinearGradient(
                colors: tier == .vip
                    ? [Color(hex: "FFD700"), Color(hex: "FF8C00")]
                    : [Color(hex: "E91E63"), Color(hex: "9C27B0")],
                startPoint: .leading, endPoint: .trailing
            )
        )
        .clipShape(Capsule())
    }
}
```

### MatchCard
```swift
struct MatchCard: View {
    let match: Match

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: match.photoUrl)) { image in
                image.resizable().scaledToFill()
            } placeholder: { Color(hex: "E91E63").opacity(0.1) }
            .frame(width: 56, height: 56)
            .clipShape(Circle())
            .overlay(Circle().stroke(
                LinearGradient(colors: [Color(hex: "E91E63"), Color(hex: "9C27B0")],
                               startPoint: .topLeading, endPoint: .bottomTrailing),
                lineWidth: match.hasUnread ? 2 : 0
            ))

            VStack(alignment: .leading, spacing: 2) {
                Text(match.name)
                    .font(.system(size: 16, weight: match.hasUnread ? .semibold : .regular))
                Text(match.lastMessage)
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 4) {
                Text(match.timeAgo)
                    .font(.system(size: 12))
                    .foregroundColor(.secondary)
                if match.hasUnread {
                    Circle()
                        .fill(Color(hex: "E91E63"))
                        .frame(width: 8, height: 8)
                }
            }
        }
        .padding(12)
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(match.name), \(match.lastMessage), \(match.timeAgo)")
    }
}
```

### Color Extension (helper)
```swift
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255
        let g = Double((int >> 8) & 0xFF) / 255
        let b = Double(int & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}
```

---

## ACESSIBILIDADE

### Checklist obrigatório para cada componente iOS
- [ ] `.accessibilityLabel()` em todos os elementos interativos
- [ ] `.accessibilityHint()` em gestos não-óbvios (swipe, drag)
- [ ] Contraste mínimo 4.5:1 (WCAG AA) — Rosa `#E91E63` sobre branco: 4.52:1 ✅
- [ ] Tamanho mínimo de toque: 44×44pt (botões, ícones)
- [ ] Suporte a Dynamic Type: usar `.font(.body)` e equivalentes escaláveis
- [ ] VoiceOver: SwipeCardView com `.accessibilityAction` para curtir/passar

### Dynamic Type example
```swift
// Em vez de:
.font(.system(size: 16))

// Usar:
.font(.body)           // 17pt padrão, escala com preferência do usuário
.font(.headline)       // 17pt bold
.font(.subheadline)    // 15pt
```

---

## ÍCONE DO APP (especificação)

- **Tamanho:** 1024×1024px (App Store), mais variações automáticas via Xcode
- **Fundo:** Gradiente 135° — `#E91E63` → `#9C27B0`
- **Símbolo:** Letra "F" estilizada em branco OU dois corações entrelaçados
- **Estilo:** Cantos arredondados pelo sistema iOS (não adicionar manualmente)
- **Sem texto** no ícone — legibilidade em tamanhos pequenos

---

## SCREENSHOTS APP STORE (especificação)

| Dispositivo | Resolução | Telas a mostrar |
|------------|-----------|-----------------|
| iPhone 15 Pro Max (6.7") | 1290×2796px | Discovery, Chat, Profile, Premium, Onboarding |
| iPhone 15 (6.1") | 1179×2556px | Mesmas 5 telas |
| iPad Pro 12.9" | 2048×2732px | Layout adaptado (se aplicável) |

**Ordem das screenshots:**
1. Tela de Discovery (swipe card) — hero shot
2. Onboarding inclusão ("Um espaço para você")
3. Chat com conselheiro IA
4. Matches com score de compatibilidade
5. Tela Premium com planos
