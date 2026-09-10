package com.example.data.model

data class NeetTopic(
    val id: String,
    val name: String,
    val isCompleted: Boolean = false
)

data class NeetChapter(
    val id: String, // e.g. "neet_phy_current_electricity"
    val name: String,
    val subjectName: String, // "Physics", "Chemistry", "Biology"
    val totalQuestionsCount: Int = 250,
    val topics: List<String>,
    val description: String = ""
)

data class NeetQuestion(
    val id: String,
    val subjectName: String,
    val chapterName: String,
    val topicName: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val difficulty: String = "MEDIUM" // "EASY", "MEDIUM", "HARD"
)

data class OfficialPyqPaper(
    val id: String,
    val year: Int,
    val examName: String = "NEET UG Official Paper",
    val totalQuestions: Int = 180,
    val durationMinutes: Int = 200,
    val isOfficial: Boolean = true
)

// Real NEET Curriculum Data
val neetPhysicsChapters = listOf(
    NeetChapter(
        id = "neet_phy_kinematics",
        name = "Kinematics & Motion in 1D/2D",
        subjectName = "Physics",
        totalQuestionsCount = 320,
        topics = listOf("Vectors", "Motion in Straight Line", "Projectile Motion", "Relative Velocity", "Circular Motion", "Revision & Practice")
    ),
    NeetChapter(
        id = "neet_phy_laws_of_motion",
        name = "Laws of Motion & Friction",
        subjectName = "Physics",
        totalQuestionsCount = 280,
        topics = listOf("Newton's First Law", "Force & Momentum", "Friction", "Pulleys & Equilibrium", "Circular Dynamics")
    ),
    NeetChapter(
        id = "neet_phy_work_energy_power",
        name = "Work, Energy & Power",
        subjectName = "Physics",
        totalQuestionsCount = 250,
        topics = listOf("Work Done by Constant & Variable Force", "Work Energy Theorem", "Potential Energy", "Collisions & Conservation")
    ),
    NeetChapter(
        id = "neet_phy_electrostatics",
        name = "Electrostatics & Capacitance",
        subjectName = "Physics",
        totalQuestionsCount = 350,
        topics = listOf("Coulomb's Law", "Electric Field & Dipole", "Gauss's Law", "Electric Potential", "Capacitors in Series & Parallel")
    ),
    NeetChapter(
        id = "neet_phy_current_electricity",
        name = "Current Electricity",
        subjectName = "Physics",
        totalQuestionsCount = 420,
        topics = listOf("Basic Concepts", "Electric Current & Drift Velocity", "Ohm's Law & Resistance", "Combination of Resistors", "Kirchhoff's Laws", "Wheatstone Bridge", "Potentiometer", "Meter Bridge")
    ),
    NeetChapter(
        id = "neet_phy_magnetism",
        name = "Magnetism & Magnetic Effects of Current",
        subjectName = "Physics",
        totalQuestionsCount = 310,
        topics = listOf("Biot-Savart Law", "Ampere's Circuital Law", "Magnetic Force on Moving Charge", "Cyclotron", "Earth's Magnetism")
    ),
    NeetChapter(
        id = "neet_phy_optics",
        name = "Ray & Wave Optics",
        subjectName = "Physics",
        totalQuestionsCount = 390,
        topics = listOf("Reflection & Refraction", "Lenses & Mirrors", "Optical Instruments", "Young's Double Slit Experiment", "Diffraction & Polarization")
    ),
    NeetChapter(
        id = "neet_phy_modern_physics",
        name = "Modern Physics & Semiconductors",
        subjectName = "Physics",
        totalQuestionsCount = 400,
        topics = listOf("Photoelectric Effect", "Dual Nature of Matter", "Bohr's Atomic Model", "Radioactivity & Nuclei", "P-N Junction Diodes", "Logic Gates")
    )
)

val neetChemistryChapters = listOf(
    NeetChapter(
        id = "neet_chem_some_basic_concepts",
        name = "Some Basic Concepts of Chemistry & Mole Concept",
        subjectName = "Chemistry",
        totalQuestionsCount = 300,
        topics = listOf("Mole Concept", "Stoichiometry", "Empirical & Molecular Formula", "Concentration Terms")
    ),
    NeetChapter(
        id = "neet_chem_thermodynamics",
        name = "Chemical Thermodynamics & Energetics",
        subjectName = "Chemistry",
        totalQuestionsCount = 340,
        topics = listOf("First Law of Thermodynamics", "Enthalpy & Hess Law", "Second Law & Entropy", "Gibbs Free Energy & Spontaneity")
    ),
    NeetChapter(
        id = "neet_chem_equilibrium",
        name = "Chemical & Ionic Equilibrium",
        subjectName = "Chemistry",
        totalQuestionsCount = 380,
        topics = listOf("Law of Mass Action", "Le Chatelier's Principle", "pH Calculation & Buffers", "Solubility Product")
    ),
    NeetChapter(
        id = "neet_chem_organic_basics",
        name = "Organic Chemistry - Basic Principles & Techniques (GOC)",
        subjectName = "Chemistry",
        totalQuestionsCount = 450,
        topics = listOf("IUPAC Nomenclature", "Inductive & Resonance Effects", "Hyperconjugation", "Isomerism", "Carbocations & Free Radicals")
    ),
    NeetChapter(
        id = "neet_chem_hydrocarbons",
        name = "Hydrocarbons (Alkanes, Alkenes, Alkynes, Arenes)",
        subjectName = "Chemistry",
        totalQuestionsCount = 360,
        topics = listOf("Preparation of Alkanes", "Electrophilic Addition to Alkenes", "Ozonolysis", "Aromaticity & Benzene Reactions")
    ),
    NeetChapter(
        id = "neet_chem_coordination",
        name = "Coordination Compounds",
        subjectName = "Chemistry",
        totalQuestionsCount = 320,
        topics = listOf("Werner's Theory", "IUPAC Naming of Complexes", "Valence Bond Theory", "Crystal Field Theory (CFT)")
    )
)

val neetBiologyChapters = listOf(
    NeetChapter(
        id = "neet_bio_cell_cycle",
        name = "Cell: The Unit of Life & Cell Cycle",
        subjectName = "Biology",
        totalQuestionsCount = 500,
        topics = listOf("Prokaryotic vs Eukaryotic Cell", "Cell Organelles", "Mitosis", "Meiosis & Crossing Over")
    ),
    NeetChapter(
        id = "neet_bio_genetics",
        name = "Principles of Inheritance & Molecular Basis",
        subjectName = "Biology",
        totalQuestionsCount = 650,
        topics = listOf("Mendel's Laws", "Sex Determination", "DNA Structure & Replication", "Transcription & Translation", "Human Genome Project")
    ),
    NeetChapter(
        id = "neet_bio_human_physio",
        name = "Human Physiology (Digestion, Circulation, Neural)",
        subjectName = "Biology",
        totalQuestionsCount = 700,
        topics = listOf("Breathing & Gas Exchange", "Body Fluids & Circulation", "Excretory Products & Elimination", "Neural Control & Coordination")
    ),
    NeetChapter(
        id = "neet_bio_plant_physio",
        name = "Plant Physiology & Photosynthesis",
        subjectName = "Biology",
        totalQuestionsCount = 480,
        topics = listOf("Transport in Plants", "Mineral Nutrition", "Photosynthesis in Higher Plants (C3/C4)", "Respiration in Plants")
    ),
    NeetChapter(
        id = "neet_bio_reproduction",
        name = "Human Reproduction & Reproductive Health",
        subjectName = "Biology",
        totalQuestionsCount = 520,
        topics = listOf("Male & Female Reproductive Systems", "Gametogenesis & Menstrual Cycle", "Fertilization & Implant", "Contraception & ART")
    )
)

// Sample Original Practice Questions
val sampleNeetQuestions = listOf(
    NeetQuestion(
        id = "q_phy_1",
        subjectName = "Physics",
        chapterName = "Current Electricity",
        topicName = "Ohm's Law & Resistance",
        questionText = "A wire of resistance 12 Ω is bent into the form of a circle. The effective resistance between two points at the ends of any diameter is:",
        options = listOf("3 Ω", "6 Ω", "12 Ω", "24 Ω"),
        correctOptionIndex = 0,
        explanation = "When bent into a circle, the wire splits into two semicircular arcs in parallel. Each arc has resistance 6 Ω. Parallel equivalent: (6 * 6) / (6 + 6) = 3 Ω.",
        difficulty = "EASY"
    ),
    NeetQuestion(
        id = "q_phy_2",
        subjectName = "Physics",
        chapterName = "Current Electricity",
        topicName = "Kirchhoff's Laws",
        questionText = "Kirchhoff's First Law (ΣI = 0) and Second Law (ΣV = 0) at a junction are respectively based on the conservation of:",
        options = listOf("Charge, Energy", "Energy, Charge", "Charge, Momentum", "Momentum, Energy"),
        correctOptionIndex = 0,
        explanation = "Kirchhoff's Junction Rule is a statement of conservation of electric charge. Kirchhoff's Loop Rule is a statement of conservation of energy.",
        difficulty = "EASY"
    ),
    NeetQuestion(
        id = "q_phy_3",
        subjectName = "Physics",
        chapterName = "Current Electricity",
        topicName = "Drift Velocity",
        questionText = "When a potential difference V is applied across a conductor of length L, the drift velocity of electrons is v_d. If the length is doubled to 2L with same V, drift velocity becomes:",
        options = listOf("v_d / 2", "v_d", "2 v_d", "v_d / 4"),
        correctOptionIndex = 0,
        explanation = "Drift velocity v_d = eEτ/m = e(V/L)τ/m. If length L is doubled while keeping V constant, E = V/(2L), so drift velocity is halved (v_d / 2).",
        difficulty = "MEDIUM"
    ),
    NeetQuestion(
        id = "q_chem_1",
        subjectName = "Chemistry",
        chapterName = "Chemical Thermodynamics & Energetics",
        topicName = "Gibbs Free Energy & Spontaneity",
        questionText = "For a reaction to be spontaneous at all temperatures, the signs of ΔH and ΔS must be respectively:",
        options = listOf("Negative, Positive", "Positive, Negative", "Negative, Negative", "Positive, Positive"),
        correctOptionIndex = 0,
        explanation = "ΔG = ΔH - TΔS. If ΔH < 0 and ΔS > 0, then ΔG is negative at all temperatures, making the reaction always spontaneous.",
        difficulty = "EASY"
    ),
    NeetQuestion(
        id = "q_bio_1",
        subjectName = "Biology",
        chapterName = "Principles of Inheritance & Molecular Basis",
        topicName = "DNA Structure & Replication",
        questionText = "Which enzyme is responsible for synthesizing the RNA primer during DNA replication in E. coli?",
        options = listOf("Primase", "DNA Polymerase I", "DNA Ligase", "Helicase"),
        correctOptionIndex = 0,
        explanation = "Primase (an RNA polymerase) synthesizes a short RNA primer to provide a 3'-OH group for DNA polymerase to initiate synthesis.",
        difficulty = "EASY"
    )
)

val officialPyqPapers = listOf(
    OfficialPyqPaper(id = "neet_2024", year = 2024, examName = "NEET UG 2024 Official Paper"),
    OfficialPyqPaper(id = "neet_2023", year = 2023, examName = "NEET UG 2023 Official Paper"),
    OfficialPyqPaper(id = "neet_2022", year = 2022, examName = "NEET UG 2022 Official Paper"),
    OfficialPyqPaper(id = "neet_2021", year = 2021, examName = "NEET UG 2021 Official Paper"),
    OfficialPyqPaper(id = "neet_2020", year = 2020, examName = "NEET UG 2020 Official Paper")
)
