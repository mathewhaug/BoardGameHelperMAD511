package com.mathewhaug.boardgamehelper.data.local

import androidx.room3.ColumnTypeConverter
import java.time.Instant

// SQLite has no Instant type, so Room stores it as an INTEGER of epoch millis and uses these two
// functions to translate in both directions. the entity still says Instant, which matters: a bare
// Long called addedAt would leave every reader guessing whether it is seconds or milliseconds
class InstantConverter {
    @ColumnTypeConverter fun fromInstant(v: Instant): Long = v.toEpochMilli()
    @ColumnTypeConverter fun toInstant(v: Long): Instant = Instant.ofEpochMilli(v)
}
