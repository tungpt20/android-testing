package com.example.android.architecture.blueprints.todoapp.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.example.android.architecture.blueprints.todoapp.data.repository.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.repository.TasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    private var database: ToDoDatabase? = null
    /*
    * The value of a volatile variable will never be cached, and all writes and reads will be done
    *  to and from the main memory. This helps make sure the value of INSTANCE is always up-to-date
    *  and the same to all execution threads.
    *  It means that changes made by one thread to INSTANCE are visible to all other threads immediately,
    *  and you don't get a situation where, say, two threads each update the same entity in a cache,
    *  which would create a problem.
    * */
    @Volatile
    var tasksRepository: TasksRepository? = null
        @VisibleForTesting set

    private val lock = Any()

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                TasksRemoteDataSource.deleteAllTasks()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            tasksRepository = null
        }
    }

    fun provideTasksRepository(context: Context): TasksRepository {
        synchronized(this) {
            return tasksRepository ?: createTasksRepository(context)
        }
    }

    private fun createTasksRepository(context: Context): TasksRepository {
        val newRepo = DefaultTasksRepository(TasksRemoteDataSource, createTaskLocalDataSource(context))
        tasksRepository = newRepo
        return newRepo
    }

    private fun createTaskLocalDataSource(context: Context): TasksDataSource {
        val database = database ?: createDataBase(context)
        return TasksLocalDataSource(database.taskDao())
    }

    private fun createDataBase(context: Context): ToDoDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java, "Tasks.db"
        ).build()
        database = result
        return result
    }
}