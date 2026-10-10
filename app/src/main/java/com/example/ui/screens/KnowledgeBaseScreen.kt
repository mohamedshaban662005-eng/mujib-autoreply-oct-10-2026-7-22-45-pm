package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.ActionReplyGreen
import com.example.ui.viewmodel.AutoReplyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(
    viewModel: AutoReplyViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()

    // Local mutable state for editing
    var businessName by remember(profile) { mutableStateOf(profile.businessName) }
    var businessField by remember(profile) { mutableStateOf(profile.businessField) }
    var shortDesc by remember(profile) { mutableStateOf(profile.shortDescription) }
    var links by remember(profile) { mutableStateOf(profile.links) }
    var whatsapp by remember(profile) { mutableStateOf(profile.whatsappNumber) }
    var dialect by remember(profile) { mutableStateOf(profile.dialect) }

    var services by remember(profile) { mutableStateOf(profile.services) }
    var offerTitle by remember(profile) { mutableStateOf(profile.activeOffer.title) }
    var offerValid by remember(profile) { mutableStateOf(profile.activeOffer.validUntil) }
    var offerConditions by remember(profile) { mutableStateOf(profile.activeOffer.conditions) }

    var areas by remember(profile) { mutableStateOf(profile.areas) }
    var workDays by remember(profile) { mutableStateOf(profile.workingHours.days) }
    var workStart by remember(profile) { mutableStateOf(profile.workingHours.startTime) }
    var workEnd by remember(profile) { mutableStateOf(profile.workingHours.endTime) }

    var orderSteps by remember(profile) { mutableStateOf(profile.orderSteps) }
    var faqs by remember(profile) { mutableStateOf(profile.faqs) }

    var returnPolicy by remember(profile) { mutableStateOf(profile.returnPolicy) }
    var paymentPolicy by remember(profile) { mutableStateOf(profile.paymentPolicy) }
    var warrantyPolicy by remember(profile) { mutableStateOf(profile.warrantyPolicy) }

    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showAddAreaDialog by remember { mutableStateOf(false) }
    var showAddFaqDialog by remember { mutableStateOf(false) }
    var hasSavedFeedback by remember { mutableStateOf(false) }

    val onSaveAll = {
        val updated = profile.copy(
            businessName = businessName,
            businessField = businessField,
            shortDescription = shortDesc,
            links = links,
            whatsappNumber = whatsapp,
            dialect = dialect,
            services = services,
            activeOffer = OfferItem(offerTitle, offerValid, offerConditions),
            areas = areas,
            workingHours = WorkingHours(workDays, workStart, workEnd),
            orderSteps = orderSteps,
            faqs = faqs,
            returnPolicy = returnPolicy,
            paymentPolicy = paymentPolicy,
            warrantyPolicy = warrantyPolicy
        )
        viewModel.updateProfile(updated)
        hasSavedFeedback = true
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // Quick Presets Selector
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تحميل نموذج نشاط جاهز (Presets):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.applyPreset("ECOMMERCE") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("متجر", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.applyPreset("RESTAURANT") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مطعم", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.applyPreset("SERVICES") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("خدمات", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 1: Business Profile
        item {
            KBSectionCard(title = "1) نبذة عن النشاط والهوية", icon = Icons.Default.Storefront) {
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("اسم النشاط {{اسم_النشاط}}") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("kb_business_name_input")
                )
                OutlinedTextField(
                    value = businessField,
                    onValueChange = { businessField = it },
                    label = { Text("مجال النشاط {{مجال_النشاط}}") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shortDesc,
                    onValueChange = { shortDesc = it },
                    label = { Text("وصف مختصر للنشاط") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("رقم واتساب") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = links,
                        onValueChange = { links = it },
                        label = { Text("رابط الموقع / السوشيال") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dialect selector
                Text("أسلوب ولهجة التحدث:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = dialect == Dialect.EGYPTIAN,
                        onClick = { dialect = Dialect.EGYPTIAN },
                        label = { Text("لهجة مصرية مهذبة") },
                        leadingIcon = if (dialect == Dialect.EGYPTIAN) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = dialect == Dialect.STANDARD_ARABIC,
                        onClick = { dialect = Dialect.STANDARD_ARABIC },
                        label = { Text("عربية مبسطة (فصحى)") },
                        leadingIcon = if (dialect == Dialect.STANDARD_ARABIC) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        // Section 2: Services & Prices
        item {
            KBSectionCard(
                title = "2) الخدمات والأسعار (قائمة الأسعار)",
                icon = Icons.Default.PriceCheck,
                action = {
                    IconButton(onClick = { showAddServiceDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "إضافة خدمة", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            ) {
                if (services.isEmpty()) {
                    Text("لا توجد خدمات مضافة بعد. اضغط + لإضافة خدمة.", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                } else {
                    services.forEach { service ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(service.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (service.description.isNotBlank()) {
                                        Text(service.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        "${service.price} ${service.currency} ${if (service.notes.isNotBlank()) "(${service.notes})" else ""}",
                                        color = ActionReplyGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                IconButton(onClick = { services = services.filter { it.id != service.id } }) {
                                    Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Active Offer
        item {
            KBSectionCard(title = "3) العرض الترويجي الحالي", icon = Icons.Default.LocalOffer) {
                OutlinedTextField(
                    value = offerTitle,
                    onValueChange = { offerTitle = it },
                    label = { Text("نص العرض") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = offerValid,
                        onValueChange = { offerValid = it },
                        label = { Text("صلاحية العرض حتى") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = offerConditions,
                        onValueChange = { offerConditions = it },
                        label = { Text("شروط العرض") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 4: Areas & Working Hours
        item {
            KBSectionCard(
                title = "4) مناطق التوصيل ومواعيد العمل",
                icon = Icons.Default.LocationOn,
                action = {
                    IconButton(onClick = { showAddAreaDialog = true }) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = "إضافة منطقة", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            ) {
                Text("المناطق ورسوم التوصيل:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                areas.forEach { area ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ${area.name}: ${area.fee}", fontSize = 13.sp)
                        IconButton(onClick = { areas = areas.filter { it.id != area.id } }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("مواعيد العمل الرسمية:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = workDays,
                    onValueChange = { workDays = it },
                    label = { Text("أيام العمل") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = workStart,
                        onValueChange = { workStart = it },
                        label = { Text("من الساعة") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = workEnd,
                        onValueChange = { workEnd = it },
                        label = { Text("إلى الساعة") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 5: Order Steps & FAQs
        item {
            KBSectionCard(
                title = "5) طريقة الطلب والأسئلة الشائعة",
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                action = {
                    IconButton(onClick = { showAddFaqDialog = true }) {
                        Icon(Icons.Default.AddComment, contentDescription = "إضافة سؤال", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            ) {
                OutlinedTextField(
                    value = orderSteps,
                    onValueChange = { orderSteps = it },
                    label = { Text("خطوات تقديم الطلب") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("الأسئلة الشائعة المعتمدة:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                faqs.forEach { faq ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("س: ${faq.question}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("ج: ${faq.answer}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { faqs = faqs.filter { it.id != faq.id } }) {
                                Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // Section 6: Policies
        item {
            KBSectionCard(title = "6) سياسات المتجر (الاسترجاع والدفع والضمان)", icon = Icons.Default.Policy) {
                OutlinedTextField(
                    value = returnPolicy,
                    onValueChange = { returnPolicy = it },
                    label = { Text("سياسة الاسترجاع والاستبدال") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paymentPolicy,
                    onValueChange = { paymentPolicy = it },
                    label = { Text("طرق الدفع المتاحة") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = warrantyPolicy,
                    onValueChange = { warrantyPolicy = it },
                    label = { Text("تفاصيل الضمان والجودة") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Save Button
        item {
            Button(
                onClick = onSaveAll,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_kb_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ وتحديث قاعدة المعرفة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(visible = hasSavedFeedback) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2500)
                    hasSavedFeedback = false
                }
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = ActionReplyGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "تم حفظ قاعدة المعرفة وتحديث برومبت الذكاء الاصطناعي بنجاح!",
                            color = Color(0xFF1B5E20),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Add Service Dialog
    if (showAddServiceDialog) {
        var name by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddServiceDialog = false },
            title = { Text("إضافة خدمة / منتج جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الخدمة") })
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("السعر (مثال: 350)") })
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات (اختياري)") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            services = services + ServiceItem(name = name, price = price, notes = notes, description = desc)
                            showAddServiceDialog = false
                        }
                    }
                ) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showAddServiceDialog = false }) { Text("إلغاء") } }
        )
    }

    // Add Area Dialog
    if (showAddAreaDialog) {
        var areaName by remember { mutableStateOf("") }
        var areaFee by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddAreaDialog = false },
            title = { Text("إضافة منطقة توصيل") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = areaName, onValueChange = { areaName = it }, label = { Text("اسم المنطقة") })
                    OutlinedTextField(value = areaFee, onValueChange = { areaFee = it }, label = { Text("رسوم التوصيل (مثال: 40 جنيه)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (areaName.isNotBlank()) {
                            areas = areas + AreaItem(name = areaName, fee = areaFee)
                            showAddAreaDialog = false
                        }
                    }
                ) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showAddAreaDialog = false }) { Text("إلغاء") } }
        )
    }

    // Add FAQ Dialog
    if (showAddFaqDialog) {
        var q by remember { mutableStateOf("") }
        var a by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddFaqDialog = false },
            title = { Text("إضافة سؤال شائع وجوابه") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = q, onValueChange = { q = it }, label = { Text("السؤال") })
                    OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("الإجابة المعتمدة") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (q.isNotBlank() && a.isNotBlank()) {
                            faqs = faqs + FaqItem(question = q, answer = a)
                            showAddFaqDialog = false
                        }
                    }
                ) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showAddFaqDialog = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
fun KBSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                action?.invoke()
            }
            content()
        }
    }
}
