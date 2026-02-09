package com.example.todolist.data

import com.example.todolist.auth.AuthRepository
import com.example.todolist.domain.Todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreTodoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : TodoRepository {

    private val todosCollection = firestore.collection("todos")

    override suspend fun insert(title: String, description: String?, id: String?) {
        val currentUser = authRepository.currentUser ?: return

        try {
            val firestoreTodo = FirestoreTodo(
                title = title,
                description = description,
                isCompleted = false,
                userId = currentUser.uid,
                createdAt = Date(),
                updatedAt = Date()
            )

            if (id != null) {
                todosCollection.document(id).set(firestoreTodo).await()
            } else {
                todosCollection.add(firestoreTodo).await()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateCompleted(id: String, isCompleted: Boolean) {
        val currentUser = authRepository.currentUser ?: return

        try {

            val document = todosCollection.document(id).get().await()
            
            if (!document.exists()) {
                throw Exception("Todo document not found with id: $id")
            }
            
            val firestoreTodo = document.toObject(FirestoreTodo::class.java)
            if (firestoreTodo?.userId != currentUser.uid) {
                throw Exception("Todo does not belong to current user")
            }

            todosCollection.document(id)
                .update(
                    mapOf(
                        "isCompleted" to isCompleted,
                        "updatedAt" to Date()
                    )
                )
                .await()

        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            todosCollection.document(id).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getAll(): Flow<List<Todo>> = callbackFlow {
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            firestoreListener?.remove()
            
            val currentUser = auth.currentUser
            if (currentUser == null) {
                trySend(emptyList())
                return@AuthStateListener
            }

            val query = todosCollection
                .whereEqualTo("userId", currentUser.uid)

            firestoreListener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val todos = snapshot.documents.mapNotNull { document ->
                        try {
                            val firestoreTodo = document.toObject(FirestoreTodo::class.java)
                            firestoreTodo?.let {
                                val completionStatus = if (!it.isCompleted && document.contains("completed")) {
                                    document.getBoolean("completed") ?: false
                                } else {
                                    it.isCompleted
                                }
                                
                                Todo(
                                    id = document.id,
                                    title = it.title,
                                    description = it.description,
                                    isCompleted = completionStatus
                                )
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(todos)
                } else {
                    trySend(emptyList())
                }
            }
        }
        
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)

        awaitClose { 
            firestoreListener?.remove()
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun getBy(id: String): Todo? {
        return try {

            val document = todosCollection.document(id).get().await()
            val firestoreTodo = document.toObject(FirestoreTodo::class.java)
            firestoreTodo?.let {
                val completionStatus = if (!it.isCompleted && document.contains("completed")) {
                    document.getBoolean("completed") ?: false
                } else {
                    it.isCompleted
                }
                
                Todo(
                    id = document.id,
                    title = it.title,
                    description = it.description,
                    isCompleted = completionStatus
                )
            }

        } catch (e: Exception) {
            null
        }
    }
}

