package com.example.data.local

import com.example.data.local.dao.LearningDao
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.LearningResourceEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.TopicEntity

object LearningInitialData {

    suspend fun populateInitialDataIfEmpty(learningDao: LearningDao) {
        val existingTopics = learningDao.getTopicsDirect("neet_phy_current_electricity")
        if (existingTopics.isNotEmpty()) return

        // 1. Initial Chapters for NEET (Physics, Chemistry, Biology)
        val chapters = listOf(
            // --- PHYSICS ---
            ChapterEntity(
                id = "neet_phy_physical_world",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Physical World & Measurement",
                orderIndex = 1,
                totalTopics = 12,
                completedTopics = 8,
                totalQuestions = 40,
                attemptedQuestions = 20,
                correctAttempts = 16
            ),
            ChapterEntity(
                id = "neet_phy_kinematics",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Kinematics",
                orderIndex = 2,
                totalTopics = 14,
                completedTopics = 0,
                totalQuestions = 50,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_laws_of_motion",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Laws of Motion",
                orderIndex = 3,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 45,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_work_energy_power",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Work, Energy & Power",
                orderIndex = 4,
                totalTopics = 14,
                completedTopics = 0,
                totalQuestions = 45,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_current_electricity",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Current Electricity",
                orderIndex = 5,
                totalTopics = 12,
                completedTopics = 7,
                totalQuestions = 45,
                attemptedQuestions = 24,
                correctAttempts = 19,
                lastAccessedTimestamp = System.currentTimeMillis()
            ),
            ChapterEntity(
                id = "neet_phy_system_of_particles",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "System of Particles & Rotational Motion",
                orderIndex = 6,
                totalTopics = 14,
                completedTopics = 0,
                totalQuestions = 45,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_gravitation",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Gravitation",
                orderIndex = 7,
                totalTopics = 10,
                completedTopics = 0,
                totalQuestions = 35,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_properties_of_matter",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Properties of Bulk Matter",
                orderIndex = 8,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_thermodynamics",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Thermodynamics",
                orderIndex = 9,
                totalTopics = 10,
                completedTopics = 3,
                totalQuestions = 38,
                attemptedQuestions = 8,
                correctAttempts = 5
            ),
            ChapterEntity(
                id = "neet_phy_oscillations_waves",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Oscillations & Waves",
                orderIndex = 10,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_electrostatics",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Electrostatics & Capacitance",
                orderIndex = 11,
                totalTopics = 12,
                completedTopics = 4,
                totalQuestions = 45,
                attemptedQuestions = 10,
                correctAttempts = 7
            ),
            ChapterEntity(
                id = "neet_phy_magnetism",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Moving Charges & Magnetism",
                orderIndex = 12,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 42,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_optics",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Ray Optics & Optical Instruments",
                orderIndex = 13,
                totalTopics = 12,
                completedTopics = 2,
                totalQuestions = 50,
                attemptedQuestions = 5,
                correctAttempts = 3
            ),
            ChapterEntity(
                id = "neet_phy_modern_physics",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Dual Nature & Atoms & Nuclei",
                orderIndex = 14,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 45,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_phy_semiconductors",
                examId = "NEET",
                subjectId = "PHYSICS",
                name = "Semiconductor Electronics",
                orderIndex = 15,
                totalTopics = 10,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),

            // --- CHEMISTRY ---
            ChapterEntity(
                id = "neet_chem_basic_concepts",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Some Basic Concepts of Chemistry",
                orderIndex = 1,
                totalTopics = 10,
                completedTopics = 5,
                totalQuestions = 35,
                attemptedQuestions = 10,
                correctAttempts = 8
            ),
            ChapterEntity(
                id = "neet_chem_structure_of_atom",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Structure of Atom",
                orderIndex = 2,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_chem_chemical_bonding",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Chemical Bonding & Molecular Structure",
                orderIndex = 3,
                totalTopics = 14,
                completedTopics = 0,
                totalQuestions = 45,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_chem_thermodynamics",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Chemical Thermodynamics",
                orderIndex = 4,
                totalTopics = 10,
                completedTopics = 4,
                totalQuestions = 40,
                attemptedQuestions = 12,
                correctAttempts = 9,
                lastAccessedTimestamp = System.currentTimeMillis() - 86400000
            ),
            ChapterEntity(
                id = "neet_chem_equilibrium",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Chemical & Ionic Equilibrium",
                orderIndex = 5,
                totalTopics = 12,
                completedTopics = 3,
                totalQuestions = 45,
                attemptedQuestions = 10,
                correctAttempts = 7
            ),
            ChapterEntity(
                id = "neet_chem_electrochemistry",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Redox Reactions & Electrochemistry",
                orderIndex = 6,
                totalTopics = 10,
                completedTopics = 2,
                totalQuestions = 35,
                attemptedQuestions = 5,
                correctAttempts = 3
            ),
            ChapterEntity(
                id = "neet_chem_solutions",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Solutions & Colligative Properties",
                orderIndex = 7,
                totalTopics = 10,
                completedTopics = 0,
                totalQuestions = 35,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_chem_kinetics",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Chemical Kinetics",
                orderIndex = 8,
                totalTopics = 10,
                completedTopics = 0,
                totalQuestions = 35,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_chem_coordination",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Coordination Compounds",
                orderIndex = 9,
                totalTopics = 10,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_chem_organic_basics",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "General Organic Chemistry (GOC)",
                orderIndex = 10,
                totalTopics = 14,
                completedTopics = 8,
                totalQuestions = 55,
                attemptedQuestions = 25,
                correctAttempts = 22
            ),
            ChapterEntity(
                id = "neet_chem_hydrocarbons",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Hydrocarbons",
                orderIndex = 11,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_chem_functional_groups",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                name = "Organic Functional Groups & Biomolecules",
                orderIndex = 12,
                totalTopics = 14,
                completedTopics = 0,
                totalQuestions = 45,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),

            // --- BIOLOGY ---
            ChapterEntity(
                id = "neet_bio_living_world",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "The Living World & Biological Classification",
                orderIndex = 1,
                totalTopics = 10,
                completedTopics = 7,
                totalQuestions = 40,
                attemptedQuestions = 20,
                correctAttempts = 18
            ),
            ChapterEntity(
                id = "neet_bio_plant_animal_kingdom",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Plant Kingdom & Animal Kingdom",
                orderIndex = 2,
                totalTopics = 14,
                completedTopics = 0,
                totalQuestions = 50,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_bio_cell_cycle",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Cell Biology & Cell Division",
                orderIndex = 3,
                totalTopics = 10,
                completedTopics = 7,
                totalQuestions = 40,
                attemptedQuestions = 28,
                correctAttempts = 26
            ),
            ChapterEntity(
                id = "neet_bio_plant_physiology",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Plant Physiology",
                orderIndex = 4,
                totalTopics = 12,
                completedTopics = 4,
                totalQuestions = 42,
                attemptedQuestions = 15,
                correctAttempts = 11
            ),
            ChapterEntity(
                id = "neet_bio_human_physio",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Human Physiology",
                orderIndex = 5,
                totalTopics = 15,
                completedTopics = 11,
                totalQuestions = 65,
                attemptedQuestions = 45,
                correctAttempts = 41,
                lastAccessedTimestamp = System.currentTimeMillis() - 43200000
            ),
            ChapterEntity(
                id = "neet_bio_reproduction",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Reproduction in Flowering Plants & Humans",
                orderIndex = 6,
                totalTopics = 14,
                completedTopics = 0,
                totalQuestions = 45,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_bio_genetics",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Genetics & Molecular Basis of Inheritance",
                orderIndex = 7,
                totalTopics = 14,
                completedTopics = 8,
                totalQuestions = 55,
                attemptedQuestions = 30,
                correctAttempts = 27
            ),
            ChapterEntity(
                id = "neet_bio_evolution",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Evolution & Human Health",
                orderIndex = 8,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_bio_biotechnology",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Biotechnology: Principles & Applications",
                orderIndex = 9,
                totalTopics = 10,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            ),
            ChapterEntity(
                id = "neet_bio_ecology",
                examId = "NEET",
                subjectId = "BIOLOGY",
                name = "Ecology & Environmental Issues",
                orderIndex = 10,
                totalTopics = 12,
                completedTopics = 0,
                totalQuestions = 40,
                attemptedQuestions = 0,
                correctAttempts = 0
            )
        )
        learningDao.insertChapters(chapters)

        // 2. Structured Topics for "Current Electricity" (NEET Physics Core)
        val currentElectricityTopics = listOf(
            TopicEntity("neet_phy_curr_01", "neet_phy_current_electricity", "Basic Concepts of Charge", 1, "COMPLETED"),
            TopicEntity("neet_phy_curr_02", "neet_phy_current_electricity", "Electric Current & Current Density", 2, "COMPLETED"),
            TopicEntity("neet_phy_curr_03", "neet_phy_current_electricity", "Drift Velocity & Mobility", 3, "COMPLETED"),
            TopicEntity("neet_phy_curr_04", "neet_phy_current_electricity", "Ohm's Law & Limitations", 4, "COMPLETED"),
            TopicEntity("neet_phy_curr_05", "neet_phy_current_electricity", "Resistance & Resistivity", 5, "IN_PROGRESS"),
            TopicEntity("neet_phy_curr_06", "neet_phy_current_electricity", "Series & Parallel Combination of Resistors", 6, "NOT_STARTED"),
            TopicEntity("neet_phy_curr_07", "neet_phy_current_electricity", "Kirchhoff's First & Second Laws", 7, "NEEDS_REVISION"),
            TopicEntity("neet_phy_curr_08", "neet_phy_current_electricity", "Wheatstone Bridge & Metre Bridge", 8, "NOT_STARTED"),
            TopicEntity("neet_phy_curr_09", "neet_phy_current_electricity", "Potentiometer Principle & Applications", 9, "NOT_STARTED"),
            TopicEntity("neet_phy_curr_10", "neet_phy_current_electricity", "Electrical Energy, Power & Heating Effect", 10, "NOT_STARTED"),
            TopicEntity("neet_phy_curr_11", "neet_phy_current_electricity", "Cells, EMF, Internal Resistance & Combination", 11, "NOT_STARTED"),
            TopicEntity("neet_phy_curr_12", "neet_phy_current_electricity", "Chapter Revision & PYQ Synthesis", 12, "NOT_STARTED")
        )
        learningDao.insertTopics(currentElectricityTopics)

        // Structured Topics for "Physical World & Measurement" (NEET Physics 01)
        val physicalWorldTopics = listOf(
            TopicEntity("neet_phy_pw_01", "neet_phy_physical_world", "Units of Measurement & SI System", 1, "COMPLETED"),
            TopicEntity("neet_phy_pw_02", "neet_phy_physical_world", "Dimensions of Physical Quantities", 2, "COMPLETED"),
            TopicEntity("neet_phy_pw_03", "neet_phy_physical_world", "Dimensional Analysis & Applications", 3, "COMPLETED"),
            TopicEntity("neet_phy_pw_04", "neet_phy_physical_world", "Errors in Measurement & Percentage Error", 4, "COMPLETED"),
            TopicEntity("neet_phy_pw_05", "neet_phy_physical_world", "Significant Figures & Rounding Off", 5, "COMPLETED"),
            TopicEntity("neet_phy_pw_06", "neet_phy_physical_world", "Vernier Callipers & Least Count", 6, "COMPLETED"),
            TopicEntity("neet_phy_pw_07", "neet_phy_physical_world", "Screw Gauge Principle & Zero Error", 7, "COMPLETED"),
            TopicEntity("neet_phy_pw_08", "neet_phy_physical_world", "Experimental Error Propagation", 8, "COMPLETED"),
            TopicEntity("neet_phy_pw_09", "neet_phy_physical_world", "Order of Magnitude & Estimation", 9, "NOT_STARTED"),
            TopicEntity("neet_phy_pw_10", "neet_phy_physical_world", "Limitations of Dimensional Analysis", 10, "NOT_STARTED"),
            TopicEntity("neet_phy_pw_11", "neet_phy_physical_world", "Practice Questions & High Yield Formulas", 11, "NOT_STARTED"),
            TopicEntity("neet_phy_pw_12", "neet_phy_physical_world", "Previous Year NEET Analysis", 12, "NOT_STARTED")
        )
        learningDao.insertTopics(physicalWorldTopics)

        // Structured Topics for "Kinematics" (NEET Physics 02)
        val kinematicsTopics = listOf(
            TopicEntity("neet_phy_kin_01", "neet_phy_kinematics", "Frame of Reference & Motion in a Straight Line", 1, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_02", "neet_phy_kinematics", "Speed, Velocity & Instantaneous Values", 2, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_03", "neet_phy_kinematics", "Uniformly Accelerated Motion", 3, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_04", "neet_phy_kinematics", "Equations of Motion & Calculus Methods", 4, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_05", "neet_phy_kinematics", "Motion Under Gravity & Free Fall", 5, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_06", "neet_phy_kinematics", "Relative Velocity in 1D", 6, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_07", "neet_phy_kinematics", "Vectors: Addition & Subtraction", 7, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_08", "neet_phy_kinematics", "Unit Vectors & Resolution of Vectors", 8, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_09", "neet_phy_kinematics", "Scalar (Dot) and Vector (Cross) Product", 9, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_10", "neet_phy_kinematics", "Projectile Motion: Horizontal & Angular", 10, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_11", "neet_phy_kinematics", "Trajectory Equation & Max Height", 11, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_12", "neet_phy_kinematics", "Uniform Circular Motion & Centripetal Acceleration", 12, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_13", "neet_phy_kinematics", "Relative Velocity in 2D", 13, "NOT_STARTED"),
            TopicEntity("neet_phy_kin_14", "neet_phy_kinematics", "Kinematics Graphical Analysis & PYQs", 14, "NOT_STARTED")
        )
        learningDao.insertTopics(kinematicsTopics)

        // Structured Topics for "Chemical Thermodynamics" (NEET Chemistry 04)
        val thermoTopics = listOf(
            TopicEntity("neet_chem_th_01", "neet_chem_thermodynamics", "System, Surroundings & Boundaries", 1, "COMPLETED"),
            TopicEntity("neet_chem_th_02", "neet_chem_thermodynamics", "State Functions & Path Functions", 2, "COMPLETED"),
            TopicEntity("neet_chem_th_03", "neet_chem_thermodynamics", "First Law of Thermodynamics & Work", 3, "COMPLETED"),
            TopicEntity("neet_chem_th_04", "neet_chem_thermodynamics", "Enthalpy & Heat Capacity (Cp and Cv)", 4, "COMPLETED"),
            TopicEntity("neet_chem_th_05", "neet_chem_thermodynamics", "Hess's Law of Constant Heat Summation", 5, "IN_PROGRESS"),
            TopicEntity("neet_chem_th_06", "neet_chem_thermodynamics", "Enthalpies of Formation & Combustion", 6, "NOT_STARTED"),
            TopicEntity("neet_chem_th_07", "neet_chem_thermodynamics", "Second Law of Thermodynamics & Entropy", 7, "NOT_STARTED"),
            TopicEntity("neet_chem_th_08", "neet_chem_thermodynamics", "Gibbs Free Energy & Spontaneity Criterion", 8, "NOT_STARTED"),
            TopicEntity("neet_chem_th_09", "neet_chem_thermodynamics", "Third Law of Thermodynamics", 9, "NOT_STARTED"),
            TopicEntity("neet_chem_th_10", "neet_chem_thermodynamics", "Thermodynamic Numericals & NEET PYQs", 10, "NOT_STARTED")
        )
        learningDao.insertTopics(thermoTopics)

        // Structured Topics for "Human Physiology" (NEET Biology 05)
        val humanPhysioTopics = listOf(
            TopicEntity("neet_bio_hp_01", "neet_bio_human_physio", "Digestion & Absorption in Alimentary Canal", 1, "COMPLETED"),
            TopicEntity("neet_bio_hp_02", "neet_bio_human_physio", "Digestive Enzymes & Gastrointestinal Hormones", 2, "COMPLETED"),
            TopicEntity("neet_bio_hp_03", "neet_bio_human_physio", "Respiratory Volumes (TV, IRV, ERV, RV)", 3, "COMPLETED"),
            TopicEntity("neet_bio_hp_04", "neet_bio_human_physio", "Transport of Gases: Oxygen Dissociation Curve", 4, "COMPLETED"),
            TopicEntity("neet_bio_hp_05", "neet_bio_human_physio", "Blood Groups & Coagulation Cascade", 5, "COMPLETED"),
            TopicEntity("neet_bio_hp_06", "neet_bio_human_physio", "Human Heart Anatomy & Cardiac Cycle", 6, "COMPLETED"),
            TopicEntity("neet_bio_hp_07", "neet_bio_human_physio", "Electrocardiogram (ECG) & Double Circulation", 7, "COMPLETED"),
            TopicEntity("neet_bio_hp_08", "neet_bio_human_physio", "Nephron Structure & Urine Formation", 8, "COMPLETED"),
            TopicEntity("neet_bio_hp_09", "neet_bio_human_physio", "Counter-Current Mechanism in Henle's Loop", 9, "COMPLETED"),
            TopicEntity("neet_bio_hp_10", "neet_bio_human_physio", "Sliding Filament Theory of Muscle Contraction", 10, "COMPLETED"),
            TopicEntity("neet_bio_hp_11", "neet_bio_human_physio", "Human Skeletal System & Synovial Joints", 11, "COMPLETED"),
            TopicEntity("neet_bio_hp_12", "neet_bio_human_physio", "Generation & Conduction of Nerve Impulse", 12, "IN_PROGRESS"),
            TopicEntity("neet_bio_hp_13", "neet_bio_human_physio", "Central Nervous System & Reflex Arc", 13, "NOT_STARTED"),
            TopicEntity("neet_bio_hp_14", "neet_bio_human_physio", "Endocrine Glands: Pituitary, Thyroid & Adrenal", 14, "NOT_STARTED"),
            TopicEntity("neet_bio_hp_15", "neet_bio_human_physio", "Mechanism of Hormone Action & PYQ Review", 15, "NOT_STARTED")
        )
        learningDao.insertTopics(humanPhysioTopics)

        // Seed default topics for any remaining chapter so each chapter has full interactive path
        val chaptersWithDetailedTopics = setOf(
            "neet_phy_current_electricity",
            "neet_phy_physical_world",
            "neet_phy_kinematics",
            "neet_chem_thermodynamics",
            "neet_bio_human_physio"
        )
        for (chap in chapters) {
            if (!chaptersWithDetailedTopics.contains(chap.id)) {
                val genericTopics = (1..chap.totalTopics).map { idx ->
                    val status = if (idx <= chap.completedTopics) "COMPLETED" else "NOT_STARTED"
                    val subTitle = when (idx) {
                        1 -> "Fundamental Concepts & Definitions"
                        2 -> "Core Principles & Governing Laws"
                        3 -> "Key Derivations & Analytical Formulas"
                        4 -> "Standard Applications & Diagrams"
                        5 -> "Important Exceptions & Nuances"
                        6 -> "Problem Solving Techniques"
                        7 -> "High Yield Numerical Patterns"
                        8 -> "Advanced Concept Extensions"
                        9 -> "Rapid Formula Summary & Memory Tricks"
                        else -> "Topic Synthesis & Previous Year Exam Questions"
                    }
                    TopicEntity(
                        id = "${chap.id}_top_${idx}",
                        chapterId = chap.id,
                        name = "$subTitle ($idx)",
                        orderIndex = idx,
                        status = status
                    )
                }
                learningDao.insertTopics(genericTopics)
            }
        }

        // 3. Curated Learning Resources (Safe YouTube searches considering Exam, Subject, Chapter, Topic & Purpose)
        val resources = listOf(
            LearningResourceEntity(
                id = "res_curr_01",
                chapterId = "neet_phy_current_electricity",
                topicId = null,
                examId = "NEET",
                subjectId = "PHYSICS",
                title = "Current Electricity Full Chapter One Shot",
                channel = "Physics Wallah",
                durationText = "3h 24m",
                resourceType = "One Shot",
                recommendedReason = "Complete NCERT line-by-line & NEET concept foundation",
                searchQuery = "NEET Physics Current Electricity one shot Physics Wallah"
            ),
            LearningResourceEntity(
                id = "res_curr_02",
                chapterId = "neet_phy_current_electricity",
                topicId = "neet_phy_curr_04",
                examId = "NEET",
                subjectId = "PHYSICS",
                title = "Ohm's Law Concept & Numericals",
                channel = "Unacademy NEET",
                durationText = "48:20",
                resourceType = "Concept",
                recommendedReason = "Derivations, temperature dependence & resistance calculations",
                searchQuery = "NEET Physics Ohms Law concept numericals"
            ),
            LearningResourceEntity(
                id = "res_curr_03",
                chapterId = "neet_phy_current_electricity",
                topicId = "neet_phy_curr_06",
                examId = "NEET",
                subjectId = "PHYSICS",
                title = "Series and Parallel Circuits",
                channel = "Physics Galaxy",
                durationText = "32:15",
                resourceType = "Concept",
                recommendedReason = "Equivalent resistance tricks & symmetrical lattice simplification",
                searchQuery = "Physics Galaxy current electricity series parallel circuits"
            ),
            LearningResourceEntity(
                id = "res_curr_04",
                chapterId = "neet_phy_current_electricity",
                topicId = "neet_phy_curr_07",
                examId = "NEET",
                subjectId = "PHYSICS",
                title = "Kirchhoff's Laws in One Shot",
                channel = "PW - Physics Wallah",
                durationText = "1h 12m",
                resourceType = "One Shot",
                recommendedReason = "Junction and loop analysis on complex multi-cell circuits",
                searchQuery = "NEET Physics Kirchhoff laws one shot"
            ),
            LearningResourceEntity(
                id = "res_curr_05",
                chapterId = "neet_phy_current_electricity",
                topicId = "neet_phy_curr_08",
                examId = "NEET",
                subjectId = "PHYSICS",
                title = "Wheatstone Bridge & Tricks",
                channel = "Vedantu NEET",
                durationText = "16:20",
                resourceType = "Numerical Practice",
                recommendedReason = "Bridge balance condition, meter bridge & lab accuracy tips",
                searchQuery = "NEET Physics Wheatstone bridge tricks"
            ),
            LearningResourceEntity(
                id = "res_curr_06",
                chapterId = "neet_phy_current_electricity",
                topicId = "neet_phy_curr_09",
                examId = "NEET",
                subjectId = "PHYSICS",
                title = "Potentiometer Simplified & Lab Experiments",
                channel = "Eduniti - Mohit Goenka",
                durationText = "35:10",
                resourceType = "Concept",
                recommendedReason = "Potential gradient, comparison of EMFs & internal resistance",
                searchQuery = "Eduniti potentiometer NEET Physics"
            ),
            LearningResourceEntity(
                id = "res_curr_07",
                chapterId = "neet_phy_current_electricity",
                topicId = "neet_phy_curr_12",
                examId = "NEET",
                subjectId = "PHYSICS",
                title = "Current Electricity Rapid Formula Revision",
                channel = "Unacademy NEET",
                durationText = "45:00",
                resourceType = "Revision",
                recommendedReason = "Rapid formula recall, short tricks & speed drills before exam",
                searchQuery = "NEET Physics Current Electricity formula revision sheet"
            ),

            // Biology curated resources
            LearningResourceEntity(
                id = "res_bio_01",
                chapterId = "neet_bio_human_physio",
                topicId = null,
                examId = "NEET",
                subjectId = "BIOLOGY",
                title = "Human Physiology Complete Rapid Revision",
                channel = "Dr. Anand Mani",
                durationText = "4h 15m",
                resourceType = "One Shot",
                recommendedReason = "Complete high-yield NCERT diagrams & physiological pathways",
                searchQuery = "NEET Biology Human Physiology one shot NCERT Dr Anand Mani"
            ),
            LearningResourceEntity(
                id = "res_bio_02",
                chapterId = "neet_bio_human_physio",
                topicId = "neet_bio_hp_07",
                examId = "NEET",
                subjectId = "BIOLOGY",
                title = "Cardiac Cycle & ECG Diagram Masterclass",
                channel = "Physics Wallah - Biology",
                durationText = "38:40",
                resourceType = "Concept",
                recommendedReason = "Atrial systole, ventricular systole, P-Q-R-S-T wave interpretations",
                searchQuery = "NEET Biology Cardiac Cycle ECG Physics Wallah"
            ),

            // Chemistry curated resources
            LearningResourceEntity(
                id = "res_chem_01",
                chapterId = "neet_chem_thermodynamics",
                topicId = null,
                examId = "NEET",
                subjectId = "CHEMISTRY",
                title = "Chemical Thermodynamics Complete One Shot",
                channel = "Pankaj Sir Chemistry",
                durationText = "3h 40m",
                resourceType = "One Shot",
                recommendedReason = "First Law, Enthalpies, Entropy & Gibbs energy calculations",
                searchQuery = "NEET Chemistry Thermodynamics one shot Pankaj Sir"
            ),
            LearningResourceEntity(
                id = "res_chem_02",
                chapterId = "neet_chem_organic_basics",
                topicId = null,
                examId = "NEET",
                subjectId = "CHEMISTRY",
                title = "General Organic Chemistry (GOC) Masterclass",
                channel = "Physics Wallah",
                durationText = "4h 05m",
                resourceType = "One Shot",
                recommendedReason = "Inductive, mesomeric, resonance effects & carbocation stability",
                searchQuery = "NEET Chemistry General Organic Chemistry GOC one shot"
            )
        )
        learningDao.insertResources(resources)

        // 4. Official & Original High-Yield Practice Questions
        val questions = listOf(
            QuestionEntity(
                id = "q_curr_001",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                topicName = "Drift Velocity",
                questionText = "When a potential difference V is applied across a conductor of length L and diameter D, the drift velocity is v_d. If the diameter of the conductor is halved while keeping V and L constant, what is the new drift velocity?",
                optionA = "v_d (unchanged)",
                optionB = "v_d / 4",
                optionC = "2 v_d",
                optionD = "4 v_d",
                correctOption = "A",
                explanation = "Drift velocity is given by v_d = eEτ/m = eVτ/(mL). Notice that v_d depends only on electric field E = V/L, relaxation time τ, and electron mass m. It is independent of the cross-sectional area or diameter when V is constant.",
                difficulty = "MEDIUM",
                pyqYear = "NEET 2024",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_curr_002",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                topicName = "Kirchhoff's Laws",
                questionText = "Kirchhoff's first law (Junction Rule) and second law (Loop Rule) are respectively based on the conservation of which fundamental physical quantities?",
                optionA = "Energy and Charge",
                optionB = "Charge and Energy",
                optionC = "Momentum and Energy",
                optionD = "Charge and Momentum",
                correctOption = "B",
                explanation = "The Junction rule states total incoming charge equals total outgoing charge per unit time (Conservation of Charge). The Loop rule states sum of all potential drops around a closed loop is zero (Conservation of Energy).",
                difficulty = "EASY",
                pyqYear = "NEET 2023",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_curr_003",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                topicName = "Wheatstone Bridge",
                questionText = "In a balanced Wheatstone bridge, the resistances in four arms are P = 10 Ω, Q = 20 Ω, R = 15 Ω, and S = 30 Ω. If the galvanometer and battery are interchanged, what will happen to the balanced condition?",
                optionA = "The bridge becomes unbalanced",
                optionB = "The bridge remains balanced",
                optionC = "Resistance across battery doubles",
                optionD = "Galvanometer shows maximum deflection",
                correctOption = "B",
                explanation = "By conjugate arm theorem, the positions of the battery and galvanometer can be interchanged without affecting the balance condition of the Wheatstone bridge.",
                difficulty = "EASY",
                pyqYear = "NEET 2022",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_curr_004",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                topicName = "Resistance & Resistivity",
                questionText = "A wire of resistance 16 Ω is stretched uniformly to twice its original length. What is its new resistance?",
                optionA = "32 Ω",
                optionB = "64 Ω",
                optionC = "16 Ω",
                optionD = "8 Ω",
                correctOption = "B",
                explanation = "Since the volume V = A·L remains constant during stretching, when length L becomes 2L, area A becomes A/2. New resistance R' = ρ(2L)/(A/2) = 4 [ρL/A] = 4 × 16 Ω = 64 Ω.",
                difficulty = "EASY",
                pyqYear = "NEET 2024",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_curr_005",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                topicName = "Potentiometer",
                questionText = "A potentiometer wire of length 100 cm has a resistance of 10 Ω. It is connected in series with a resistance of 40 Ω and a battery of EMF 2 V of negligible internal resistance. The potential gradient along the wire is:",
                optionA = "0.4 V/m",
                optionB = "0.04 V/m",
                optionC = "0.2 V/m",
                optionD = "0.02 V/m",
                correctOption = "A",
                explanation = "Total circuit resistance R_total = 10 + 40 = 50 Ω. Current I = 2V / 50Ω = 0.04 A. Voltage across wire V_wire = I × R_wire = 0.04 × 10 = 0.4 V. Potential gradient k = V_wire / L = 0.4 V / 1 m = 0.4 V/m.",
                difficulty = "MEDIUM",
                pyqYear = "NEET 2021",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_curr_006",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                topicName = "Electrical Energy & Power",
                questionText = "Two electric bulbs rated 25W-220V and 100W-220V are connected in series to a 220V AC mains. Which bulb will glow brighter?",
                optionA = "The 100 W bulb",
                optionB = "The 25 W bulb",
                optionC = "Both will glow equally bright",
                optionD = "Neither will glow",
                correctOption = "B",
                explanation = "Resistance R = V²/P. The 25W bulb has higher resistance (R_25 = 220²/25 = 1936 Ω vs R_100 = 484 Ω). In series, current I is the same through both bulbs. Power consumed P = I²R. Therefore, the 25W bulb dissipates more power and glows brighter.",
                difficulty = "MEDIUM",
                pyqYear = "NEET 2025",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_curr_007",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                topicName = "Ohm's Law",
                questionText = "The temperature coefficient of resistance for a given alloy is positive and small. As temperature rises from 0°C to 100°C, the resistivity of this material:",
                optionA = "Decreases exponentially",
                optionB = "Increases almost linearly",
                optionC = "Remains absolutely invariant",
                optionD = "Drops to zero",
                correctOption = "B",
                explanation = "For metallic alloys with positive temperature coefficient α, resistivity varies approximately linearly according to ρ_T = ρ_0(1 + αΔT).",
                difficulty = "EASY",
                pyqYear = "NEET 2023",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_curr_008",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                topicName = "Cells & EMF",
                questionText = "A cell of EMF E and internal resistance r is connected across an external variable resistance R. Under what condition is maximum power transferred to the external resistance?",
                optionA = "R = r / 2",
                optionB = "R = 2r",
                optionC = "R = r",
                optionD = "R = 0",
                correctOption = "C",
                explanation = "According to the Maximum Power Transfer Theorem, maximum power is delivered from a source to a load when the load resistance R equals the internal resistance r of the source.",
                difficulty = "MEDIUM",
                pyqYear = "NEET 2022",
                isOfficialPYQ = true
            ),

            // --- MORE PHYSICS QUESTIONS & PYQS ---
            QuestionEntity(
                id = "q_kin_001",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_kinematics",
                topicName = "Projectile Motion",
                questionText = "A projectile is fired at an angle of 45° with the horizontal. Elevation angle of the projectile at its highest point as observed from the point of projection is:",
                optionA = "tan⁻¹(1/2)",
                optionB = "45°",
                optionC = "60°",
                optionD = "tan⁻¹(2)",
                correctOption = "A",
                explanation = "At maximum height H = u²sin²(45°)/(2g) = u²/(4g), the horizontal distance from projection point is R/2 = u²/(2g). Elevation angle φ has tan(φ) = H / (R/2) = (1/2) tan(45°) = 1/2. Thus φ = tan⁻¹(1/2).",
                difficulty = "MEDIUM",
                pyqYear = "NEET 2024",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_kin_002",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_kinematics",
                topicName = "Relative Velocity",
                questionText = "Two cars A and B are moving along straight parallel tracks with velocities 20 m/s and 10 m/s in the same direction. What is the velocity of car A relative to car B?",
                optionA = "30 m/s in direction of A",
                optionB = "10 m/s in direction of A",
                optionC = "10 m/s in opposite direction",
                optionD = "200 m/s",
                correctOption = "B",
                explanation = "Relative velocity v_AB = v_A - v_B = 20 - 10 = +10 m/s in the direction of motion.",
                difficulty = "EASY",
                pyqYear = "NEET 2023",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_lom_001",
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_laws_of_motion",
                topicName = "Friction",
                questionText = "A block of mass 10 kg is placed on a rough horizontal surface with coefficient of static friction μ_s = 0.5. If a horizontal force of 40 N is applied, the force of friction acting on the block is: (take g = 10 m/s²)",
                optionA = "50 N",
                optionB = "40 N",
                optionC = "10 N",
                optionD = "0 N",
                correctOption = "B",
                explanation = "Limiting friction f_max = μ_s × N = 0.5 × (10 × 10) = 50 N. Since the applied force (40 N) is less than f_max, the block does not slip. Static friction adjusts to equal the applied force, so f = 40 N.",
                difficulty = "MEDIUM",
                pyqYear = "NEET 2025",
                isOfficialPYQ = true
            ),

            // --- CHEMISTRY QUESTIONS & PYQS ---
            QuestionEntity(
                id = "q_chem_001",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                chapterId = "neet_chem_thermodynamics",
                topicName = "Gibbs Free Energy",
                questionText = "For a spontaneous reaction at all temperatures, the signs of enthalpy change (ΔH) and entropy change (ΔS) must be respectively:",
                optionA = "ΔH > 0 and ΔS < 0",
                optionB = "ΔH < 0 and ΔS > 0",
                optionC = "ΔH > 0 and ΔS > 0",
                optionD = "ΔH < 0 and ΔS < 0",
                correctOption = "B",
                explanation = "According to Gibbs-Helmholtz equation ΔG = ΔH - TΔS. For spontaneity, ΔG must be negative. When ΔH is negative (exothermic) and ΔS is positive, ΔG is always negative irrespective of temperature.",
                difficulty = "EASY",
                pyqYear = "NEET 2024",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_chem_002",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                chapterId = "neet_chem_thermodynamics",
                topicName = "First Law of Thermodynamics",
                questionText = "In an adiabatic expansion of an ideal gas, the work done by the system is 50 J. What is the change in internal energy (ΔU)?",
                optionA = "+50 J",
                optionB = "-50 J",
                optionC = "0 J",
                optionD = "+100 J",
                correctOption = "B",
                explanation = "For an adiabatic process, heat exchange q = 0. By First Law ΔU = q - w = 0 - 50 J = -50 J.",
                difficulty = "EASY",
                pyqYear = "NEET 2023",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_chem_003",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                chapterId = "neet_chem_basic_concepts",
                topicName = "Mole Concept",
                questionText = "The number of moles of hydrogen molecules required to produce 20 moles of ammonia through Haber's process is:",
                optionA = "10",
                optionB = "20",
                optionC = "30",
                optionD = "40",
                correctOption = "C",
                explanation = "The balanced equation is N₂ + 3H₂ → 2NH₃. To produce 2 moles of NH₃, 3 moles of H₂ are required. For 20 moles of NH₃: (3 / 2) × 20 = 30 moles of H₂.",
                difficulty = "EASY",
                pyqYear = "NEET 2025",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_chem_004",
                examId = "NEET",
                subjectId = "CHEMISTRY",
                chapterId = "neet_chem_structure_of_atom",
                topicName = "Quantum Numbers",
                questionText = "Which set of quantum numbers is NOT permitted for an electron in an atom?",
                optionA = "n = 3, l = 2, m = -2, s = +1/2",
                optionB = "n = 4, l = 0, m = 0, s = -1/2",
                optionC = "n = 3, l = 3, m = 0, s = +1/2",
                optionD = "n = 2, l = 1, m = +1, s = -1/2",
                correctOption = "C",
                explanation = "Azimuthal quantum number l can only take integer values from 0 to (n - 1). For n = 3, maximum allowed value of l is 2. Therefore l = 3 is forbidden.",
                difficulty = "EASY",
                pyqYear = "NEET 2022",
                isOfficialPYQ = true
            ),

            // --- BIOLOGY QUESTIONS & PYQS ---
            QuestionEntity(
                id = "q_bio_001",
                examId = "NEET",
                subjectId = "BIOLOGY",
                chapterId = "neet_bio_human_physio",
                topicName = "Electrocardiogram (ECG)",
                questionText = "In a standard standard electrocardiogram (ECG), which wave represents the depolarisation of the ventricles?",
                optionA = "P-wave",
                optionB = "QRS complex",
                optionC = "T-wave",
                optionD = "U-wave",
                correctOption = "B",
                explanation = "In a standard ECG: P-wave represents atrial depolarisation; QRS complex represents ventricular depolarisation (initiating ventricular contraction); T-wave represents ventricular repolarisation.",
                difficulty = "EASY",
                pyqYear = "NEET 2024",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_bio_002",
                examId = "NEET",
                subjectId = "BIOLOGY",
                chapterId = "neet_bio_human_physio",
                topicName = "Counter-Current Mechanism",
                questionText = "The counter-current mechanism that helps maintain an osmotic gradient in the medullary interstitium of human kidneys operates between:",
                optionA = "Ascending and descending limbs of Henle's loop and Vasa Recta",
                optionB = "Proximal convoluted tubule and distal convoluted tubule",
                optionC = "Glomerulus and Bowman's capsule",
                optionD = "Collecting duct and renal pelvis",
                correctOption = "A",
                explanation = "The proximity between Henle's loop and vasa recta, as well as the counter-current flow of filtrate/blood in their limbs, creates the medullary hyperosmolarity essential for water reabsorption.",
                difficulty = "MEDIUM",
                pyqYear = "NEET 2023",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_bio_003",
                examId = "NEET",
                subjectId = "BIOLOGY",
                chapterId = "neet_bio_human_physio",
                topicName = "Respiratory Volumes",
                questionText = "The volume of air remaining in the lungs even after a forcible expiration is known as:",
                optionA = "Tidal Volume (TV)",
                optionB = "Inspiratory Reserve Volume (IRV)",
                optionC = "Residual Volume (RV)",
                optionD = "Expiratory Reserve Volume (ERV)",
                correctOption = "C",
                explanation = "Residual Volume (RV) is approximately 1100 mL to 1200 mL and cannot be cleared even by maximal exhalation, preventing lung collapse.",
                difficulty = "EASY",
                pyqYear = "NEET 2025",
                isOfficialPYQ = true
            ),
            QuestionEntity(
                id = "q_bio_004",
                examId = "NEET",
                subjectId = "BIOLOGY",
                chapterId = "neet_bio_cell_unit_of_life",
                topicName = "Endomembrane System",
                questionText = "Which of the following organelles is NOT part of the endomembrane system of eukaryotic cells?",
                optionA = "Endoplasmic Reticulum",
                optionB = "Golgi Complex",
                optionC = "Lysosomes",
                optionD = "Mitochondria",
                correctOption = "D",
                explanation = "The endomembrane system includes ER, Golgi apparatus, lysosomes, and vacuoles because their functions are coordinated. Mitochondria, chloroplasts, and peroxisomes are semi-autonomous or not coordinated with these, hence excluded.",
                difficulty = "EASY",
                pyqYear = "NEET 2022",
                isOfficialPYQ = true
            )
        )
        learningDao.insertQuestions(questions)

        // 5. Seed Real Initial Question Attempts & Mock Test for Authentic Performance Engine
        val initialAttempts = listOf(
            // Physics - Current Electricity (Kirchhoff's Laws & Wheatstone Bridge targeted attempts)
            QuestionAttemptEntity(questionId = "q_curr_002", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Kirchhoff's Laws", selectedOption = "A", isCorrect = false, timeTakenSeconds = 64, timestamp = System.currentTimeMillis() - 172800000),
            QuestionAttemptEntity(questionId = "q_curr_002", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Kirchhoff's Laws", selectedOption = "C", isCorrect = false, timeTakenSeconds = 55, timestamp = System.currentTimeMillis() - 170000000),
            QuestionAttemptEntity(questionId = "q_curr_002", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Kirchhoff's Laws", selectedOption = "B", isCorrect = true, timeTakenSeconds = 48, timestamp = System.currentTimeMillis() - 160000000),
            QuestionAttemptEntity(questionId = "q_curr_002", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Kirchhoff's Laws", selectedOption = "D", isCorrect = false, timeTakenSeconds = 70, timestamp = System.currentTimeMillis() - 150000000),
            QuestionAttemptEntity(questionId = "q_curr_002", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Kirchhoff's Laws", selectedOption = "B", isCorrect = true, timeTakenSeconds = 40, timestamp = System.currentTimeMillis() - 140000000),
            QuestionAttemptEntity(questionId = "q_curr_003", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Wheatstone Bridge", selectedOption = "A", isCorrect = false, timeTakenSeconds = 52, timestamp = System.currentTimeMillis() - 130000000),
            QuestionAttemptEntity(questionId = "q_curr_003", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Wheatstone Bridge", selectedOption = "B", isCorrect = true, timeTakenSeconds = 45, timestamp = System.currentTimeMillis() - 120000000),
            QuestionAttemptEntity(questionId = "q_curr_003", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Wheatstone Bridge", selectedOption = "D", isCorrect = false, timeTakenSeconds = 60, timestamp = System.currentTimeMillis() - 110000000),
            QuestionAttemptEntity(questionId = "q_curr_001", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Drift Velocity", selectedOption = "A", isCorrect = true, timeTakenSeconds = 35, timestamp = System.currentTimeMillis() - 100000000),
            QuestionAttemptEntity(questionId = "q_curr_004", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Resistance & Resistivity", selectedOption = "B", isCorrect = true, timeTakenSeconds = 30, timestamp = System.currentTimeMillis() - 90000000),
            QuestionAttemptEntity(questionId = "q_curr_006", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Electrical Energy & Power", selectedOption = "B", isCorrect = true, timeTakenSeconds = 42, timestamp = System.currentTimeMillis() - 80000000),
            QuestionAttemptEntity(questionId = "q_curr_007", chapterId = "neet_phy_current_electricity", subjectId = "PHYSICS", topicName = "Ohm's Law", selectedOption = "B", isCorrect = true, timeTakenSeconds = 25, timestamp = System.currentTimeMillis() - 70000000),

            // Chemistry attempts
            QuestionAttemptEntity(questionId = "q_chem_001", chapterId = "neet_chem_thermodynamics", subjectId = "CHEMISTRY", topicName = "Gibbs Free Energy", selectedOption = "B", isCorrect = true, timeTakenSeconds = 28, timestamp = System.currentTimeMillis() - 60000000),
            QuestionAttemptEntity(questionId = "q_chem_002", chapterId = "neet_chem_thermodynamics", subjectId = "CHEMISTRY", topicName = "First Law of Thermodynamics", selectedOption = "B", isCorrect = true, timeTakenSeconds = 32, timestamp = System.currentTimeMillis() - 50000000),
            QuestionAttemptEntity(questionId = "q_chem_003", chapterId = "neet_chem_basic_concepts", subjectId = "CHEMISTRY", topicName = "Mole Concept", selectedOption = "C", isCorrect = true, timeTakenSeconds = 40, timestamp = System.currentTimeMillis() - 40000000),
            QuestionAttemptEntity(questionId = "q_chem_004", chapterId = "neet_chem_structure_of_atom", subjectId = "CHEMISTRY", topicName = "Quantum Numbers", selectedOption = "C", isCorrect = true, timeTakenSeconds = 35, timestamp = System.currentTimeMillis() - 30000000),

            // Biology attempts
            QuestionAttemptEntity(questionId = "q_bio_001", chapterId = "neet_bio_human_physio", subjectId = "BIOLOGY", topicName = "Electrocardiogram (ECG)", selectedOption = "B", isCorrect = true, timeTakenSeconds = 22, timestamp = System.currentTimeMillis() - 25000000),
            QuestionAttemptEntity(questionId = "q_bio_002", chapterId = "neet_bio_human_physio", subjectId = "BIOLOGY", topicName = "Counter-Current Mechanism", selectedOption = "A", isCorrect = true, timeTakenSeconds = 30, timestamp = System.currentTimeMillis() - 20000000),
            QuestionAttemptEntity(questionId = "q_bio_003", chapterId = "neet_bio_human_physio", subjectId = "BIOLOGY", topicName = "Respiratory Volumes", selectedOption = "C", isCorrect = true, timeTakenSeconds = 18, timestamp = System.currentTimeMillis() - 15000000),
            QuestionAttemptEntity(questionId = "q_bio_004", chapterId = "neet_bio_cell_unit_of_life", subjectId = "BIOLOGY", topicName = "Endomembrane System", selectedOption = "D", isCorrect = true, timeTakenSeconds = 24, timestamp = System.currentTimeMillis() - 10000000)
        )
        for (att in initialAttempts) {
            learningDao.recordQuestionAttempt(att)
        }

        // Initial Seed Quiz Attempt
        learningDao.recordQuizAttempt(
            QuizAttemptEntity(
                examId = "NEET",
                subjectId = "PHYSICS",
                chapterId = "neet_phy_current_electricity",
                chapterName = "Current Electricity",
                totalQuestions = 10,
                correctAnswers = 7,
                timeTakenSeconds = 540,
                mode = "PRACTICE",
                strongTopicsJson = "[\"Ohm's Law\",\"Drift Velocity\"]",
                weakTopicsJson = "[\"Kirchhoff's Laws\",\"Wheatstone Bridge\"]",
                timestamp = System.currentTimeMillis() - 86400000
            )
        )
    }
}
