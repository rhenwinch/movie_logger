package com.xcape.movie_logger.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract val movieDatabaseDao: MovieDatabaseDao

    private class MovieDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.movieDatabaseDao)
                }
            }
        }

        suspend fun populateDatabase(daoSource: MovieDatabaseDao) {
            // Delete all content here.
            daoSource.clearMovies()

            // TODO: Add populators
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getInstance(
            context: Context,
            scope: CoroutineScope
        ): MovieDatabase {
            synchronized(this) {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        MovieDatabase::class.java,
                        "word_database"
                    )
                        .addCallback(MovieDatabaseCallback(scope))
                        .build()
                    INSTANCE = instance
                    // return instance
                    instance
                }
            }
        }
    }
}

