### ProGuard rules for data module

# Keep model and entity classes used by Room and Hilt
-keepclassmembers class * {
    @androidx.room.Dao <methods>;
    @androidx.room.Entity <fields>;
}