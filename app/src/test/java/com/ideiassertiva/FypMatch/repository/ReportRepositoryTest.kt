package com.ideiassertiva.FypMatch.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.ideiassertiva.FypMatch.models.Report
import com.ideiassertiva.FypMatch.models.ReportReason
import com.ideiassertiva.FypMatch.repositories.ReportRepository
import io.mockk.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReportRepositoryTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: ReportRepository

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        firestore = mockk(relaxed = true)
        repository = ReportRepository(firestore)
    }

    @After
    fun tearDown() { clearAllMocks() }

    @Test
    fun `submitReport deve retornar sucesso`() = runTest {
        val mockDoc = mockk<DocumentReference>(relaxed = true)
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { firestore.collection("reports") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDoc
        every { mockDoc.set(any<Report>()) } returns mockTask
        coEvery { mockTask.await() } returns mockk()

        val report = Report(reporterUserId = "u1", reportedUserId = "u2", reason = ReportReason.HARASSMENT)
        val result = repository.submitReport(report)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `blockUser deve retornar sucesso`() = runTest {
        val mockDoc = mockk<DocumentReference>(relaxed = true)
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockTask = mockk<Task<Void>>(relaxed = true)
        val mockUserDoc = mockk<DocumentReference>(relaxed = true)
        val mockUserCollection = mockk<CollectionReference>(relaxed = true)

        every { firestore.collection("users") } returns mockUserCollection
        every { mockUserCollection.document("u1") } returns mockUserDoc
        every { mockUserDoc.collection("blocked") } returns mockCollection
        every { mockCollection.document("u2") } returns mockDoc
        every { mockDoc.set(any()) } returns mockTask
        coEvery { mockTask.await() } returns mockk()

        val result = repository.blockUser("u1", "u2")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `isUserBlocked retorna false quando nao bloqueado`() = runTest {
        val mockSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
        val mockDoc = mockk<DocumentReference>(relaxed = true)
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockUserDoc = mockk<DocumentReference>(relaxed = true)
        val mockUserCollection = mockk<CollectionReference>(relaxed = true)

        every { mockSnapshot.exists() } returns false
        every { firestore.collection("users") } returns mockUserCollection
        every { mockUserCollection.document("u1") } returns mockUserDoc
        every { mockUserDoc.collection("blocked") } returns mockCollection
        every { mockCollection.document("u2") } returns mockDoc
        every { mockDoc.get() } returns mockTask
        coEvery { mockTask.await() } returns mockSnapshot

        val result = repository.isUserBlocked("u1", "u2")
        assertFalse(result)
    }
}
