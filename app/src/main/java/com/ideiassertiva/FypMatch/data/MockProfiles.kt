package com.ideiassertiva.FypMatch.data

import com.ideiassertiva.FypMatch.model.*

/**
 * Perfis de demonstração — usados como fallback quando o Firestore ainda
 * não tem usuários reais (ambiente de testes / beta sem backend populado).
 * Em produção com usuários reais, este fallback não é acionado.
 */
object MockProfiles {

    val profiles: List<User> = listOf(
        User(
            id = "mock_marina",
            displayName = "Marina",
            email = "marina@demo.fypmatch.com",
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS,
            profile = UserProfile(
                fullName = "Marina Alves",
                age = 27,
                bio = "Designer de dia, ilustradora de madrugada. Procuro alguém pra dividir playlists e silêncios confortáveis.",
                aboutMe = "Sou tranquila, gosto de conversas profundas e de café forte. Ansiedade faz parte, mas tô aprendendo a lidar.",
                photos = listOf(
                    "https://randomuser.me/api/portraits/women/68.jpg",
                    "https://randomuser.me/api/portraits/women/65.jpg"
                ),
                location = Location(city = "São Paulo", state = "SP"),
                gender = Gender.FEMALE,
                orientation = Orientation.BISEXUAL,
                intention = Intention.DATING,
                interests = listOf("Arte", "Música indie", "Café", "Cinema"),
                profession = "Designer Gráfica",
                height = 165,
                zodiacSign = ZodiacSign.PISCES,
                petPreference = PetPreference.LOVE_PETS,
                enneagramType = "Tipo 4 — O Individualista",
                personalityArchetype = "O Criativo",
                loveLanguage = "Tempo de qualidade",
                isProfileComplete = true
            )
        ),
        User(
            id = "mock_rafael",
            displayName = "Rafael",
            email = "rafael@demo.fypmatch.com",
            subscription = SubscriptionStatus.PREMIUM,
            accessLevel = AccessLevel.FULL_ACCESS,
            profile = UserProfile(
                fullName = "Rafael Costa",
                age = 30,
                bio = "Chef de cozinha. Te conquisto com um risoto. Bora viajar comendo por aí?",
                aboutMe = "Extrovertido, adoro receber gente em casa. Domingo é dia de almoço longo com vinho.",
                photos = listOf(
                    "https://randomuser.me/api/portraits/men/32.jpg",
                    "https://randomuser.me/api/portraits/men/36.jpg"
                ),
                location = Location(city = "Rio de Janeiro", state = "RJ"),
                gender = Gender.MALE,
                orientation = Orientation.GAY,
                intention = Intention.DATING,
                interests = listOf("Gastronomia", "Viagens", "Vinhos", "Praia"),
                profession = "Chef de Cozinha",
                height = 180,
                zodiacSign = ZodiacSign.LEO,
                petPreference = PetPreference.LIKE_PETS,
                enneagramType = "Tipo 2 — O Prestativo",
                personalityArchetype = "O Anfitrião",
                loveLanguage = "Atos de serviço",
                isProfileComplete = true
            )
        ),
        User(
            id = "mock_bia",
            displayName = "Bia",
            email = "bia@demo.fypmatch.com",
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS,
            profile = UserProfile(
                fullName = "Bia Mendes",
                age = 24,
                bio = "Dev, gamer e neurodivergente orgulhose. Me explica seu hiperfoco que eu explico o meu.",
                aboutMe = "Autista, comunicação direta é meu amor. Prefiro mensagem a ligação. Memes são linguagem oficial.",
                photos = listOf(
                    "https://randomuser.me/api/portraits/women/44.jpg",
                    "https://randomuser.me/api/portraits/women/90.jpg"
                ),
                location = Location(city = "Belo Horizonte", state = "MG"),
                gender = Gender.NON_BINARY,
                orientation = Orientation.PANSEXUAL,
                intention = Intention.FRIENDSHIP,
                interests = listOf("Games", "Tecnologia", "Anime", "Board games"),
                profession = "Desenvolvedora",
                height = 170,
                zodiacSign = ZodiacSign.AQUARIUS,
                petPreference = PetPreference.LOVE_PETS,
                enneagramType = "Tipo 5 — O Investigador",
                personalityArchetype = "O Sábio",
                loveLanguage = "Palavras de afirmação",
                isProfileComplete = true
            )
        ),
        User(
            id = "mock_camila",
            displayName = "Camila",
            email = "camila@demo.fypmatch.com",
            subscription = SubscriptionStatus.VIP,
            accessLevel = AccessLevel.FULL_ACCESS,
            profile = UserProfile(
                fullName = "Camila Rocha",
                age = 29,
                bio = "Médica e maratonista. Energia de sobra pra quem topar acordar cedo no fim de semana.",
                aboutMe = "Disciplinada e otimista. Valorizo honestidade acima de tudo. Corrida é minha terapia.",
                photos = listOf(
                    "https://randomuser.me/api/portraits/women/12.jpg",
                    "https://randomuser.me/api/portraits/women/19.jpg"
                ),
                location = Location(city = "Porto Alegre", state = "RS"),
                gender = Gender.FEMALE,
                orientation = Orientation.LESBIAN,
                intention = Intention.DATING,
                interests = listOf("Corrida", "Saúde", "Trilhas", "Culinária saudável"),
                profession = "Médica",
                height = 172,
                zodiacSign = ZodiacSign.CAPRICORN,
                petPreference = PetPreference.LIKE_PETS,
                enneagramType = "Tipo 3 — O Realizador",
                personalityArchetype = "A Atleta",
                loveLanguage = "Contato físico",
                isProfileComplete = true
            )
        ),
        User(
            id = "mock_lucas",
            displayName = "Lucas",
            email = "lucas@demo.fypmatch.com",
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS,
            profile = UserProfile(
                fullName = "Lucas Ferreira",
                age = 26,
                bio = "Músico e leitor compulsivo. Te mando indireta em forma de música. Vamos trocar livros?",
                aboutMe = "Introvertido que recarrega tocando violão. Gosto de planos tranquilos: livraria, café, conversa boa.",
                photos = listOf(
                    "https://randomuser.me/api/portraits/men/75.jpg",
                    "https://randomuser.me/api/portraits/men/79.jpg"
                ),
                location = Location(city = "Curitiba", state = "PR"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                interests = listOf("Música", "Literatura", "Café", "Vinil"),
                profession = "Músico",
                height = 178,
                zodiacSign = ZodiacSign.LIBRA,
                petPreference = PetPreference.LOVE_PETS,
                enneagramType = "Tipo 9 — O Pacificador",
                personalityArchetype = "O Sonhador",
                loveLanguage = "Presentes",
                isProfileComplete = true
            )
        )
    )
}
