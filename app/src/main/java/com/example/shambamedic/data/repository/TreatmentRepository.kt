package com.example.shambamedic.data.repository

import com.example.shambamedic.data.local.dao.DiseaseDao
import com.example.shambamedic.data.local.dao.TreatmentDao
import com.example.shambamedic.data.local.entity.DiseaseEntity
import com.example.shambamedic.data.local.entity.TreatmentEntity
import com.example.shambamedic.domain.model.Disease
import com.example.shambamedic.domain.model.Treatment
import com.example.shambamedic.domain.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TreatmentRepository(
    private val treatmentDao: TreatmentDao,
    private val diseaseDao: DiseaseDao
) {

    suspend fun getTreatmentsForDisease(diseaseId: String): List<Treatment> {
        return treatmentDao.getTreatmentsForDisease(diseaseId).map { it.toDomain() }
    }

    suspend fun getDiseaseById(diseaseId: String): Disease? {
        return diseaseDao.getDiseaseById(diseaseId)?.toDomain()
    }

    suspend fun getDiseaseByName(diseaseName: String): Disease? {
        return diseaseDao.getDiseaseByName(diseaseName)?.toDomain()
    }

    fun getAllDiseases(): Flow<List<Disease>> {
        return diseaseDao.getAllDiseases().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun seedDiseaseData() {
        // Checked against the most recently added disease (not disease_maize_001) so that
        // devices which already ran this seed before disease_tomato_004 existed still get
        // backfilled, instead of short-circuiting forever on stale local data.
        if (diseaseDao.getDiseaseById("disease_tomato_004") != null) return

        val diseases = listOf(
            DiseaseEntity("disease_maize_001", "Maize Lethal Necrosis (MLN)", "maize", "Yellowing and browning of leaves starting from tips, severe necrosis, stunted growth, premature death of the plant", "High - can cause 100% yield loss"),
            DiseaseEntity("disease_maize_002", "Gray Leaf Spot", "maize", "Rectangular grey to tan lesions running parallel to leaf veins, premature leaf death", "Medium - causes 10-50% yield loss"),
            DiseaseEntity("disease_maize_003", "Common Rust", "maize", "Small circular to elongated brown pustules on both leaf surfaces, powdery orange-brown spore masses", "Low to Medium - causes 5-30% yield loss"),
            DiseaseEntity("disease_potato_001", "Potato Late Blight", "potato", "Dark water-soaked lesions on leaves turning brown with white fungal growth on undersides in humid conditions, rapid plant collapse", "Very High - can cause complete crop failure within days"),
            DiseaseEntity("disease_potato_002", "Potato Early Blight", "potato", "Dark brown lesions with concentric rings forming a target-board pattern on older leaves, yellowing around lesions", "Medium - causes 20-30% yield loss"),
            DiseaseEntity("disease_tomato_001", "Tomato Bacterial Wilt", "tomato", "Sudden wilting of leaves without yellowing, brown discoloration of stem vascular tissue, white bacterial ooze when stem cut and placed in water", "Very High - no effective chemical cure once infected"),
            DiseaseEntity("disease_tomato_002", "Tomato Early Blight", "tomato", "Dark brown lesions with concentric rings on lower leaves, yellowing, defoliation starting from bottom of plant", "Medium - causes 30-50% yield loss"),
            DiseaseEntity("disease_tomato_003", "Tomato Leaf Mold", "tomato", "Pale green to yellow spots on upper leaf surface with olive-green to grey mold on undersides, leaf curl and drop", "Medium - causes significant yield loss in humid conditions"),
            DiseaseEntity("disease_tomato_004", "Tomato Late Blight", "tomato", "Dark, water-soaked lesions on leaves and stems that rapidly enlarge and turn brown-black, white fungal growth on leaf undersides in humid conditions, firm brown lesions on fruit, rapid plant collapse", "Very High - can destroy a field within days under humid conditions")
        )
        diseaseDao.insertAllDiseases(diseases)

        // treatmentId is a freshly generated UUID every call, so re-running this after a
        // partial seed would duplicate treatments for diseases that were already seeded;
        // clearing first keeps this idempotent.
        treatmentDao.deleteAllTreatments()

        val treatments = listOf(
            TreatmentEntity(UUID.randomUUID().toString(), "disease_maize_001", "Cultural Control", "Certified MLN-tolerant seed varieties (DK8031, H614D)", "Plant resistant varieties, practice crop rotation", "As per planting guidelines", "KSh 800 - 1500 per 2kg packet"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_maize_001", "Vector Control", "Duduthrin 15EC, Kingcode Elite 50EC", "Foliar spray targeting thrips and aphids", "20ml per 20L of water", "KSh 350 - 600 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_maize_002", "Fungicide Application", "Amistar 250SC, Score 250EC", "Foliar spray at first sign of disease", "10ml per 20L of water", "KSh 500 - 900 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_maize_003", "Fungicide Application", "Tilt 250EC, Bumper 250EC", "Foliar spray at early infection stage", "10ml per 20L of water", "KSh 400 - 750 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_potato_001", "Fungicide Application", "Ridomil Gold 68WG, Dithane M45", "Preventive foliar spray every 7-14 days during wet season", "2.5g per litre of water", "KSh 450 - 800 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_potato_001", "Cultural Control", "Certified disease-free seed tubers", "Use certified seed, destroy infected plants, avoid overhead irrigation", "N/A", "KSh 3000 - 6000 per 50kg bag"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_potato_002", "Fungicide Application", "Mancozeb 80WP, Bravo 500SC", "Foliar spray at first sign of symptoms", "2g per litre of water", "KSh 300 - 550 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_tomato_001", "Cultural Control", "Resistant varieties (Tengeru 97, Onyx F1)", "Remove and destroy infected plants, avoid waterlogging, practice 3-year crop rotation", "N/A", "KSh 500 - 1200 per seed packet"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_tomato_001", "Soil Treatment", "Copper-based bactericides (Kocide 2000)", "Soil drench around plant base", "3g per litre of water", "KSh 600 - 1000 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_tomato_002", "Fungicide Application", "Dithane M45, Milraz 72WP", "Foliar spray every 7 days from early infection", "2g per litre of water", "KSh 350 - 650 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_tomato_003", "Fungicide Application", "Amistar 250SC, Cantus 50WG", "Foliar spray at first sign of mold", "10ml per 20L of water", "KSh 500 - 900 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_tomato_003", "Environmental Control", "N/A", "Improve greenhouse ventilation, reduce humidity below 85%, avoid wetting foliage", "N/A", "No direct cost"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_tomato_004", "Fungicide Application", "Ridomil Gold 68WG, Milraz 72WP", "Preventive foliar spray every 7-10 days during humid/wet season", "2.5g per litre of water", "KSh 450 - 800 per application"),
            TreatmentEntity(UUID.randomUUID().toString(), "disease_tomato_004", "Cultural Control", "Resistant varieties, staking for airflow", "Remove and destroy infected plants immediately, avoid overhead irrigation, practice crop rotation away from potatoes", "N/A", "KSh 500 - 1200 per seed packet")
        )
        treatmentDao.insertAllTreatments(treatments)
    }
}
