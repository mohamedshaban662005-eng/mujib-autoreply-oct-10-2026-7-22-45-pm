package com.example.ai

import android.util.Log
import com.example.model.*
import org.json.JSONObject

object AutoReplyEngine {
    private const val TAG = "AutoReplyEngine"

    suspend fun processNotification(
        payload: NotificationPayload,
        profile: BusinessProfile
    ): AutoReplyResult {
        val systemInstruction = PromptBuilder.buildSystemInstruction(profile)
        val userTurn = PromptBuilder.buildUserTurn(payload)

        // 1. Try Gemini API if key is available
        if (GeminiClient.hasApiKey()) {
            val geminiResult = GeminiClient.generateAutoReply(systemInstruction, userTurn)
            if (geminiResult.isSuccess) {
                val rawJson = geminiResult.getOrNull() ?: ""
                val parsed = parseJsonResponse(rawJson)
                if (parsed != null) {
                    return sanitizeResult(parsed)
                }
            } else {
                Log.w(TAG, "Gemini failed, falling back to local rule engine: ${geminiResult.exceptionOrNull()?.message}")
            }
        }

        // 2. Intelligent local rule-based fallback adhering strictly to the system instruction
        return executeLocalRuleEngine(payload, profile)
    }

    private fun parseJsonResponse(rawJson: String): AutoReplyResult? {
        return try {
            // Extract JSON object if wrapped in code blocks
            val cleanJson = rawJson
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleanJson)
            val actionStr = json.optString("action", "REPLY").uppercase()
            val reply = json.optString("reply", "")
            val reason = json.optString("reason", "gemini-response")

            val action = when (actionStr) {
                "NO_REPLY" -> ActionType.NO_REPLY
                "HANDOFF" -> ActionType.HANDOFF
                else -> ActionType.REPLY
            }

            AutoReplyResult(action, reply, reason, cleanJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON response: $rawJson", e)
            null
        }
    }

    private fun sanitizeResult(result: AutoReplyResult): AutoReplyResult {
        if (result.action == ActionType.NO_REPLY) {
            return result.copy(reply = "")
        }

        var text = result.reply
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .trim()

        if (text.length > 300) {
            text = text.substring(0, 297) + "..."
        }

        return result.copy(reply = text)
    }

    private fun executeLocalRuleEngine(
        payload: NotificationPayload,
        profile: BusinessProfile
    ): AutoReplyResult {
        val msg = payload.message.trim()
        val sender = payload.sender.ifBlank { "عزيزنا العميل" }
        val isEgyptian = profile.dialect == Dialect.EGYPTIAN

        // 1. Check for NO_REPLY conditions
        if (msg.isEmpty()) {
            return AutoReplyResult(ActionType.NO_REPLY, "", "Empty message")
        }

        // Gratitude only, emojis, simple acknowledgments
        val noReplyKeywords = listOf("شكرا", "شكراً", "تمام", "تسلم", "اوك", "ok", "👍", "🙏", "❤️", "🌹", "مشكور", "جزاك الله خير")
        if (noReplyKeywords.any { msg.equals(it, ignoreCase = true) }) {
            return AutoReplyResult(ActionType.NO_REPLY, "", "No clear question or action needed (greeting or thank you only)")
        }

        // Group message without mentioning business
        if (payload.isGroup) {
            val mentionsBusiness = msg.contains(profile.businessName, ignoreCase = true) ||
                    profile.services.any { msg.contains(it.name, ignoreCase = true) }
            if (!mentionsBusiness) {
                return AutoReplyResult(ActionType.NO_REPLY, "", "Group message not mentioning business")
            }
        }

        // 2. Complaint or delay -> HANDOFF
        val complaintKeywords = listOf("شكوى", "اتأخر", "تأخر", "تاخر", "ماحدش بيرد", "محدش بيرد", "مشكلة", "بايظ", "مكسور", "ناقص", "غلط", "نصاب", "سيء", "زفت")
        if (complaintKeywords.any { msg.contains(it, ignoreCase = true) }) {
            val replyText = if (isEgyptian) {
                "أستاذ $sender، حقك علينا ونعتذر جداً عن التأخير أو أي إزعاج. ياريت تبعتلنا رقم الطلب ورقم تليفونك وفريقنا هيتواصل معاك حالاً."
            } else {
                "أهلاً بك $sender، نعتذر بشدة عن التأخير الحاصل. يرجى تزويدنا برقم الطلب ورقم هاتفك وسيقوم فريقنا بالتواصل معك فوراً."
            }
            return AutoReplyResult(ActionType.HANDOFF, replyText, "Customer complaint or delay - escalated to human team")
        }

        // 3. Question about prices or specific service
        val matchedService = profile.services.firstOrNull { service ->
            msg.contains(service.name, ignoreCase = true) ||
                    service.name.split(" ").any { word -> word.length > 3 && msg.contains(word, ignoreCase = true) }
        }

        val askedPrice = msg.contains("سعر", ignoreCase = true) ||
                msg.contains("بكام", ignoreCase = true) ||
                msg.contains("كم", ignoreCase = true) ||
                msg.contains("تكلفة", ignoreCase = true)

        val matchedArea = profile.areas.firstOrNull { area ->
            msg.contains(area.name, ignoreCase = true) ||
                    area.name.split(" ").any { word -> word.length > 3 && msg.contains(word, ignoreCase = true) }
        }

        val askedDelivery = msg.contains("توصيل", ignoreCase = true) ||
                msg.contains("شحن", ignoreCase = true) ||
                msg.contains("بتوصلوا", ignoreCase = true) ||
                msg.contains("توصلوا", ignoreCase = true)

        // Matched service and price
        if (matchedService != null) {
            val notesStr = if (matchedService.notes.isNotBlank()) " (${matchedService.notes})" else ""
            var reply = if (isEgyptian) {
                "أهلاً أستاذ $sender 🌟 سعر ${matchedService.name} هو ${matchedService.price} ${matchedService.currency}$notesStr."
            } else {
                "مرحباً $sender 🌟 سعر ${matchedService.name} هو ${matchedService.price} ${matchedService.currency}$notesStr."
            }

            if (matchedArea != null) {
                reply += if (isEgyptian) {
                    " والتوصيل لمنطقة ${matchedArea.name} رسومه ${matchedArea.fee}."
                } else {
                    " والتوصيل إلى ${matchedArea.name} تبلغ رسومه ${matchedArea.fee}."
                }
            }

            if (!payload.withinWorkingHours) {
                reply += if (isEgyptian) " علماً بأن مواعيد عملنا ${profile.workingHours.startTime} إلى ${profile.workingHours.endTime}." else " علماً بأن مواعيد العمل الرسمية من ${profile.workingHours.startTime} إلى ${profile.workingHours.endTime}."
            }

            reply += if (isEgyptian) " تحب نحجزلك أو نأكد طلبك الآن؟" else " هل ترغب في تأكيد الطلب الآن؟"

            return AutoReplyResult(ActionType.REPLY, reply, "Service and pricing found in knowledge base")
        }

        // Matched area and delivery inquiry only
        if (askedDelivery || matchedArea != null) {
            if (matchedArea != null) {
                val reply = if (isEgyptian) {
                    "أهلاً أستاذ $sender! نعم بنوصل لـ ${matchedArea.name} ورسوم التوصيل ${matchedArea.fee}. تحب تطلب إيه النهاردة؟"
                } else {
                    "مرحباً $sender! نعم نغطي منطقة ${matchedArea.name} وتكلفة الشحن ${matchedArea.fee}. كيف يمكننا خدمتك اليوم؟"
                }
                return AutoReplyResult(ActionType.REPLY, reply, "Area and delivery found in knowledge base")
            }
        }

        // Working hours inquiry
        if (msg.contains("مواعيد", ignoreCase = true) || msg.contains("شغالين", ignoreCase = true) || msg.contains("فاتحين", ignoreCase = true)) {
            val reply = if (isEgyptian) {
                "أهلاً أستاذ $sender، مواعيد عملنا ${profile.workingHours.days} من الساعة ${profile.workingHours.startTime} لحد ${profile.workingHours.endTime}. نقدر نساعدك بإيه؟"
            } else {
                "مرحباً $sender، مواعيد العمل الرسمية هي ${profile.workingHours.days} من ${profile.workingHours.startTime} حتى ${profile.workingHours.endTime}. نسعد بخدمتك!"
            }
            return AutoReplyResult(ActionType.REPLY, reply, "Working hours in knowledge base")
        }

        // FAQs matched
        val matchedFaq = profile.faqs.firstOrNull { faq ->
            msg.contains(faq.question, ignoreCase = true) ||
                    faq.question.split(" ").filter { it.length > 3 }.any { msg.contains(it, ignoreCase = true) }
        }
        if (matchedFaq != null) {
            val reply = if (isEgyptian) {
                "أهلاً أستاذ $sender 🌟 ${matchedFaq.answer} تحب تستفسر عن أي شيء تاني؟"
            } else {
                "مرحباً $sender 🌟 ${matchedFaq.answer} يسعدنا الإجابة على أي استفسار آخر."
            }
            return AutoReplyResult(ActionType.REPLY, reply, "Matched FAQ in knowledge base")
        }

        // Offers inquiry
        if (msg.contains("عرض", ignoreCase = true) || msg.contains("خصم", ignoreCase = true) || msg.contains("عروض", ignoreCase = true)) {
            if (profile.activeOffer.title.isNotBlank()) {
                val reply = if (isEgyptian) {
                    "أهلاً أستاذ $sender! عرضنا الحالي: ${profile.activeOffer.title}، وساري حتى ${profile.activeOffer.validUntil}. تحب تستفيد بالعرض؟"
                } else {
                    "مرحباً $sender! عرضنا المميز الحالي: ${profile.activeOffer.title} حتى ${profile.activeOffer.validUntil}. هل تود الاستفادة منه؟"
                }
                return AutoReplyResult(ActionType.REPLY, reply, "Active offer in knowledge base")
            }
        }

        // Order steps inquiry
        if (msg.contains("اطلب ازاي", ignoreCase = true) || msg.contains("طريقة الطلب", ignoreCase = true) || msg.contains("احجز", ignoreCase = true)) {
            val reply = if (isEgyptian) {
                "أهلاً بك! طريقة الطلب سهلة جداً: ${profile.orderSteps}. تقدر كمان تتواصل مباشرة على واتساب: ${profile.whatsappNumber}."
            } else {
                "مرحباً بك! خطوات الطلب: ${profile.orderSteps}. كما يمكنك التواصل عبر واتساب: ${profile.whatsappNumber}."
            }
            return AutoReplyResult(ActionType.REPLY, reply, "Order steps from knowledge base")
        }

        // Information not found in Knowledge Base -> HANDOFF
        val handoffReply = if (isEgyptian) {
            "أهلاً أستاذ $sender، تفاصيل استفسارك محتاجة تأكيد من القسم المختص. ياريت تسيب لنا رقم تليفونك وفريقنا هيكلمك في أقرب وقت."
        } else {
            "مرحباً $sender، استفسارك يتطلب مراجعة من الفريق المختص. يرجى ترك رقم هاتفك وسيقوم زملاؤنا بالتواصل معك قريباً."
        }
        return AutoReplyResult(ActionType.HANDOFF, handoffReply, "Information not in knowledge base - escalated to human representative")
    }
}
