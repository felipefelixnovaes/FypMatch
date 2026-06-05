package com.ideiassertiva.FypMatch.data

/**
 * Catálogo de interesses selecionáveis, agrupados por categoria.
 * Usado na edição de perfil para padronizar os interesses (melhora o match).
 */
object InterestCatalog {

    val categories: Map<String, List<String>> = linkedMapOf(
        "Esportes" to listOf(
            "Futebol", "Corrida", "Academia", "Yoga", "Natação",
            "Ciclismo", "Vôlei", "Tênis", "Surf", "Escalada", "Pilates"
        ),
        "Música" to listOf(
            "Rock", "Pop", "MPB", "Sertanejo", "Eletrônica",
            "Jazz", "Funk", "Indie", "Clássica", "Rap", "Reggae"
        ),
        "Arte & Cultura" to listOf(
            "Cinema", "Teatro", "Museus", "Fotografia", "Pintura",
            "Literatura", "Poesia", "Dança", "Shows"
        ),
        "Gastronomia" to listOf(
            "Cozinhar", "Vinhos", "Cafés", "Comida japonesa",
            "Veganismo", "Confeitaria", "Cervejas", "Churrasco"
        ),
        "Tecnologia & Games" to listOf(
            "Games", "Programação", "IA", "Gadgets",
            "Anime", "Realidade virtual", "Board games", "RPG"
        ),
        "Lifestyle" to listOf(
            "Viagens", "Natureza", "Praia", "Camping", "Pets",
            "Meditação", "Voluntariado", "Astrologia", "Plantas", "Moda"
        ),
        "Social" to listOf(
            "Festas", "Bares", "Networking", "Stand-up", "Karaokê", "Happy hour"
        )
    )
}
