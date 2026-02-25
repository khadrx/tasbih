package com.khadr.tasbih.data

data class DhikrPreset(
    val id   : String,
    val title: String,
    val items: List<Dhikr>
)

val allPresets = listOf(

    DhikrPreset(
        id    = "tasbih_salah",
        title = "تسابيح بعد الصلاة",
        items = listOf(
            Dhikr("سُبْحَانَ اللَّهِ",          33),
            Dhikr("الْحَمْدُ لِلَّهِ",           33),
            Dhikr("اللَّهُ أَكْبَرُ",            33),
            Dhikr("لَا إِلَٰهَ إِلَّا اللَّهُ",  1)
        )
    ),

    DhikrPreset(
        id    = "morning_azkar",
        title = "أذكار الصباح",
        items = listOf(
            Dhikr("أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ", 3),
            Dhikr("بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",           3),
            Dhikr("آيَةُ الْكُرْسِيِّ",                              1),
            Dhikr("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",                  100),
            Dhikr("لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ",             10),
            Dhikr("اسْتَغْفِرُ اللَّهَ",                             3)
        )
    ),

    DhikrPreset(
        id    = "evening_azkar",
        title = "أذكار المساء",
        items = listOf(
            Dhikr("أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ", 3),
            Dhikr("آيَةُ الْكُرْسِيِّ",                              1),
            Dhikr("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",                  100),
            Dhikr("اسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",          100),
            Dhikr("لَا إِلَٰهَ إِلَّا اللَّهُ",                      10)
        )
    ),

    DhikrPreset(
        id    = "janazah",
        title = "أذكار الجنازة",
        items = listOf(
            Dhikr("إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ",    3),
            Dhikr("اللَّهُمَّ اغْفِرْ لَهُ وَارْحَمْهُ",             33),
            Dhikr("اللَّهُمَّ ارْفَعْ دَرَجَتَهُ",                   33),
            Dhikr("اللَّهُمَّ أَلْحِقْهُ بِالصَّالِحِينَ",           33)
        )
    ),

    DhikrPreset(
        id    = "istighfar",
        title = "الاستغفار",
        items = listOf(
            Dhikr("اسْتَغْفِرُ اللَّهَ",                             100),
            Dhikr("اسْتَغْفِرُ اللَّهَ الْعَظِيمَ",                  100),
            Dhikr("أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",         33)
        )
    ),

    DhikrPreset(
        id    = "salawat",
        title = "الصلاة على النبي",
        items = listOf(
            Dhikr("اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",               100),
            Dhikr("صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ",               100)
        )
    ),

    DhikrPreset(
        id    = "ruqyah",
        title = "الرقية الشرعية",
        items = listOf(
            Dhikr("بِسْمِ اللَّهِ أَرْقِيكَ",                        3),
            Dhikr("أَعُوذُ بِعِزَّةِ اللَّهِ وَقُدْرَتِهِ",          7),
            Dhikr("سُبْحَانَ اللَّهِ",                               33),
            Dhikr("اللَّهُ أَكْبَرُ",                                33)
        )
    ),

    DhikrPreset(
        id    = "hajj_umrah",
        title = "تلبية الحج والعمرة",
        items = listOf(
            Dhikr("لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ",                  100),
            Dhikr("سُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ",            33),
            Dhikr("اللَّهُ أَكْبَرُ كَبِيرًا",                       33)
        )
    ),

    DhikrPreset(
        id    = "dua_khatm",
        title = "أذكار ختم القرآن",
        items = listOf(
            Dhikr("سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ",             3),
            Dhikr("سُبْحَانَ اللَّهِ",                               33),
            Dhikr("الْحَمْدُ لِلَّهِ",                               33),
            Dhikr("اللَّهُ أَكْبَرُ",                                34)
        )
    )
)