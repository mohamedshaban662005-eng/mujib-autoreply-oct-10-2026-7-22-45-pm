package com.example.model

enum class SupportedApp(
    val id: String,
    val displayName: String,
    val packageName: String,
    val brandHex: Long
) {
    WHATSAPP("WHATSAPP", "واتساب", "com.whatsapp", 0xFF25D366),
    WHATSAPP_BUSINESS("WHATSAPP", "واتساب للأعمال", "com.whatsapp.w4b", 0xFF128C7E),
    MESSENGER("MESSENGER", "ماسنجر", "com.facebook.orca", 0xFF0084FF),
    INSTAGRAM("INSTAGRAM", "إنستغرام", "com.instagram.android", 0xFFE1306C),
    FACEBOOK("FACEBOOK", "فيسبوك", "com.facebook.katana", 0xFF1877F2),
    TELEGRAM("OTHER", "تيليجرام", "org.telegram.messenger", 0xFF229ED9),
    OTHER("OTHER", "تطبيق آخر", "", 0xFF607D8B);

    companion object {
        fun fromPackage(pkg: String?): SupportedApp {
            if (pkg == null) return OTHER
            return entries.firstOrNull { it.packageName.isNotEmpty() && pkg.startsWith(it.packageName) }
                ?: OTHER
        }

        fun fromName(name: String): SupportedApp {
            return entries.firstOrNull { it.id.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) }
                ?: OTHER
        }
    }
}

enum class Dialect(val label: String, val code: String) {
    EGYPTIAN("لهجة مصرية بسيطة ومهذبة", "مصري"),
    STANDARD_ARABIC("عربية مبسطة (فصحى)", "فصحى")
}

enum class ActionType(val label: String, val arabicDesc: String) {
    REPLY("REPLY", "رد تلقائي فوري"),
    HANDOFF("HANDOFF", "تحويل لموظف خدمة العملاء"),
    NO_REPLY("NO_REPLY", "تجاهل (بدون رد)")
}

data class AutoReplyResult(
    val action: ActionType,
    val reply: String,
    val reason: String,
    val rawJson: String = ""
)

data class NotificationPayload(
    val app: SupportedApp,
    val sender: String,
    val isGroup: Boolean,
    val message: String,
    val history: String = "",
    val localTime: String = "",
    val withinWorkingHours: Boolean = true
)

data class ServiceItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val price: String,
    val currency: String = "جنيه",
    val notes: String = ""
)

data class OfferItem(
    val title: String,
    val validUntil: String,
    val conditions: String
)

data class AreaItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val fee: String
)

data class WorkingHours(
    val days: String = "السبت إلى الخميس",
    val startTime: String = "09:00 ص",
    val endTime: String = "10:00 م"
)

data class FaqItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val answer: String
)

data class BusinessProfile(
    val businessName: String = "متجر الأمل للمفروشات",
    val businessField: String = "مفروشات ومنسوجات منزلية فاخرة",
    val shortDescription: String = "أفضل المفروشات القطنية والأطقم التركية بأعلى جودة وضمان حقيقي",
    val links: String = "https://instagram.com/al-amal-home",
    val whatsappNumber: String = "+201001234567",
    val dialect: Dialect = Dialect.EGYPTIAN,
    val services: List<ServiceItem> = listOf(
        ServiceItem(name = "طقم سرير قطن مصري 5 قطع", description = "قطن 100% عالي النعومة", price = "450", currency = "جنيه", notes = "متوفر 6 ألوان"),
        ServiceItem(name = "لحاف فيبر دبل تركي", description = "حشوة فيبر حراري مضغوط ومبطن", price = "850", currency = "جنيه", notes = "مع 2 خدادية مجاناً"),
        ServiceItem(name = "مفرش عروسة ستان مطرز", description = "ستان تركي مطرز خيوط حرير", price = "1200", currency = "جنيه", notes = "طلب مسبق")
    ),
    val activeOffer: OfferItem = OfferItem(
        title = "خصم 20% على الطقم الثاني + شحن مجاني للطلبات فوق 1000 جنيه",
        validUntil = "نهاية الشهر الحالي",
        conditions = "للأطقم القطنية فقط"
    ),
    val areas: List<AreaItem> = listOf(
        AreaItem(name = "مدينة نصر ومصر الجديدة", fee = "40 جنيه"),
        AreaItem(name = "التجمع والمعادي", fee = "50 جنيه"),
        AreaItem(name = "الجيزة والمهندسين", fee = "45 جنيه"),
        AreaItem(name = "المحافظات", fee = "70 جنيه")
    ),
    val workingHours: WorkingHours = WorkingHours(
        days = "يومياً عدا الجمعة",
        startTime = "10:00 ص",
        endTime = "11:00 م"
    ),
    val orderSteps: String = "ابعت لنا صورة الموديل + الاسم والعنوان ورقم التليفون، وهيتم تأكيد الطلب فوراً",
    val faqs: List<FaqItem> = listOf(
        FaqItem(
            question = "هل متاح المعاينة قبل الاستلام؟",
            answer = "نعم، تقدر تعاين المنتج وتتأكد من الخامة مع مندوب الشحن قبل الدفع."
        ),
        FaqItem(
            question = "كم يستغرق وقت التوصيل؟",
            answer = "التوصيل داخل القاهرة والجيزة خلال 24 إلى 48 ساعة، والمحافظات خلال 3 إلى 4 أيام."
        )
    ),
    val returnPolicy: String = "استبدال أو استرجاع مجاني خلال 14 يوماً بشرط سلامة المنتج وغلافه الأصلي.",
    val paymentPolicy: String = "الدفع عند الاستلام كاش، أو عبر فودافون كاش / انستاباي.",
    val warrantyPolicy: String = "ضمان استرجاع فوري ضد أي عيوب صناعة أو عدم مطابقة للمواصفات."
)
