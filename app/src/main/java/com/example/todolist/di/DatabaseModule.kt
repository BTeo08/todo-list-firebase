package com.example.todolist.di

import android.content.Context
import androidx.room.Room
import com.example.todolist.auth.AuthRepository
import com.example.todolist.data.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(
        @ApplicationContext context: Context
    ): TodoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "todo-app"
        ).build()
    }

    @Provides
    fun provideTodoDao(database: TodoDatabase): TodoDao = database.todoDao

    @Provides
    @Singleton
    fun provideTodoRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): TodoRepository = FirestoreTodoRepository(firestore, authRepository)
}