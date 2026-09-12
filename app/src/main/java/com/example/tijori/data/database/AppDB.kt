package com.example.tijori.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tijori.data.converter.DateConverter
import com.example.tijori.data.dao.AppConfigDao
import com.example.tijori.data.dao.TransactionDao
import com.example.tijori.data.dao.UserDao
import com.example.tijori.data.entities.AppConfig
import com.example.tijori.data.entities.Transaction
import com.example.tijori.data.entities.User
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [User::class, AppConfig::class, Transaction::class], version = 1, exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class TijoriDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun transactionDao(): TransactionDao

}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTijoriDatabase(
        @ApplicationContext context: Context
    ): TijoriDatabase = Room.databaseBuilder(
        context, TijoriDatabase::class.java, "tijori.db"
    ).build()

    @Provides
    @Singleton
    fun provideUserDao(database: TijoriDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideAppConfigDao(database: TijoriDatabase): AppConfigDao = database.appConfigDao()

    @Provides
    @Singleton
    fun provideTransactionDao(database: TijoriDatabase): TransactionDao = database.transactionDao()

}