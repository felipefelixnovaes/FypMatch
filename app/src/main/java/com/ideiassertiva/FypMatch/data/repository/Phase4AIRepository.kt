package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertivas.FypMatch.model.BehaviorAnalyzer
import com.ideiassertivas.FypMatch.model.CompatibilityMLEngine
import com.ideiassertivas.FypMatch.model.CompatibilityScore
import com.ideiassertivas.FypMatch.model.NeuroPreferences
import com.ideiassertivas.FypMatch.model.NeuroProfile
import com.ideiassertivas.FypMatch.model.PersonalityAnalyzer
import com.ideiassertivas.FypMatch.model.PersonalityProfile
import com.ideiassertivas.FypMatch.model.PersonalityTraits
import com.ideiassertivas.FypMatch.model.SwipeBehavior
import com.ideiassertivas.FypMatch.model.SwipeRecord
import com.ideiassertivas.FypMatch.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Phase4AIRepository @Inject constructor() {

    private val personalityAnalyzer = PersonalityAnalyzer()
    private val compatibilityEngine = CompatibilityMLEngine()

    /**
     * Analisa personalidade a partir de traits do usuário.
     * PersonalityAnalyzer.analyzePersonality recebe (Any?, List<Any>) e retorna PersonalityProfile.
     */
    suspend fun analyzePersonality(
        userId: String,
        traits: PersonalityTraits
    ): Result<PersonalityProfile> {
        return try {
            val profile = personalityAnalyzer.analyzePersonality(traits, emptyList<Any>())
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calcula compatibilidade entre dois usuários usando o CompatibilityMLEngine.
     */
    suspend fun analyzeCompatibility(
        user1Id: String,
        user2Id: String,
        user1: User?,
        user2: User?
    ): Result<CompatibilityScore> {
        return try {
            val score = compatibilityEngine.analyzeCompatibility(user1, user2)
            Result.success(score)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analisa padrões de comportamento de swipe do usuário.
     */
    suspend fun analyzeSwipeBehavior(
        userId: String,
        swipeHistory: List<SwipeRecord>
    ): Result<SwipeBehavior> {
        return try {
            val analyzer = BehaviorAnalyzer()
            val behavior = analyzer.analyzeSwipeBehavior(userId, swipeHistory)
            Result.success(behavior)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cria um NeuroProfile baseado nas traits de personalidade.
     * Usa NeuroPreferences conforme a definição real do modelo.
     */
    suspend fun createNeuroProfile(
        userId: String,
        traits: PersonalityTraits
    ): Result<NeuroProfile> {
        return try {
            val profile = NeuroProfile(
                preferences = NeuroPreferences(
                    needsClearCommunication = traits.openness > 0.7f,
                    prefersDirectness = traits.conscientiousness > 0.6f,
                    sensitiveToCriticism = traits.neuroticism > 0.6f,
                    needsRoutine = traits.conscientiousness > 0.7f,
                    prefersTextOverVoice = traits.extraversion < 0.4f
                )
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retorna personalidade em cache ou analisa na hora.
     */
    suspend fun getOrAnalyzePersonality(
        userId: String,
        userProfile: Any?
    ): Result<PersonalityProfile> {
        return try {
            val profile = personalityAnalyzer.analyzePersonality(userProfile, emptyList<Any>())
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analisa personalidades em lote (retorna mapa userId -> PersonalityProfile).
     */
    fun batchAnalyzePersonalities(userProfiles: List<User>): Map<String, PersonalityProfile> {
        return userProfiles.associate { user ->
            user.id to try {
                personalityAnalyzer.analyzePersonality(user, emptyList<Any>())
            } catch (e: Exception) {
                PersonalityProfile()
            }
        }
    }
}
