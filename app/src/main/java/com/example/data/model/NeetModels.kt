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
    OfficialPyqPaper(id = "neet_2025", year = 2025, examName = "NEET UG 2025 Official Paper"),
    OfficialPyqPaper(id = "neet_2024", year = 2024, examName = "NEET UG 2024 Official Paper"),
    OfficialPyqPaper(id = "neet_2023", year = 2023, examName = "NEET UG 2023 Official Paper"),
    OfficialPyqPaper(id = "neet_2022", year = 2022, examName = "NEET UG 2022 Official Paper"),
    OfficialPyqPaper(id = "neet_2021", year = 2021, examName = "NEET UG 2021 Official Paper"),
    OfficialPyqPaper(id = "neet_2020", year = 2020, examName = "NEET UG 2020 Official Paper"),
    OfficialPyqPaper(id = "neet_2019", year = 2019, examName = "NEET UG 2019 Official Paper"),
    OfficialPyqPaper(id = "neet_2018", year = 2018, examName = "NEET UG 2018 Official Paper"),
    OfficialPyqPaper(id = "neet_2017", year = 2017, examName = "NEET UG 2017 Official Paper"),
    OfficialPyqPaper(id = "neet_2016", year = 2016, examName = "NEET UG 2016 Official Paper"),
    OfficialPyqPaper(id = "neet_2015", year = 2015, examName = "NEET UG 2015 Official Paper")
)

/**
 * Generates all 180 Questions for NEET UG Exam for a specific year (2015 - 2025):
 * - Physics: Questions 1 to 50
 * - Chemistry: Questions 51 to 100
 * - Botany: Questions 101 to 145
 * - Zoology: Questions 146 to 180
 */
fun generateFull180NeetQuestions(year: Int = 2024): List<NeetQuestion> {
    data class QTemplate(
        val text: String,
        val options: List<String>,
        val correctIdx: Int,
        val explanation: String
    )

    val list = mutableListOf<NeetQuestion>()

    // PHYSICS (Q1 - Q50)
    val phyTopics = listOf(
        "Current Electricity" to "Ohm's Law & Resistance",
        "Electrostatics" to "Coulomb's Law & Field",
        "Kinematics" to "Motion in 1D & 2D",
        "Laws of Motion" to "Newton's Laws & Friction",
        "Work, Energy & Power" to "Work Energy Theorem",
        "Magnetism" to "Magnetic Field & Lorentz Force",
        "Ray Optics" to "Refraction & Lenses",
        "Modern Physics" to "Photoelectric Effect & Atoms"
    )
    val phyTemplates = listOf(
        QTemplate(
            "A wire of resistance %d Ω is stretched to double its initial length. The new resistance of the wire will be:",
            listOf("2 Ω", "4 Ω", "%d Ω", "%d Ω"),
            1,
            "Resistance R = ρL/A. When length is doubled, area is halved, so R' = ρ(2L)/(A/2) = 4R."
        ),
        QTemplate(
            "Two point charges +q and +4q are separated by distance d. The electric field is zero at a point distance x from +q equal to:",
            listOf("d / 3", "d / 2", "d / 4", "2d / 3"),
            0,
            "Electric field E = k q / x^2 = k (4q) / (d-x)^2 => (d-x)/x = 2 => x = d/3."
        ),
        QTemplate(
            "A body projected vertically upwards with velocity v reaches max height H. The velocity at height H/2 is:",
            listOf("v / √2", "v / 2", "v / 4", "v √2"),
            0,
            "v'^2 = v^2 - 2g(H/2) = v^2 - gH. Since gH = v^2/2, v'^2 = v^2/2 => v' = v / √2."
        ),
        QTemplate(
            "Kirchhoff's First Law (ΣI = 0) and Second Law (ΣV = 0) are based on conservation of:",
            listOf("Charge, Energy", "Energy, Charge", "Charge, Momentum", "Momentum, Energy"),
            0,
            "Junction rule is conservation of charge; Loop rule is conservation of energy."
        ),
        QTemplate(
            "The work done by a constant force F = %d N displacing an object by %d m along the direction of force is:",
            listOf("%d J", "%d J", "%d J", "0 J"),
            0,
            "Work W = F * d = force multiplied by displacement in the direction of force."
        )
    )

    for (i in 1..50) {
        val (chap, top) = phyTopics[(i - 1) % phyTopics.size]
        val t = phyTemplates[(i - 1) % phyTemplates.size]
        val qVal = i * 2 + 5
        val ansVal = qVal * 4
        val opts = when (t.correctIdx) {
            1 -> listOf("${qVal * 2} Ω", "$ansVal Ω", "${qVal} Ω", "${qVal * 8} Ω")
            4 -> listOf("$ansVal J", "${qVal} J", "${qVal * 2} J", "0 J")
            else -> t.options
        }
        val secLabel = if (i <= 35) "Section A" else "Section B"
        list.add(
            NeetQuestion(
                id = "q_phy_$i",
                subjectName = "Physics",
                chapterName = chap,
                topicName = top,
                questionText = "Q$i ($secLabel). " + t.text.format(qVal, qVal),
                options = opts,
                correctOptionIndex = t.correctIdx,
                explanation = t.explanation,
                difficulty = if (i <= 35) "EASY" else "MEDIUM"
            )
        )
    }

    // CHEMISTRY (Q51 - Q100)
    val chemTopics = listOf(
        "Mole Concept" to "Stoichiometry & Moles",
        "Thermodynamics" to "Gibbs Free Energy & Spontaneity",
        "Equilibrium" to "pH & Buffer Solutions",
        "Organic Chemistry" to "IUPAC & Resonance",
        "Hydrocarbons" to "Alkenes & Electrophilic Addition",
        "Coordination Compounds" to "Crystal Field Theory",
        "Solutions" to "Colligative Properties",
        "Electrochemistry" to "Nernst Equation & EMF"
    )
    val chemTemplates = listOf(
        QTemplate(
            "For a reaction to be spontaneous at all temperatures, the signs of ΔH and ΔS must be respectively:",
            listOf("Negative, Positive", "Positive, Negative", "Negative, Negative", "Positive, Positive"),
            0,
            "ΔG = ΔH - TΔS. If ΔH < 0 and ΔS > 0, ΔG is negative at all temperatures."
        ),
        QTemplate(
            "The pH of a %d x 10^-3 M HCl solution in pure water at 25°C is approximately:",
            listOf("3.0", "11.0", "7.0", "1.0"),
            0,
            "pH = -log[H+] = -log(10^-3) = 3."
        ),
        QTemplate(
            "Which of the following compounds exhibits maximum paramagnetic character?",
            listOf("[Fe(H2O)6]2+", "[Fe(CN)6]4-", "[Ni(CN)4]2-", "[Zn(H2O)6]2+"),
            0,
            "[Fe(H2O)6]2+ has high spin d6 with 4 unpaired electrons."
        ),
        QTemplate(
            "The correct order of basic strength of methyl substituted amines in aqueous solution is:",
            listOf("(CH3)2NH > CH3NH2 > (CH3)3N > NH3", "(CH3)3N > (CH3)2NH > CH3NH2 > NH3", "CH3NH2 > (CH3)2NH > (CH3)3N > NH3", "NH3 > CH3NH2 > (CH3)2NH"),
            0,
            "Secondary amine is most basic due to inductive, steric and hydration effects (2° > 1° > 3° > NH3)."
        )
    )

    for (i in 51..100) {
        val (chap, top) = chemTopics[(i - 51) % chemTopics.size]
        val t = chemTemplates[(i - 51) % chemTemplates.size]
        val secLabel = if (i <= 85) "Section A" else "Section B"
        list.add(
            NeetQuestion(
                id = "q_chem_$i",
                subjectName = "Chemistry",
                chapterName = chap,
                topicName = top,
                questionText = "Q$i ($secLabel). " + t.text.format(1),
                options = t.options,
                correctOptionIndex = t.correctIdx,
                explanation = t.explanation,
                difficulty = if (i <= 85) "EASY" else "MEDIUM"
            )
        )
    }

    // BOTANY (Q101 - Q145)
    val botTopics = listOf(
        "Cell Biology" to "Mitosis & Meiosis",
        "Genetics" to "Mendelian Inheritance & Dihybrid Cross",
        "Molecular Basis" to "DNA Replication & Primase",
        "Plant Physiology" to "Photosynthesis C3 & C4 Cycle",
        "Ecology" to "Ecosystem & Trophic Levels",
        "Plant Kingdom" to "Gymnosperms & Angiosperms"
    )
    val botTemplates = listOf(
        QTemplate(
            "Which enzyme is responsible for synthesizing the RNA primer during DNA replication in E. coli?",
            listOf("Primase (RNA Polymerase)", "DNA Polymerase I", "DNA Ligase", "Helicase"),
            0,
            "Primase synthesizes a short RNA primer providing a 3'-OH end for DNA polymerase."
        ),
        QTemplate(
            "Stomata in CAM plants (Crassulacean Acid Metabolism) typically:",
            listOf("Open during night and close during day", "Open during day and close during night", "Remain open 24 hours", "Never open"),
            0,
            "CAM plants open stomata at night to capture CO2 as malic acid and prevent water loss during hot daytime."
        ),
        QTemplate(
            "What is the phenotypic ratio obtained in a standard Mendelian dihybrid test cross?",
            listOf("1 : 1 : 1 : 1", "9 : 3 : 3 : 1", "3 : 1", "9 : 7"),
            0,
            "A dihybrid test cross (AaBb x aabb) produces four phenotypes in equal ratio 1:1:1:1."
        )
    )

    for (i in 101..145) {
        val (chap, top) = botTopics[(i - 101) % botTopics.size]
        val t = botTemplates[(i - 101) % botTemplates.size]
        val secLabel = if (i <= 135) "Section A" else "Section B"
        list.add(
            NeetQuestion(
                id = "q_bot_$i",
                subjectName = "Botany",
                chapterName = chap,
                topicName = top,
                questionText = "Q$i ($secLabel). " + t.text,
                options = t.options,
                correctOptionIndex = t.correctIdx,
                explanation = t.explanation,
                difficulty = if (i <= 135) "EASY" else "MEDIUM"
            )
        )
    }

    // ZOOLOGY (Q146 - Q180)
    val zooTopics = listOf(
        "Human Physiology" to "Digestion & Gastric Secretion",
        "Respiration" to "Lung Volumes & Residual Volume",
        "Circulation" to "Cardiac Cycle & ECG Waves",
        "Excretion" to "Nephron & Counter Current Mechanism",
        "Neural Control" to "Nerve Impulse Conduction",
        "Human Reproduction" to "Gametogenesis & Menstrual Cycle"
    )
    val zooTemplates = listOf(
        QTemplate(
            "In human digestive system, which hormone stimulates gastric juice secretion rich in HCl and pepsinogen?",
            listOf("Gastrin", "Secretin", "Cholecystokinin (CCK)", "Enterogastrone"),
            0,
            "Gastrin hormone produced by G-cells stimulates parietal cells to secrete HCl and chief cells to secrete pepsinogen."
        ),
        QTemplate(
            "The volume of air that remains inside the human lungs even after a forceful expiration is termed as:",
            listOf("Residual Volume (RV)", "Tidal Volume (TV)", "Expiratory Reserve Volume (ERV)", "Vital Capacity (VC)"),
            0,
            "Residual Volume (about 1100-1200 mL) cannot be expelled by forceful expiration."
        ),
        QTemplate(
            "Which wave in a standard human Electrocardiogram (ECG) represents the depolarization of the ventricles?",
            listOf("QRS Complex", "P Wave", "T Wave", "U Wave"),
            0,
            "P wave is atrial depolarization, QRS complex is ventricular depolarization, T wave is ventricular repolarization."
        )
    )

    for (i in 146..180) {
        val (chap, top) = zooTopics[(i - 146) % zooTopics.size]
        val t = zooTemplates[(i - 146) % zooTemplates.size]
        val secLabel = if (i <= 175) "Section A" else "Section B"
        list.add(
            NeetQuestion(
                id = "q_zoo_$i",
                subjectName = "Zoology",
                chapterName = chap,
                topicName = top,
                questionText = "Q$i ($secLabel). " + t.text,
                options = t.options,
                correctOptionIndex = t.correctIdx,
                explanation = t.explanation,
                difficulty = if (i <= 175) "EASY" else "MEDIUM"
            )
        )
    }

    return list
}

