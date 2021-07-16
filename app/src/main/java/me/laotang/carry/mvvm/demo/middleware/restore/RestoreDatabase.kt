package me.laotang.carry.mvvm.demo.middleware.restore

import android.content.Context
import androidx.room.*

@Database(
    entities = [RestoreEntity::class],
    version = 1
)
abstract class RestoreDatabase : RoomDatabase() {

    abstract fun restoreDao(): RestoreDao

    companion object {
        private var instance: RestoreDatabase? = null

        @Synchronized
        fun getInstance(context: Context): RestoreDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    RestoreDatabase::class.java,
                    "restore_database.db"
                ).allowMainThreadQueries()
                    .build()
            }
            return instance as RestoreDatabase
        }
    }
}

@Entity(tableName = "Restore")
data class RestoreEntity(
    @PrimaryKey
    @ColumnInfo(name = "tag")
    val tag: String,
    @ColumnInfo(name = "lastValue", defaultValue = "")
    val lastValue: String,
    @ColumnInfo(name = "time")
    val time: Long,
)

@Dao
interface RestoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg restore: RestoreEntity)

    @Query("SELECT * FROM Restore WHERE tag = :tag")
    fun find(tag: String): RestoreEntity?

    @Delete
    fun delete(restore: RestoreEntity): Int
}