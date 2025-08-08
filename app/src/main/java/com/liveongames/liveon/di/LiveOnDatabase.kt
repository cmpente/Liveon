// app/src/main/java/com/liveongames/data/db/LiveOnDatabase.kt
package com.liveongames.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.liveongames.data.db.dao.CharacterDao
import com.liveongames.data.db.dao.CrimeDao
import com.liveongames.data.db.dao.PetDao
import com.liveongames.data.db.dao.EventDao
import com.liveongames.data.db.dao.SaveSlotDao
import com.liveongames.data.db.entity.CharacterEntity
import com.liveongames.data.db.entity.CrimeEntity
import com.liveongames.data.db.entity.PetEntity
import com.liveongames.data.db.entity.EventEntity
import com.liveongames.data.db.entity.SaveSlotEntity

@Database(
    entities = [
        CrimeEntity::class,
        PetEntity::class,
        EventEntity::class,
        SaveSlotEntity::class,
        CharacterEntity::class  // Add this line
    ],
    version = 1,
    exportSchema = false
)
abstract class LiveOnDatabase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
    abstract fun petDao(): PetDao
    abstract fun eventDao(): EventDao
    abstract fun saveSlotDao(): SaveSlotDao
    abstract fun characterDao(): CharacterDao  // Add this line
}