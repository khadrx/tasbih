package com.khadr.tasbih.data

data class Dhikr(val name: String, val target: Int)

val defaultDhikrList = listOf(
    Dhikr("سُبْحَانَ اللَّهِ",          33),
    Dhikr("الْحَمْدُ لِلَّهِ",           33),
    Dhikr("اللَّهُ أَكْبَرُ",            33),
    Dhikr("لَا إِلَٰهَ إِلَّا اللَّهُ",  100),
    Dhikr("اسْتَغْفِرُ اللَّهَ",         100)
)