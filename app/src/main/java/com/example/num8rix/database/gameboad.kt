package database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class gameboad(
    val name: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)