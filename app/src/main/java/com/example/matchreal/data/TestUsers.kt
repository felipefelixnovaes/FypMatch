package com.example.matchreal.data

import com.example.matchreal.model.*
import java.util.Date

object TestUsers {
    
    fun getTestUsers(): List<User> = listOf(
        // 1. Ana Clara - Designer UX/UI
        User(
            id = "user_001",
            email = "ana.clara@email.com",
            displayName = "Ana Clara",
            profile = UserProfile(
                fullName = "Ana Clara Santos",
                age = 26,
                bio = "Designer UX/UI apaixonada por criar experiências incríveis. Amo café, filmes indie e viagens. Procuro alguém para compartilhar aventuras! ☕🎬✈️",
                location = Location(city = "São Paulo", state = "SP"),
                gender = Gender.FEMALE,
                orientation = Orientation.BISEXUAL,
                intention = Intention.DATING,
                interests = listOf("Design", "Cinema", "Viagens", "Café", "Arte", "Fotografia"),
                education = "Superior Completo - Design",
                profession = "Designer UX/UI",
                height = 165,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.PREMIUM,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 2. Bruno Costa - Desenvolvedor
        User(
            id = "user_002", 
            email = "bruno.costa@email.com",
            displayName = "Bruno",
            profile = UserProfile(
                fullName = "Bruno Costa",
                age = 29,
                bio = "Dev full-stack que ama tecnologia e inovação. Nas horas vagas, jogo videogame e faço trilhas. Buscando alguém para dividir código e risadas! 💻🎮🥾",
                location = Location(city = "Rio de Janeiro", state = "RJ"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                interests = listOf("Programação", "Games", "Trilhas", "Tecnologia", "Música", "Filmes"),
                education = "Superior Completo - Ciência da Computação",
                profession = "Desenvolvedor Full-Stack",
                height = 178,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 3. Carlos Eduardo - Professor
        User(
            id = "user_003",
            email = "carlos.eduardo@email.com", 
            displayName = "Carlos",
            profile = UserProfile(
                fullName = "Carlos Eduardo Oliveira",
                age = 35,
                bio = "Professor de história que adora contar histórias. Pai de um golden retriever chamado Beethoven. Procuro conexões verdadeiras e boas conversas! 📚🐕❤️",
                location = Location(city = "Belo Horizonte", state = "MG"),
                gender = Gender.MALE,
                orientation = Orientation.GAY,
                intention = Intention.DATING,
                interests = listOf("História", "Livros", "Cães", "Música Clássica", "Teatro", "Culinária"),
                education = "Pós-graduação - História",
                profession = "Professor",
                height = 172,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.VIP,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 4. Daniela Ferreira - Psicóloga
        User(
            id = "user_004",
            email = "daniela.ferreira@email.com",
            displayName = "Dani",
            profile = UserProfile(
                fullName = "Daniela Ferreira",
                age = 31,
                bio = "Psicóloga clínica que acredita no poder das conexões humanas. Yoga, meditação e natureza são meus refúgios. Busco alguém para crescer junto! 🧘‍♀️🌱💕",
                location = Location(city = "Florianópolis", state = "SC"),
                gender = Gender.FEMALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                interests = listOf("Psicologia", "Yoga", "Meditação", "Natureza", "Livros", "Wellness"),
                education = "Superior Completo - Psicologia",
                profession = "Psicóloga",
                height = 168,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.PREMIUM,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 5. Emerson Silva - Músico
        User(
            id = "user_005",
            email = "emerson.silva@email.com",
            displayName = "Emerson",
            profile = UserProfile(
                fullName = "Emerson Silva",
                age = 24,
                bio = "Músico indie que vive entre acordes e melodias. Toco guitarra, baixo e um pouco de tudo. Procuro alguém para harmonizar comigo! 🎸🎵✨",
                location = Location(city = "Porto Alegre", state = "RS"),
                gender = Gender.MALE,
                orientation = Orientation.PANSEXUAL,
                intention = Intention.CASUAL,
                interests = listOf("Música", "Guitarra", "Bandas Indie", "Festivais", "Arte", "Boemia"),
                education = "Superior Incompleto - Música",
                profession = "Músico",
                height = 175,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 6. Fernanda Lopes - Médica
        User(
            id = "user_006",
            email = "fernanda.lopes@email.com",
            displayName = "Fernanda",
            profile = UserProfile(
                fullName = "Fernanda Lopes",
                age = 33,
                bio = "Médica pediatra com um coração gigante. Amo crianças, animais e causas sociais. Nas folgas, pratico corrida e leio muito. Busco amor genuíno! 👩‍⚕️🏃‍♀️📖",
                location = Location(city = "Recife", state = "PE"),
                gender = Gender.FEMALE,
                orientation = Orientation.LESBIAN,
                intention = Intention.DATING,
                interests = listOf("Medicina", "Corrida", "Leitura", "Voluntariado", "Animais", "Viagens"),
                education = "Pós-graduação - Medicina",
                profession = "Médica Pediatra",
                height = 162,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.VIP,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 7. Gabriel Santos - Engenheiro
        User(
            id = "user_007",
            email = "gabriel.santos@email.com",
            displayName = "Gabriel",
            profile = UserProfile(
                fullName = "Gabriel Santos",
                age = 28,
                bio = "Engenheiro civil que constrói pontes, literalmente! Amo esportes, cerveja artesanal e churrascos. Procuro alguém para construir algo especial! 🏗️🍺⚽",
                location = Location(city = "Brasília", state = "DF"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                interests = listOf("Engenharia", "Futebol", "Cerveja", "Churrasco", "Arquitetura", "Esportes"),
                education = "Superior Completo - Engenharia Civil",
                profession = "Engenheiro Civil",
                height = 183,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 8. Helena Martins - Artista
        User(
            id = "user_008",
            email = "helena.martins@email.com",
            displayName = "Helena",
            profile = UserProfile(
                fullName = "Helena Martins",
                age = 27,
                bio = "Artista visual que vê magia em tudo. Pintura, escultura e performance são minha linguagem. Vegetariana, amante da lua e dos gatos. Busco alma gêmea! 🎨🌙🐱",
                location = Location(city = "Salvador", state = "BA"),
                gender = Gender.FEMALE,
                orientation = Orientation.BISEXUAL,
                intention = Intention.DATING,
                interests = listOf("Arte", "Pintura", "Gatos", "Vegetarianismo", "Lua", "Espiritualidade"),
                education = "Superior Completo - Artes Visuais",
                profession = "Artista Visual",
                height = 170,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.PREMIUM,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 9. Igor Pereira - Chef
        User(
            id = "user_009",
            email = "igor.pereira@email.com",
            displayName = "Igor",
            profile = UserProfile(
                fullName = "Igor Pereira",
                age = 30,
                bio = "Chef que tempera a vida com sabor e paixão. Cozinho com amor e sempre invento receitas novas. Busco alguém para dividir a mesa e o coração! 👨‍🍳🍷❤️",
                location = Location(city = "Curitiba", state = "PR"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                interests = listOf("Culinária", "Vinhos", "Gastronomia", "Viagens", "Mercados", "Restaurantes"),
                education = "Superior Completo - Gastronomia",
                profession = "Chef de Cozinha",
                height = 177,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.VIP,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 10. Julia Costa - Jornalista
        User(
            id = "user_010",
            email = "julia.costa@email.com",
            displayName = "Julia",
            profile = UserProfile(
                fullName = "Julia Costa",
                age = 25,
                bio = "Jornalista que conta histórias que importam. Curiosa, questionadora e sempre em busca da verdade. Amo café, livros e debates intensos! ☕📰💭",
                location = Location(city = "Fortaleza", state = "CE"),
                gender = Gender.FEMALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.FRIENDSHIP,
                interests = listOf("Jornalismo", "Política", "Café", "Livros", "Debates", "Sociedade"),
                education = "Superior Completo - Jornalismo",
                profession = "Jornalista",
                height = 163,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 11. Kai Nakamura - Game Designer
        User(
            id = "user_011",
            email = "kai.nakamura@email.com",
            displayName = "Kai",
            profile = UserProfile(
                fullName = "Kai Nakamura",
                age = 23,
                bio = "Game designer não-binário que cria mundos virtuais. Anime, RPG e cultura japonesa são minha vida. Procuro conexões autênticas e level up no amor! 🎮🌸⚡",
                location = Location(city = "São Paulo", state = "SP"),
                gender = Gender.NON_BINARY,
                orientation = Orientation.PANSEXUAL,
                intention = Intention.DATING,
                interests = listOf("Games", "Anime", "RPG", "Cultura Japonesa", "Design", "Tecnologia"),
                education = "Superior Completo - Design de Games",
                profession = "Game Designer",
                height = 168,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.PREMIUM,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 12. Lucas Almeida - Veterinário
        User(
            id = "user_012",
            email = "lucas.almeida@email.com",
            displayName = "Lucas",
            profile = UserProfile(
                fullName = "Lucas Almeida",
                age = 32,
                bio = "Veterinário apaixonado por todos os bichinhos. Tenho 3 gatos resgatados e um coração imenso. Procuro alguém que ame animais tanto quanto eu! 🐾❤️🐱",
                location = Location(city = "Goiânia", state = "GO"),
                gender = Gender.MALE,
                orientation = Orientation.GAY,
                intention = Intention.DATING,
                interests = listOf("Veterinária", "Animais", "Resgate", "Natureza", "Caminhadas", "Proteção Animal"),
                education = "Superior Completo - Veterinária",
                profession = "Veterinário",
                height = 174,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 13. Mariana Silva - Advogada
        User(
            id = "user_013",
            email = "mariana.silva@email.com",
            displayName = "Mari",
            profile = UserProfile(
                fullName = "Mariana Silva",
                age = 29,
                bio = "Advogada que luta por justiça social. Feminista, ativista e sonhadora. Amo teatro, dança e conversas profundas. Busco parceria para mudar o mundo! ⚖️💃🌍",
                location = Location(city = "Rio de Janeiro", state = "RJ"),
                gender = Gender.FEMALE,
                orientation = Orientation.BISEXUAL,
                intention = Intention.DATING,
                interests = listOf("Direito", "Ativismo", "Teatro", "Dança", "Feminismo", "Política"),
                education = "Pós-graduação - Direito",
                profession = "Advogada",
                height = 166,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.VIP,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 14. Nicolas Ferreira - Fotógrafo
        User(
            id = "user_014",
            email = "nicolas.ferreira@email.com",
            displayName = "Nicolas",
            profile = UserProfile(
                fullName = "Nicolas Ferreira",
                age = 26,
                bio = "Fotógrafo que captura momentos únicos. Viajo o mundo em busca da foto perfeita. Skate, surf e aventuras são meu estilo. Vamos criar memórias? 📸🏄‍♂️🛹",
                location = Location(city = "Florianópolis", state = "SC"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.CASUAL,
                interests = listOf("Fotografia", "Viagens", "Surf", "Skate", "Aventura", "Natureza"),
                education = "Superior Completo - Fotografia",
                profession = "Fotógrafo",
                height = 180,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.PREMIUM,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 15. Olivia Santos - Nutricionista
        User(
            id = "user_015",
            email = "olivia.santos@email.com",
            displayName = "Olivia",
            profile = UserProfile(
                fullName = "Olivia Santos",
                age = 34,
                bio = "Nutricionista que acredita em alimentação como medicina. Vegana, pratico crossfit e amo smoothies. Busco alguém para dividir um estilo de vida saudável! 🥗💪🌱",
                location = Location(city = "Belo Horizonte", state = "MG"),
                gender = Gender.FEMALE,
                orientation = Orientation.LESBIAN,
                intention = Intention.DATING,
                interests = listOf("Nutrição", "CrossFit", "Veganismo", "Saúde", "Smoothies", "Bem-estar"),
                education = "Pós-graduação - Nutrição",
                profession = "Nutricionista",
                height = 171,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.VIP,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 16. Pedro Rodrigues - Arquiteto
        User(
            id = "user_016",
            email = "pedro.rodrigues@email.com",
            displayName = "Pedro",
            profile = UserProfile(
                fullName = "Pedro Rodrigues",
                age = 37,
                bio = "Arquiteto que desenha sonhos e constrói futuros. Minimalista, amante de bom design e café especial. Procuro alguém para projetar uma vida a dois! ☕🏛️💫",
                location = Location(city = "São Paulo", state = "SP"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                interests = listOf("Arquitetura", "Design", "Café", "Minimalismo", "Arte", "Urbanismo"),
                education = "Pós-graduação - Arquitetura",
                profession = "Arquiteto",
                height = 179,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 17. Quinn Taylor - Tradutor/a
        User(
            id = "user_017",
            email = "quinn.taylor@email.com",
            displayName = "Quinn",
            profile = UserProfile(
                fullName = "Quinn Taylor",
                age = 28,
                bio = "Tradutor/a que navega entre idiomas e culturas. Fluente em 5 línguas, amo literatura e viagens. Demissexual, busco conexão intelectual primeiro! 📚🌍💭",
                location = Location(city = "Porto Alegre", state = "RS"),
                gender = Gender.NON_BINARY,
                orientation = Orientation.DEMISEXUAL,
                intention = Intention.FRIENDSHIP,
                interests = listOf("Idiomas", "Literatura", "Tradução", "Culturas", "Viagens", "Filosofia"),
                education = "Superior Completo - Letras/Tradução",
                profession = "Tradutor/a",
                height = 167,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.PREMIUM,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 18. Renata Lima - Fisioterapeuta
        User(
            id = "user_018",
            email = "renata.lima@email.com",
            displayName = "Renata",
            profile = UserProfile(
                fullName = "Renata Lima",
                age = 30,
                bio = "Fisioterapeuta que ajuda pessoas a se moverem melhor. Pilates, natação e vida ativa são essenciais. Busco alguém para dançar pela vida! 💃🏊‍♀️🤸‍♀️",
                location = Location(city = "Recife", state = "PE"),
                gender = Gender.FEMALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                interests = listOf("Fisioterapia", "Pilates", "Natação", "Dança", "Saúde", "Movimento"),
                education = "Superior Completo - Fisioterapia",
                profession = "Fisioterapeuta",
                height = 164,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 19. Samuel Oliveira - Barista
        User(
            id = "user_019",
            email = "samuel.oliveira@email.com",
            displayName = "Samuel",
            profile = UserProfile(
                fullName = "Samuel Oliveira",
                age = 22,
                bio = "Barista apaixonado por cada grão de café. Latte art é minha forma de arte. Amo música indie, livros e conversas até tarde. Vamos tomar um café? ☕🎨📚",
                location = Location(city = "Curitiba", state = "PR"),
                gender = Gender.MALE,
                orientation = Orientation.BISEXUAL,
                intention = Intention.CASUAL,
                interests = listOf("Café", "Latte Art", "Música Indie", "Livros", "Arte", "Conversas"),
                education = "Ensino Médio",
                profession = "Barista",
                height = 176,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.FREE,
            accessLevel = AccessLevel.FULL_ACCESS
        ),
        
        // 20. Valentina Rocha - Bióloga
        User(
            id = "user_020",
            email = "valentina.rocha@email.com",
            displayName = "Val",
            profile = UserProfile(
                fullName = "Valentina Rocha",
                age = 27,
                bio = "Bióloga marinha apaixonada pelos oceanos. Mergulho, pesquisa e conservação são minha vida. Surfo nas horas vagas e sonho em salvar o planeta! 🌊🐠🏄‍♀️",
                location = Location(city = "Vitória", state = "ES"),
                gender = Gender.FEMALE,
                orientation = Orientation.PANSEXUAL,
                intention = Intention.DATING,
                interests = listOf("Biologia Marinha", "Mergulho", "Surf", "Conservação", "Oceanos", "Pesquisa"),
                education = "Pós-graduação - Biologia Marinha",
                profession = "Bióloga Marinha",
                height = 169,
                isProfileComplete = true
            ),
            subscription = SubscriptionStatus.VIP,
            accessLevel = AccessLevel.FULL_ACCESS
        )
    )
}
