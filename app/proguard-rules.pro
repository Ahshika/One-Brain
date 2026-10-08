# One Brain – R8 rules.
# AdMob, UMP and Play Billing ship their own consumer rules.

# Session/result objects travel between activities as Serializable extras and
# through SavedStateHandle; keep their serialization members stable.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Enums are persisted by name() in the save file.
-keepclassmembers enum com.onebrain.game.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Strip verbose logging from release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
