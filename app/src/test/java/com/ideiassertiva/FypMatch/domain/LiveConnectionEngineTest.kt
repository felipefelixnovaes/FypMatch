package com.ideiassertiva.FypMatch.domain

import com.ideiassertiva.FypMatch.data.repository.LiveConnectionDataSource
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeLiveConnectionDataSource : LiveConnectionDataSource {
    var connection: LiveConnection = LiveConnection(
        id = "conn_123",
        dimensions = ConnectionDimensions(affinity = 10f, lightness = 5f, continuity = 10f)
    )
    val loggedEvents = mutableListOf<ConnectionEvent>()

    override suspend fun getOrCreateLiveConnection(
        matchId: String,
        user1Id: String,
        user2Id: String
    ): LiveConnection = connection

    override suspend fun getLiveConnection(liveConnectionId: String): LiveConnection? = connection

    override suspend fun updateConnectionScore(liveConnectionId: String, updatedConnection: LiveConnection) {
        connection = updatedConnection
    }

    override suspend fun logConnectionEvent(liveConnectionId: String, event: ConnectionEvent) {
        loggedEvents.add(event.copy(liveConnectionId = liveConnectionId))
    }
}

class LiveConnectionEngineTest {

    @Test
    fun `when GAME_PLAYED event occurs, affinity should increase and status should recalculate`() = runBlocking {
        val repo = FakeLiveConnectionDataSource()
        val engine = LiveConnectionEngine(repo)
        val event = ConnectionEvent(type = ConnectionEventType.GAME_PLAYED)

        engine.processNewEvent("conn_123", event)

        val updatedConnection = repo.connection
        assertEquals(15f, updatedConnection.dimensions.affinity)
        assertEquals(1, repo.loggedEvents.size)
    }

    @Test
    fun `when MESSAGE_SENT event occurs, organic continuity should increase`() = runBlocking {
        val repo = FakeLiveConnectionDataSource()
        val engine = LiveConnectionEngine(repo)
        val event = ConnectionEvent(type = ConnectionEventType.MESSAGE_SENT)

        engine.processNewEvent("conn_123", event)

        val updatedConnection = repo.connection
        assertTrue(updatedConnection.dimensions.continuity > 10f)
        assertTrue(updatedConnection.dimensions.initiative > 0f)
    }
}
