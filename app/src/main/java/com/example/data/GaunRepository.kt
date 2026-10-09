package com.example.data

import com.example.model.CommunityService
import com.example.model.EmergencyContact
import com.example.model.GaunLocation

class GaunRepository {

    fun getLocations(): List<GaunLocation> {
        return listOf(
            NepalAdministrativeData.defaultLocation,
            NepalAdministrativeData.findLocation("bagmati", "kathmandu", "kathmandu_met") ?: NepalAdministrativeData.defaultLocation,
            NepalAdministrativeData.findLocation("gandaki", "kaski", "pokhara") ?: NepalAdministrativeData.defaultLocation,
            NepalAdministrativeData.findLocation("koshi", "morang", "biratnagar") ?: NepalAdministrativeData.defaultLocation,
            NepalAdministrativeData.findLocation("lumbini", "rupandehi", "butwal") ?: NepalAdministrativeData.defaultLocation
        )
    }

    fun getEmergencyContacts(): List<EmergencyContact> {
        return listOf(
            EmergencyContact("ec_1", "वडा अध्यक्ष (Ward Chair)", "Ward Chairperson", "जनप्रतिनिधि", "Elected Representative", "9851011223", isPrimaryHelpline = true),
            EmergencyContact("ec_2", "वडा सचिव (Ward Secretary)", "Ward Secretary", "प्रशासनिक प्रमुख", "Administrative Officer", "9841022334", isPrimaryHelpline = true),
            EmergencyContact("ec_3", "स्थानीय प्रहरी चौकी (Local Police)", "Local Police Beat", "शान्ति सुरक्षा", "Security Incharge", "011-660100", isPrimaryHelpline = true),
            EmergencyContact("ec_4", "महिला स्वास्थ्य स्वयंसेविका (FCHV)", "Community Health Volunteer", "स्वास्थ्य सहायता", "Health Volunteer", "9860112233"),
            EmergencyContact("ec_5", "कृषि प्राविधिक (Agro Technician)", "JT / JTA Agro Tech", "कृषि परामर्श", "Agriculture Advisor", "9843998877"),
            EmergencyContact("ec_6", "पशु सेवा प्राविधिक (Vet Technician)", "Veterinary Technician", "पशु स्वास्थ्य", "Animal Health Advisor", "9841554433")
        )
    }

    fun getCommunityServices(): List<CommunityService> {
        return listOf(
            CommunityService(
                id = "srv_citizencard",
                titleNe = "नागरिकता प्रमाणपत्र सिफारिस",
                titleEn = "Citizenship Certificate Recommendation",
                categoryNe = "व्यक्तिगत तथा कानुनी",
                categoryEn = "Legal & Personal",
                descriptionNe = "नेपाली नागरिकताको प्रमाणपत्र प्राप्त गर्न वडा कार्यालयबाट गरिने आवश्यक सिफारिस।",
                descriptionEn = "Ward level recommendation to obtain Nepali citizenship certificate from District Administration.",
                requiredDocumentsNe = listOf(
                    "१. बाबु वा आमाको नागरिकता प्रमाणपत्रको सक्कल र प्रतिलिपि",
                    "२. जन्म दर्ता प्रमाणपत्रको सक्कल र प्रतिलिपि",
                    "३. शैक्षिक प्रमाणपत्र (एसएलसी/एसईई वा सो सरह)",
                    "४. हालसालै खिचिएको पासपोर्ट साइजको फोटो ३ प्रति",
                    "५. बसाइँसराइ भए बसाइँसराइको प्रमाणपत्र"
                ),
                requiredDocumentsEn = listOf(
                    "1. Original & copy of father or mother's citizenship",
                    "2. Original & copy of birth registration certificate",
                    "3. Educational certificate (SEE/SLC or equivalent)",
                    "4. 3 recent passport-sized photos",
                    "5. Migration certificate if relocated"
                ),
                feeNe = "निशुल्क (Free)",
                feeEn = "Free of charge",
                processingTimeNe = "सोही दिन (Same Day)",
                processingTimeEn = "Same day within 2 hours"
            ),
            CommunityService(
                id = "srv_birth_reg",
                titleNe = "जन्म दर्ता प्रमाणपत्र",
                titleEn = "Birth Registration Certificate",
                categoryNe = "व्यक्तिगत घटना दर्ता",
                categoryEn = "Vital Registration",
                descriptionNe = "शिशु जन्मेको ३५ दिनभित्र गरिने निशुल्क जन्म दर्ता।",
                descriptionEn = "Official registration of newborn birth within 35 days (Free of cost).",
                requiredDocumentsNe = listOf(
                    "१. अस्पतालको जन्म सम्बन्धी डिस्चार्ज स्लिप वा स्वास्थ्य चौकीको प्रमाण",
                    "२. बाबु र आमाको नागरिकताको प्रतिलिपि",
                    "३. बाबुआमाको विवाह दर्ता प्रमाणपत्रको प्रतिलिपि"
                ),
                requiredDocumentsEn = listOf(
                    "1. Hospital birth certificate or health center proof",
                    "2. Copies of parents' citizenship certificates",
                    "3. Copy of parents' marriage certificate"
                ),
                feeNe = "३५ दिनभित्र निशुल्क (३५ दिन कटेपछि रु. ५०)",
                feeEn = "Free within 35 days (Rs. 50 after 35 days)",
                processingTimeNe = "तुरुन्तै (Immediate)",
                processingTimeEn = "Immediate (approx 30 mins)"
            ),
            CommunityService(
                id = "srv_agri_subsidy",
                titleNe = "कृषि तथा पशुपालन अनुदान सिफारिस",
                titleEn = "Agriculture & Livestock Subsidy Recommendation",
                categoryNe = "कृषि तथा उद्यम",
                categoryEn = "Agriculture & Enterprise",
                descriptionNe = "उन्नत बीउ, मल, कृषि यन्त्र (मिनी टिलर) तथा पशुपालनका लागि सरकारी अनुदान सिफारिस।",
                descriptionEn = "Recommendation for government subsidies on seeds, fertilizer, mini-tillers, and livestock.",
                requiredDocumentsNe = listOf(
                    "१. जग्गाधनी प्रमाणपुर्जा (लालपुर्जा) वा जग्गा भाडा सम्झौता",
                    "२. कृषक समूह वा सहकारीको सिफारिस पत्र",
                    "३. आवेदकको नागरिकता प्रतिलिपि",
                    "४. कृषि/पशु व्यवसाय सम्बन्धी संक्षिप्त योजना"
                ),
                requiredDocumentsEn = listOf(
                    "1. Land ownership certificate (Lalpurja) or lease agreement",
                    "2. Farmer group or cooperative recommendation letter",
                    "3. Copy of applicant's citizenship",
                    "4. Brief proposal of farming / livestock activity"
                ),
                feeNe = "रु. १०० (राजस्व दस्तुर)",
                feeEn = "Rs. 100",
                processingTimeNe = "१ देखि २ दिन",
                processingTimeEn = "1 to 2 business days"
            ),
            CommunityService(
                id = "srv_social_security",
                titleNe = "सामाजिक सुरक्षा भत्ता दर्ता / नवीकरण",
                titleEn = "Social Security Allowance Registration & Renewal",
                categoryNe = "सामाजिक सुरक्षा",
                categoryEn = "Social Welfare",
                descriptionNe = "ज्येष्ठ नागरिक, एकल महिला, अपाङ्गता भएका व्यक्ति तथा बालबालिकाका लागि सामाजिक सुरक्षा भत्ता।",
                descriptionEn = "Registration and annual renewal of elderly, single women, disability and child allowances.",
                requiredDocumentsNe = listOf(
                    "१. नेपाली नागरिकताको प्रमाणपत्र प्रतिलिपि",
                    "२. अपाङ्गता परिचयपत्र (अपाङ्गता भएका व्यक्तिको हकमा)",
                    "३. बैंक खाता नम्बर विवरण",
                    "४. २ प्रति पासपोर्ट साइजको फोटो"
                ),
                requiredDocumentsEn = listOf(
                    "1. Copy of citizenship certificate",
                    "2. Disability ID card (if applicable)",
                    "3. Bank account details",
                    "4. 2 passport-sized photos"
                ),
                feeNe = "निशुल्क (Free)",
                feeEn = "Free of charge",
                processingTimeNe = "२४ घण्टाभित्र",
                processingTimeEn = "Within 24 hours"
            )
        )
    }
}
