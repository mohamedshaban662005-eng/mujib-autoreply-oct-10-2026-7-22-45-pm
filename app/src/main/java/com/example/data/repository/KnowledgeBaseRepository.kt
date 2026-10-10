package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class KnowledgeBaseRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kb_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<BusinessProfile> = _profile.asStateFlow()

    private val _isServiceActive = MutableStateFlow(prefs.getBoolean("is_service_active", true))
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    private val _autoReplyDelaySeconds = MutableStateFlow(prefs.getInt("auto_reply_delay", 2))
    val autoReplyDelaySeconds: StateFlow<Int> = _autoReplyDelaySeconds.asStateFlow()

    fun updateProfile(newProfile: BusinessProfile) {
        saveProfile(newProfile)
        _profile.value = newProfile
    }

    fun setServiceActive(active: Boolean) {
        prefs.edit().putBoolean("is_service_active", active).apply()
        _isServiceActive.value = active
    }

    fun setAutoReplyDelaySeconds(seconds: Int) {
        prefs.edit().putInt("auto_reply_delay", seconds).apply()
        _autoReplyDelaySeconds.value = seconds
    }

    fun applyPreset(presetType: String) {
        val newProfile = when (presetType) {
            "RESTAURANT" -> BusinessProfile(
                businessName = "مطعم ومشاوي السلطان",
                businessField = "مشاوي وطواجن ومأكولات شرقية",
                shortDescription = "أشهى المشاوي على الفحم والطواجن البلدي الطازجة بأفضل جودة وخدمة سريعة",
                links = "https://facebook.com/alsultan-restaurant",
                whatsappNumber = "+201123456789",
                dialect = Dialect.EGYPTIAN,
                services = listOf(
                    ServiceItem(name = "وجبة ميكس جريل مشكل", description = "كباب وكفتة وشيش طاووق مع رز وسلطات", price = "220", currency = "جنيه", notes = "تكفي فرداً واحداً"),
                    ServiceItem(name = "طاجن لحمة موزة بالبصل", description = "لحم بلدي طازج ومطهو في الفرن الفخاري", price = "260", currency = "جنيه", notes = "يقدم مع أرز معمر"),
                    ServiceItem(name = "سرفيس العيلة (4 أفراد)", description = "كيلو مشاوي مشكل + بطاطس + 4 شوربة وسلطات", price = "780", currency = "جنيه", notes = "يشمل عصير عائلي مجاناً")
                ),
                activeOffer = OfferItem(
                    title = "عرض الغداء: اطلب وجبتين واحصل على الثالثة بنصف السعر",
                    validUntil = "الأحد القادم",
                    conditions = "من الساعة 1 ظهراً حتى 6 مساءً"
                ),
                areas = listOf(
                    AreaItem(name = "الدقي والمهندسين", fee = "25 جنيه"),
                    AreaItem(name = "الزمالك والعجوزة", fee = "30 جنيه"),
                    AreaItem(name = "الهرم وفيصل", fee = "35 جنيه")
                ),
                workingHours = WorkingHours(
                    days = "طوال أيام الأسبوع",
                    startTime = "11:00 ص",
                    endTime = "02:00 ص"
                ),
                orderSteps = "حدد الوجبات والعنوان بالتفصيل ورقم التليفون، وخلال 45 دقيقة هيكون عندك ساخن",
                faqs = listOf(
                    FaqItem(
                        question = "هل اللحوم طازجة وبلدية؟",
                        answer = "نعم، جميع اللحوم ذبح اليوم بلدي 100% ومضمونة بأعلى معايير النظافة."
                    ),
                    FaqItem(
                        question = "هل التوصيل متوفر في أوقات متأخرة؟",
                        answer = "نعم، خدمة التوصيل مستمرة حتى الثانية صباحاً يومياً."
                    )
                ),
                returnPolicy = "استبدال فوري أو إرجاع قيمة الوجبة في حال وجود أي خطأ بالطلب.",
                paymentPolicy = "كاش عند الاستلام، أو فيزا ومحافظ إلكترونية مع المندوب.",
                warrantyPolicy = "ضمان وصول الوجبة ساخنة وبأعلى جودة تغليف حراري."
            )
            "SERVICES" -> BusinessProfile(
                businessName = "شركة درع الأمان للصيانة والتكييف",
                businessField = "صيانة وتركيب التكييفات والأجهزة المنزلية",
                shortDescription = "مركز صيانة معتمد بأحدث الأجهزة وفنيين مدربين وقطع غيار أصلية بضمان معتمد",
                links = "https://armor-maintenance.com",
                whatsappNumber = "+201209876543",
                dialect = Dialect.EGYPTIAN,
                services = listOf(
                    ServiceItem(name = "صيانة وتنظيف تكييف كامل", description = "غسيل بالضغط وفحص الفريون والدوائر الكهربائية", price = "200", currency = "جنيه", notes = "شامل تنظيف الفلاتر"),
                    ServiceItem(name = "شحن فريون هندي أصلي R22/R410", description = "شحن كامل ومعالجة التسريب إن وجد", price = "450", currency = "جنيه", notes = "مع كشف تسريب مجاني"),
                    ServiceItem(name = "فك وتركيب تكييف (نقل مكان)", description = "فك بأمان وإعادة تركيب بميزان مائي", price = "350", currency = "جنيه", notes = "بدون مواسير إضافية")
                ),
                activeOffer = OfferItem(
                    title = "فحص ومعاينة مجانية لأكثر من جهازين في نفس الزيارة",
                    validUntil = "15 من الشهر الحالي",
                    conditions = "للقاهرة الكبرى فقط"
                ),
                areas = listOf(
                    AreaItem(name = "القاهرة الجديدة والشروق", fee = "مجاناً مع الصيانة"),
                    AreaItem(name = "مدينة نصر والمعادي", fee = "مجاناً مع الصيانة"),
                    AreaItem(name = "أكتوبر والشيخ زايد", fee = "50 جنيه رسوم انتقال")
                ),
                workingHours = WorkingHours(
                    days = "من السبت إلى الخميس",
                    startTime = "09:00 ص",
                    endTime = "08:00 م"
                ),
                orderSteps = "ابعت نوع وموديل التكييف وعنوانك بالتفصيل لتحديد موعد الزيارة المنزلية",
                faqs = listOf(
                    FaqItem(
                        question = "هل يوجد ضمان على الصيانة؟",
                        answer = "نعم، شهادة ضمان معتمدة لمدة 3 أشهر على الصيانة وعام كامل على قطع الغيار."
                    )
                ),
                returnPolicy = "إعادة الزيارة مجاناً في حالة عدم رضاء العميل خلال فترة الضمان.",
                paymentPolicy = "كاش بعد انتهاء الفحص والصيانة، أو تحويل انستاباي وفودافون كاش.",
                warrantyPolicy = "ضمان خطي معتمد من الشركة على كافة أعمال الصيانة والشحن."
            )
            else -> BusinessProfile() // Default E-Commerce
        }
        updateProfile(newProfile)
    }

    private fun loadProfile(): BusinessProfile {
        val raw = prefs.getString("business_profile_json", null) ?: return BusinessProfile()
        return try {
            val json = JSONObject(raw)
            val servicesList = mutableListOf<ServiceItem>()
            val servicesArr = json.optJSONArray("services") ?: JSONArray()
            for (i in 0 until servicesArr.length()) {
                val itemObj = servicesArr.getJSONObject(i)
                servicesList.add(
                    ServiceItem(
                        id = itemObj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = itemObj.optString("name", ""),
                        description = itemObj.optString("description", ""),
                        price = itemObj.optString("price", ""),
                        currency = itemObj.optString("currency", "جنيه"),
                        notes = itemObj.optString("notes", "")
                    )
                )
            }

            val areasList = mutableListOf<AreaItem>()
            val areasArr = json.optJSONArray("areas") ?: JSONArray()
            for (i in 0 until areasArr.length()) {
                val itemObj = areasArr.getJSONObject(i)
                areasList.add(
                    AreaItem(
                        id = itemObj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = itemObj.optString("name", ""),
                        fee = itemObj.optString("fee", "")
                    )
                )
            }

            val faqsList = mutableListOf<FaqItem>()
            val faqsArr = json.optJSONArray("faqs") ?: JSONArray()
            for (i in 0 until faqsArr.length()) {
                val itemObj = faqsArr.getJSONObject(i)
                faqsList.add(
                    FaqItem(
                        id = itemObj.optString("id", java.util.UUID.randomUUID().toString()),
                        question = itemObj.optString("question", ""),
                        answer = itemObj.optString("answer", "")
                    )
                )
            }

            val offerObj = json.optJSONObject("activeOffer")
            val offer = if (offerObj != null) {
                OfferItem(
                    title = offerObj.optString("title", ""),
                    validUntil = offerObj.optString("validUntil", ""),
                    conditions = offerObj.optString("conditions", "")
                )
            } else OfferItem("", "", "")

            val whObj = json.optJSONObject("workingHours")
            val wh = if (whObj != null) {
                WorkingHours(
                    days = whObj.optString("days", "يومياً"),
                    startTime = whObj.optString("startTime", "09:00 ص"),
                    endTime = whObj.optString("endTime", "10:00 م")
                )
            } else WorkingHours()

            val dialectStr = json.optString("dialect", "EGYPTIAN")
            val dialect = try {
                Dialect.valueOf(dialectStr)
            } catch (e: Exception) {
                Dialect.EGYPTIAN
            }

            BusinessProfile(
                businessName = json.optString("businessName", "متجر الأمل"),
                businessField = json.optString("businessField", "مفروشات"),
                shortDescription = json.optString("shortDescription", ""),
                links = json.optString("links", ""),
                whatsappNumber = json.optString("whatsappNumber", ""),
                dialect = dialect,
                services = if (servicesList.isEmpty()) BusinessProfile().services else servicesList,
                activeOffer = offer,
                areas = if (areasList.isEmpty()) BusinessProfile().areas else areasList,
                workingHours = wh,
                orderSteps = json.optString("orderSteps", ""),
                faqs = if (faqsList.isEmpty()) BusinessProfile().faqs else faqsList,
                returnPolicy = json.optString("returnPolicy", ""),
                paymentPolicy = json.optString("paymentPolicy", ""),
                warrantyPolicy = json.optString("warrantyPolicy", "")
            )
        } catch (e: Exception) {
            BusinessProfile()
        }
    }

    private fun saveProfile(profile: BusinessProfile) {
        val json = JSONObject()
        json.put("businessName", profile.businessName)
        json.put("businessField", profile.businessField)
        json.put("shortDescription", profile.shortDescription)
        json.put("links", profile.links)
        json.put("whatsappNumber", profile.whatsappNumber)
        json.put("dialect", profile.dialect.name)
        json.put("orderSteps", profile.orderSteps)
        json.put("returnPolicy", profile.returnPolicy)
        json.put("paymentPolicy", profile.paymentPolicy)
        json.put("warrantyPolicy", profile.warrantyPolicy)

        val servicesArr = JSONArray()
        profile.services.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("description", it.description)
            obj.put("price", it.price)
            obj.put("currency", it.currency)
            obj.put("notes", it.notes)
            servicesArr.put(obj)
        }
        json.put("services", servicesArr)

        val areasArr = JSONArray()
        profile.areas.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("fee", it.fee)
            areasArr.put(obj)
        }
        json.put("areas", areasArr)

        val faqsArr = JSONArray()
        profile.faqs.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("question", it.question)
            obj.put("answer", it.answer)
            faqsArr.put(obj)
        }
        json.put("faqs", faqsArr)

        val offerObj = JSONObject()
        offerObj.put("title", profile.activeOffer.title)
        offerObj.put("validUntil", profile.activeOffer.validUntil)
        offerObj.put("conditions", profile.activeOffer.conditions)
        json.put("activeOffer", offerObj)

        val whObj = JSONObject()
        whObj.put("days", profile.workingHours.days)
        whObj.put("startTime", profile.workingHours.startTime)
        whObj.put("endTime", profile.workingHours.endTime)
        json.put("workingHours", whObj)

        prefs.edit().putString("business_profile_json", json.toString()).apply()
    }
}
