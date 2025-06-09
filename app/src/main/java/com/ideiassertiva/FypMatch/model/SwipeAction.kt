package com.ideiassertiva.FypMatch.model

enum class SwipeAction {
    LIKE,    // Curtir (swipe direita)
    PASS,    // Passar (swipe esquerda)
    SUPER_LIKE // Super like (swipe para cima)
}

enum class SwipeDirection {
    LEFT,
    RIGHT,
    UP,
    NONE
}

data class SwipeState(
    val direction: SwipeDirection = SwipeDirection.NONE,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Float = 0f,
    val alpha: Float = 1f,
    val isAnimating: Boolean = false
)

data class SwipeResult(
    val userId: String,
    val action: SwipeAction,
    val isMatch: Boolean = false
) 
