package com.khadr.tasbih.utils

private val generalMessages = listOf(
    "بارك الله فيك",
    "أحسنت، واصل",
    "زادك الله توفيقاً",
    "اللهم تقبّل منك",
    "أجرك على الله",
    "نور الله قلبك",
    "ما شاء الله، استمر",
    "وفّقك الله دائماً"
)

private val stepMessages = mapOf(
    "سُبْحَانَ اللَّهِ" to listOf(
        "سبحان الله وبحمده",
        "التسبيح يُنير القلوب",
        "أحسنت، واصل التسبيح"
    ),
    "الْحَمْدُ لِلَّهِ" to listOf(
        "الحمد لله على كل حال",
        "اشكر الله دائماً",
        "الحمد لله، زادك شكراً"
    ),
    "اللَّهُ أَكْبَرُ" to listOf(
        "الله أكبر من كل شيء",
        "عظّمت الله حقّ عظمته",
        "أحسنت، واصل التكبير"
    ),
    "لَا إِلَٰهَ إِلَّا اللَّهُ" to listOf(
        "أفضل ما قلته أنت والنبيّون",
        "لا إله إلا الله وحده لا شريك له",
        "ملأت الميزان بالتوحيد"
    ),
    "اسْتَغْفِرُ اللَّهَ" to listOf(
        "الله غفور رحيم",
        "الاستغفار يزيد الرزق",
        "بارك الله فيك وغفر لك"
    )
)

fun getEncouragementMessage(dhikrName: String): String {
    val specific = stepMessages[dhikrName]
    return if (specific != null && Math.random() > 0.35) specific.random()
    else generalMessages.random()
}