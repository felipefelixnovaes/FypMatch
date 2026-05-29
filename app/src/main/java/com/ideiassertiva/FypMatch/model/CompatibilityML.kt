package com.ideiassertiva.FypMatch.model

// FIXME: BehaviorAnalyzer rewritten as stubs — SwipeRecord/SwipeAction models missing

data class CompatibilityScore(
    val overall: Float,
    val factors: List<CompatibilityFactor>,
    val personalityMatch: Float = 0.5f,
    val communicationMatch: Float = 0.5f
)

data class CompatibilityFactor(
    val name: String,
    val score: Float,
    val description: String = ""
)

data class SwipeBehavior(
    val userId: String,
    val swipeHistory: List<Any>,
    val patterns: SwipePatterns,
    val preferences: Any?
)

data class SwipePatterns(
    val averageSwipeTime: Long,
    val likeRate: Float,
    val agePreferencePattern: AgePattern,
    val distancePattern: DistancePattern,
    val photoPreferences: PhotoPreferences,
    val bioReadingBehavior: BioReadingBehavior
)

data class AgePattern(
    val minAge: Int,
    val maxAge: Int,
    val mostLikedAgeRange: IntRange,
    val intensity: Float
)

data class DistancePattern(
    val minDistance: Float,
    val maxDistance: Float,
    val averageDistance: Float
)

data class PhotoPreferences(
    val prefersPhotos: Boolean,
    val prefersFullBody: Boolean,
    val prefersSelfies: Boolean,
    val prefersGroupPhotos: Boolean,
    val prefersProfessionalPhotos: Boolean
)

data class BioReadingBehavior(
    val averageReadingTime: Long,
    val readsFullBio: Boolean,
    val keywords: List<String>,
    val preferredTopics: List<String>
)

data class PersonalityProfile(
    val mbtiType: String = "INFP",
    val confidence: Float = 0.7f,
    val traits: PersonalityTraits = PersonalityTraits(),
    val communicationStyle: CommunicationStyle = CommunicationStyle()
)

data class CommunicationStyle(
    val emotionality: Float = 0.5f,
    val directness: Float = 0.5f,
    val formality: Float = 0.5f
)

// Behavior analysis for learning
class BehaviorAnalyzer {
    
    fun analyzeSwipeBehavior(userId: String, swipeHistory: List<Any>): SwipeBehavior {
        return SwipeBehavior(
            userId = userId,
            swipeHistory = emptyList(),
            patterns = SwipePatterns(3000L, 0.5f, AgePattern(18, 35, 22..28, 0.3f), DistancePattern(10f, 25f, 15f), PhotoPreferences(false, false, false, false, false), BioReadingBehavior(0L, false, emptyList(), emptyList())),
            preferences = null
        )
    }
}

class CompatibilityMLEngine {

    // Sobrecarga legada — mantida por compatibilidade
    fun analyzeCompatibility(userId: String, targetId: String, currentUser: Any?, targetUser: Any?): CompatibilityScore {
        return analyzeCompatibility(currentUser as? User, targetUser as? User)
    }

    // Engine real — algoritmo de 6 dimensões ponderadas
    fun analyzeCompatibility(currentUser: User?, targetUser: User?): CompatibilityScore {
        if (currentUser == null || targetUser == null) {
            return CompatibilityScore(overall = 0.5f, factors = emptyList())
        }

        val f1 = interestsScore(currentUser, targetUser)
        val f2 = intentionsScore(currentUser, targetUser)
        val f3 = lifestyleScore(currentUser, targetUser)
        val f4 = ageScore(currentUser, targetUser)
        val f5 = valuesScore(currentUser, targetUser)
        val f6 = activityBonus(targetUser)

        val weighted = f1.score * 0.30f + f2.score * 0.25f + f3.score * 0.20f +
                       f4.score * 0.10f + f5.score * 0.10f + f6.score * 0.05f
        val overall = (weighted / 100f).coerceIn(0f, 1f)

        return CompatibilityScore(
            overall = overall,
            factors = listOf(f1, f2, f3, f4, f5, f6),
            personalityMatch = f1.score / 100f,
            communicationMatch = f2.score / 100f
        )
    }

    // --- Dimensão 1: Interesses (30%) ---
    private fun interestsScore(a: User, b: User): CompatibilityFactor {
        val setA = (a.profile.interests + a.profile.hobbies).toSet()
        val setB = (b.profile.interests + b.profile.hobbies).toSet()
        val intersection = setA.intersect(setB).size
        val union = setA.union(setB).size
        val jaccard = if (union > 0) intersection.toFloat() / union.toFloat() else 0f
        val score = (jaccard * 100f).toInt().toFloat()
        val desc = when {
            score >= 70 -> "Vocês têm muitos interesses em comum!"
            score >= 40 -> "Alguns interesses compartilhados"
            else        -> "Interesses diferentes — ótimo para descobrir coisas novas"
        }
        return CompatibilityFactor("Interesses", score, desc)
    }

    // --- Dimensão 2: Intenções (25%) ---
    private fun intentionsScore(a: User, b: User): CompatibilityFactor {
        val score = if (a.profile.intention != Intention.NOT_SPECIFIED &&
                        b.profile.intention != Intention.NOT_SPECIFIED) {
            if (a.profile.intention == b.profile.intention) 100f else 30f
        } else 55f
        val desc = when {
            score >= 90 -> "Querem a mesma coisa — ótimo sinal! 💪"
            score >= 50 -> "Intenções parcialmente alinhadas"
            else        -> "Buscam coisas diferentes no momento"
        }
        return CompatibilityFactor("Intenções", score, desc)
    }

    // --- Dimensão 3: Estilo de vida (20%) ---
    private fun lifestyleScore(a: User, b: User): CompatibilityFactor {
        val smokingScore = lifestyleCompat(
            a.profile.smokingStatus.name, b.profile.smokingStatus.name, "NEVER", "REGULARLY"
        )
        val drinkingScore = lifestyleCompat(
            a.profile.drinkingStatus.name, b.profile.drinkingStatus.name, "NEVER", "REGULARLY"
        )
        val childrenScore = childrenCompat(a, b)
        val avg = ((smokingScore + drinkingScore + childrenScore) / 3f)
        val desc = when {
            avg >= 80 -> "Estilos de vida muito compatíveis"
            avg >= 50 -> "Diferenças de estilo de vida gerenciáveis"
            else      -> "Alguns hábitos podem precisar de conversa"
        }
        return CompatibilityFactor("Estilo de vida", avg, desc)
    }

    private fun lifestyleCompat(a: String, b: String, neutral: String, dealbreaker: String): Float {
        if (a == b) return 100f
        if ((a == neutral && b == dealbreaker) || (b == neutral && a == dealbreaker)) return 20f
        return 60f
    }

    private fun childrenCompat(a: User, b: User): Float {
        val aHas = a.profile.hasChildren
        val bHas = b.profile.hasChildren
        val aWants = a.profile.wantsChildren
        val bWants = b.profile.wantsChildren
        return when {
            aHas == ChildrenStatus.YES && bHas == ChildrenStatus.YES -> 80f
            aHas == ChildrenStatus.NO  && bHas == ChildrenStatus.NO  -> 90f
            aWants == bWants && aWants != ChildrenStatus.NOT_SPECIFIED -> 85f
            (aHas == ChildrenStatus.YES && bHas == ChildrenStatus.NO) ||
            (aHas == ChildrenStatus.NO  && bHas == ChildrenStatus.YES) -> 30f
            else -> 60f
        }
    }

    // --- Dimensão 4: Idade (10%) ---
    private fun ageScore(a: User, b: User): CompatibilityFactor {
        val aInB = b.preferences.ageRange.contains(a.profile.age)
        val bInA = a.preferences.ageRange.contains(b.profile.age)
        val score = when {
            aInB && bInA -> 100f
            aInB || bInA -> 60f
            else          -> 20f
        }
        val desc = when {
            score >= 90 -> "Faixa etária ideal para os dois"
            score >= 50 -> "Uma das partes está fora da faixa preferida"
            else        -> "Fora da faixa etária preferida"
        }
        return CompatibilityFactor("Faixa etária", score, desc)
    }

    // --- Dimensão 5: Valores (10%) ---
    private fun valuesScore(a: User, b: User): CompatibilityFactor {
        val langA = a.profile.languages.toSet()
        val langB = b.profile.languages.toSet()
        val langScore = if (langA.union(langB).isNotEmpty())
            langA.intersect(langB).size.toFloat() / langA.union(langB).size.toFloat()
        else 1f

        val relScore = when {
            a.profile.religion == Religion.NOT_SPECIFIED ||
            b.profile.religion == Religion.NOT_SPECIFIED -> 0.7f
            a.profile.religion == b.profile.religion     -> 1.0f
            else                                          -> 0.4f
        }

        val combined = ((langScore * 0.6f + relScore * 0.4f) * 100f)
        val desc = if (combined >= 70) "Valores e origem cultural próximos" else "Backgrounds culturais diferentes"
        return CompatibilityFactor("Valores", combined, desc)
    }

    // --- Dimensão 6: Bônus de atividade (5%) ---
    private fun activityBonus(target: User): CompatibilityFactor {
        val isOnline = (Date().time - target.lastActive.time) < 5 * 60 * 1000L
        val isProfileComplete = target.profile.isProfileComplete
        val hasPhotos = target.profile.photos.size >= 3

        var score = 0f
        if (isOnline)           score += 60f
        if (isProfileComplete)  score += 20f
        if (hasPhotos)          score += 20f

        val desc = if (isOnline) "Online agora — ótimo momento para curtir!" else "Visto recentemente"
        return CompatibilityFactor("Atividade", score, desc)
    }
}

class PersonalityAnalyzer {
    fun analyzePersonality(currentUser: Any?, mockMessages: List<Any>): PersonalityProfile {
        return PersonalityProfile()
    }
}