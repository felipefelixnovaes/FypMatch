package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.ideiassertiva.FypMatch.model.User

internal fun DocumentSnapshot.toFypUserOrNull(userId: String = id): User? {
    return try {
        toObject(User::class.java)?.copy(id = userId)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        null
    }
}
