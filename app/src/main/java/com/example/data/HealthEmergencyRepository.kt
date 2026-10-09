package com.example.data

import com.example.model.AmbulanceServiceInfo
import com.example.model.BasicHealthGuideline
import com.example.model.EmergencyServiceNumber
import com.example.model.GaunLocation
import com.example.model.HealthFacilitiesUiState
import com.example.model.HealthFacility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface HealthEmergencyRepository {
    fun getEmergencyNumbers(): List<EmergencyServiceNumber>
    fun getAmbulanceServices(location: GaunLocation): List<AmbulanceServiceInfo>
    fun getNearbyHealthFacilities(location: GaunLocation): Flow<HealthFacilitiesUiState>
    fun getBasicHealthGuidelines(): List<BasicHealthGuideline>
}

class ConfigurableHealthEmergencyRepository : HealthEmergencyRepository {

    override fun getEmergencyNumbers(): List<EmergencyServiceNumber> {
        return listOf(
            EmergencyServiceNumber("100", "नेपाल प्रहरी (Police)", "Nepal Police", "100", true, "शान्ति सुरक्षा तथा आपतकाल", "Crime, peace & emergency assistance"),
            EmergencyServiceNumber("102", "एम्बुलेन्स सेवा (Ambulance)", "Nepal Ambulance", "102", true, "तत्काल स्वास्थ्य उद्धार सेवा", "Medical emergency dispatch"),
            EmergencyServiceNumber("101", "दमकल सेवा (Fire Brigade)", "Fire Department", "101", true, "आगलागी नियन्त्रण सेवा", "Fire response service"),
            EmergencyServiceNumber("1149", "विपद् व्यवस्थापन (Disaster SOS)", "National Disaster Helpline", "1149", true, "बाढी, पहिरो तथा विपद् सूचना", "Disaster management hotline"),
            EmergencyServiceNumber("104", "बाल हेल्पलाइन (Child Helpline)", "Child Helpline Nepal", "104", false, "बाल अधिकार तथा उद्धार", "Child protection & rescue"),
            EmergencyServiceNumber("1145", "महिला हेल्पलाइन (Women Helpline)", "Women Helpline Nepal", "1145", false, "महिला हिंसा विरुद्ध सहायता", "Women safety & legal aid")
        )
    }

    override fun getAmbulanceServices(location: GaunLocation): List<AmbulanceServiceInfo> {
        val name = location.nameNe
        val nameEn = location.nameEn
        return listOf(
            AmbulanceServiceInfo(
                id = "amb_1",
                providerNameNe = "$name रेडक्रस एम्बुलेन्स सेवा",
                providerNameEn = "$nameEn Red Cross Ambulance",
                contactNumber = "9851000102",
                secondaryContact = "011-660102",
                baseStationNe = "केन्द्रीय स्वास्थ्य केन्द्र",
                baseStationEn = "Central Health Post",
                isAvailableNow = true
            ),
            AmbulanceServiceInfo(
                id = "amb_2",
                providerNameNe = "सामुदायिक विपद् एम्बुलेन्स",
                providerNameEn = "Community Emergency Ambulance",
                contactNumber = "9841234567",
                baseStationNe = "प्राथमिक स्वास्थ्य केन्द्र",
                baseStationEn = "Primary Health Center",
                isAvailableNow = true
            )
        )
    }

    override fun getNearbyHealthFacilities(location: GaunLocation): Flow<HealthFacilitiesUiState> = flow {
        emit(HealthFacilitiesUiState.Loading)
        val list = getFacilitiesForLocation(location)
        val ambulances = getAmbulanceServices(location)
        emit(HealthFacilitiesUiState.Success(list, ambulances))
    }

    override fun getBasicHealthGuidelines(): List<BasicHealthGuideline> {
        return listOf(
            BasicHealthGuideline(
                id = "snake_bite",
                titleNe = "सर्पदंश (सर्पले टोकेमा) प्राथमिक उपचार",
                titleEn = "Snake Bite First Aid",
                summaryNe = "डराउनु हुँदैन, बिरामीलाई नहल्लाउनुहोस् र तुरुन्तै नजिकको एन्टी-भेनम भएको अस्पताल पुर्याउनुहोस्।",
                summaryEn = "Keep victim calm and still, immobilize limb, and rush immediately to anti-snake venom center.",
                stepsNe = listOf(
                    "१. बिरामीलाई शान्त राख्ने र टोकेको भागलाई स्थिर राख्ने।",
                    "२. टोकेको ठाउँमा ब्लेडले काट्ने वा मुखले रगत चुस्ने काम कदापि नगर्ने।",
                    "३. कसिलो डोरी वा तारले नबाँध्ने, खुकुलो पट्टी मात्र लगाउने।",
                    "४. जतिसक्दो चाँडो नजिकको सर्पदंश उपचार केन्द्र वा अस्पताल पुर्याउने।"
                ),
                stepsEn = listOf(
                    "1. Keep the patient calm and immobilize the bitten limb.",
                    "2. Do NOT cut, burn, or attempt to suck venom from the wound.",
                    "3. Do NOT tie tight tourniquets.",
                    "4. Transport immediately to an anti-snake venom medical center."
                ),
                warningNe = "धामी-झाँक्रीको भरमा समय खेर नफाल्नुहोला!",
                warningEn = "Do not waste time with traditional healers!"
            ),
            BasicHealthGuideline(
                id = "diarrhea_ors",
                titleNe = "झाडापखाला र जलवियोजन (जीवनजल बनाउने विधि)",
                titleEn = "Diarrhea & Dehydration (ORS Preparation)",
                summaryNe = "लगातार पातलो दिसा-वान्ता भएमा प्रशस्त जीवनजल र झोलिलो खानेकुरा खुवाउनुपर्छ।",
                summaryEn = "Prevent dehydration by administering Oral Rehydration Solution (ORS) frequently.",
                stepsNe = listOf(
                    "१. १ लिटर उमालेर चिसो पारेको पानीमा एक पुरिया जीवनजल राम्ररी घोल्ने।",
                    "२. हरेक पटक पातलो दिसा भएपछि एक गिलास जीवनजल खुवाउने।",
                    "३. बालबालिकालाई आमाको दूध र पोसिलो गेडागुडीको झोल निरन्तर खुवाउने।",
                    "४. आँखा गढ्ने वा होस हराउने लक्षण देखिएमा तुरुन्त स्वास्थ्य चौकी लैजाने।"
                ),
                stepsEn = listOf(
                    "1. Dissolve 1 sachet of ORS into 1 liter of safe drinking water.",
                    "2. Give one glass of ORS after every loose bowel movement.",
                    "3. Continue regular breastfeeding and fluids for infants.",
                    "4. Seek emergency medical care if lethargic or unresponsive."
                )
            ),
            BasicHealthGuideline(
                id = "cpr_faint",
                titleNe = "अचेत वा बेहोस भएको अवस्थामा प्राथमिक उपचार",
                titleEn = "Unconsciousness & Fainting First Aid",
                summaryNe = "श्वासप्रश्वास जाँच गर्नुहोस् र बिरामीलाई सुरक्षित कोल्टे अवस्थामा राख्नुहोस्।",
                summaryEn = "Check airway and breathing; place in recovery position and call ambulance.",
                stepsNe = listOf(
                    "१. बिरामीको काँध बिस्तारै हल्लाएर होस भए नभएको जाँच्ने।",
                    "२. नाक र छाती नजिक कान लगेर श्वास फेरेको नफेरेको हेर्ने।",
                    "३. श्वास चलिरहेको छ भने कोल्टे पारी सुताउने (रिकभरी पोजिसन)।",
                    "४. तुरुन्त १०२ मा फोन गरी एम्बुलेन्स बोलाउने।"
                ),
                stepsEn = listOf(
                    "1. Check responsiveness by gently tapping shoulders.",
                    "2. Check breathing and clear airways.",
                    "3. Place in recovery position if breathing.",
                    "4. Immediately call 102 for emergency ambulance."
                )
            )
        )
    }

    private fun getFacilitiesForLocation(location: GaunLocation): List<HealthFacility> {
        val name = location.nameNe
        val nameEn = location.nameEn
        val ward = location.wardNumber ?: 1
        return listOf(
            HealthFacility(
                id = "hf_1",
                nameNe = "$name प्राथमिक स्वास्थ्य केन्द्र",
                nameEn = "$nameEn Primary Health Center",
                typeNe = "प्राथमिक स्वास्थ्य केन्द्र",
                typeEn = "Primary Health Center",
                locationName = name,
                wardNumber = 1,
                contactNumber = "011-660111",
                is24Hours = true,
                distanceKm = 1.2,
                doctorAvailable = true,
                servicesAvailableNe = listOf("२४ घण्टे आकस्मिक सेवा", "प्रसूति सेवा (बर्थिङ सेन्टर)", "प्रयोगशाला (Lab)", "औषधि वितरण"),
                servicesAvailableEn = listOf("24/7 Emergency", "Birthing Center", "Laboratory", "Free Medicines")
            ),
            HealthFacility(
                id = "hf_2",
                nameNe = "वडा नं $ward आधारभूत स्वास्थ्य चौकी",
                nameEn = "Ward $ward Basic Health Post",
                typeNe = "स्वास्थ्य चौकी",
                typeEn = "Health Post",
                locationName = name,
                wardNumber = ward,
                contactNumber = "9841001122",
                is24Hours = false,
                distanceKm = 0.5,
                doctorAvailable = false,
                servicesAvailableNe = listOf("सामान्य उपचार", "खोप तथा पोषण सेवा", "परिवार नियोजन", "निशुल्क आधारभूत औषधि"),
                servicesAvailableEn = listOf("General Checkup", "Vaccines & Nutrition", "Family Planning", "Basic Medicines")
            ),
            HealthFacility(
                id = "hf_3",
                nameNe = "जिल्ला अस्पताल",
                nameEn = "District Hospital",
                typeNe = "जिल्ला अस्पताल",
                typeEn = "District Hospital",
                locationName = location.districtNe,
                wardNumber = null,
                contactNumber = "011-660222",
                is24Hours = true,
                distanceKm = 14.5,
                doctorAvailable = true,
                servicesAvailableNe = listOf("शल्यक्रिया (Surgery)", "आईसीयू (ICU)", "एक्स-रे तथा अल्ट्रासाउन्ड", "सर्पदंश उपचार केन्द्र"),
                servicesAvailableEn = listOf("General Surgery", "ICU", "X-Ray & Ultrasound", "Anti-Snake Venom Unit")
            )
        )
    }
}
