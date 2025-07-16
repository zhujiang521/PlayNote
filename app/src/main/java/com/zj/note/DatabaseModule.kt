package com.zj.note

import android.content.Context
import androidx.room.Room
import com.zj.data.room.AppDatabase
import com.zj.data.room.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlin.jvm.java

private const val CONVERSATION_DATABASE = "conversation-database"

@Module
@InstallIn(ActivityRetainedComponent::class)
object DatabaseModule {

    @Provides
    @ActivityRetainedScoped
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return databaseBuilder(context)
    }

    @Provides
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao {
        return appDatabase.noteDao()
    }
}

fun databaseBuilder(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        CONVERSATION_DATABASE
    ).build()
}