package com.example.data

import com.example.model.GaunLocation
import com.example.model.LocalNotice
import com.example.model.NoticeCategory
import com.example.model.NoticePriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface NoticeRepository {
    fun getNoticesFlow(location: GaunLocation): Flow<List<LocalNotice>>
    suspend fun fetchNotices(location: GaunLocation): Result<List<LocalNotice>>
}

class LocalNoticeRepository : NoticeRepository {

    private val noticesFlow = MutableStateFlow<List<LocalNotice>>(emptyList())

    init {
        noticesFlow.value = getDefaultNotices()
    }

    override fun getNoticesFlow(location: GaunLocation): Flow<List<LocalNotice>> {
        if (noticesFlow.value.isEmpty()) {
            noticesFlow.value = getDefaultNotices(location)
        }
        return noticesFlow.asStateFlow()
    }

    override suspend fun fetchNotices(location: GaunLocation): Result<List<LocalNotice>> {
        val list = getDefaultNotices(location)
        noticesFlow.value = list
        return Result.success(list)
    }

    fun getDemoFormatTemplates(location: GaunLocation): List<LocalNotice> = getDefaultNotices(location)

    private fun getDefaultNotices(location: GaunLocation? = null): List<LocalNotice> {
        val locName = location?.nameNe ?: "हेलम्बु गाउँपालिका"
        val locNameEn = location?.nameEn ?: "Helambu Rural Municipality"
        return listOf(
            LocalNotice(
                id = "demo_agri_1",
                title = "जैविक विषादी तथा गहुँको उन्नत बीउ वितरण कार्यक्रम",
                titleEn = "Distribution of Organic Pesticides and Improved Wheat Seeds",
                description = "कृषि शाखाबाट ५०% अनुदानमा उन्नत बीउ तथा जैविक कीटनाशक वितरण भइरहेको छ। इच्छुक कृषकहरूले नागरिकता र जग्गाधनी प्रमाणपुर्जासहित वडा कार्यालयमा सम्पर्क राख्नुहोला।",
                descriptionEn = "Agriculture Section is distributing improved seeds and organic bio-pesticides at 50% subsidy. Interested farmers are requested to visit ward office with citizenship and land certificate.",
                dateIso = "2026-09-28",
                category = NoticeCategory.AGRICULTURE,
                priority = NoticePriority.HIGH,
                locationName = locName,
                wardNumber = location?.wardNumber ?: 1,
                publisher = "$locName कृषि शाखा"
            ),
            LocalNotice(
                id = "demo_health_1",
                title = "बालबालिकाहरूका लागि नियमित पोलियो तथा दादुरा-रुबेला खोप अभियान",
                titleEn = "Child Routine Immunization & Polio Campaign",
                description = "५ वर्ष मुनिका सम्पूर्ण बालबालिकाहरूका लागि आगामी शुक्रबार सबै वडाका स्वास्थ्य चौकी तथा खोप केन्द्रहरूमा नियमित खोप सञ्चालन हुनेछ। खोप कार्ड लिएर आउनुहोला।",
                descriptionEn = "Routine vaccination for all children under 5 years will be conducted this Friday across all ward health posts. Please bring vaccination cards.",
                dateIso = "2026-09-27",
                category = NoticeCategory.HEALTH,
                priority = NoticePriority.URGENT,
                locationName = locName,
                wardNumber = null,
                publisher = "$locName स्वास्थ्य शाखा"
            ),
            LocalNotice(
                id = "demo_admin_1",
                title = "राष्ट्रिय परिचयपत्र तथा व्यक्तिगत घटना दर्ता सम्बन्धी वडा सूचना",
                titleEn = "Ward Notice on National ID & Vital Registration",
                description = "राष्ट्रिय परिचयपत्र विवरण संकलन तथा सामाजिक सुरक्षा भत्ता नवीकरण सम्बन्धी शिविर यसै हप्ता वडा कार्यालयमा आयोजना गरिएको छ। समयमै दर्ता गर्नुहोला।",
                descriptionEn = "National Identity Card biometric enrollment camp and social security allowance renewal is active at the ward office. Please register on time.",
                dateIso = "2026-09-25",
                category = NoticeCategory.CIVIC,
                priority = NoticePriority.NORMAL,
                locationName = locName,
                wardNumber = location?.wardNumber ?: 1,
                publisher = "$locName वडा कार्यालय"
            ),
            LocalNotice(
                id = "demo_disaster_1",
                title = "पहाडी तथा नदी तटीय क्षेत्रमा बाढी-पहिरोको सतर्कता सूचना",
                titleEn = "Monsoon Flash Flood & Landslide Warning",
                description = "जल तथा मौसम विज्ञान विभागको पूर्वानुमान अनुसार लगातार भारी वर्षा हुनसक्ने भएकाले खोला किनार र भिरालो जमिनमा बसोबास गर्ने सबै नागरिकहरूलाई उच्च सतर्कता अपनाउन अनुरोध छ।",
                descriptionEn = "Due to heavy rainfall forecast, residents near riverbanks and steep slopes are advised to remain on high alert. Keep emergency numbers handy.",
                dateIso = "2026-09-26",
                category = NoticeCategory.DISASTER,
                priority = NoticePriority.URGENT,
                locationName = locName,
                wardNumber = null,
                publisher = "विपद् व्यवस्थापन समिति"
            )
        )
    }
}
