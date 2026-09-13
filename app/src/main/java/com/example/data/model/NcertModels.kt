package com.example.data.model

/**
 * NCERT Library Data Models & Official Catalog.
 *
 * Provides a clean, data-driven, hierarchical representation of the official NCERT curriculum
 * for Classes 9, 10, 11, and 12.
 *
 * Hierarchy:
 * Class -> Subject -> Book -> Chapter
 *
 * Features:
 * - Stable IDs for seamless future PYQ & AI question generator integration.
 * - Official NCERT textbook portal & PDF URL metadata (https://ncert.nic.in/textbook.php).
 * - Multi-part book support (Part I, Part II).
 * - Multi-language support (English & Hindi metadata).
 * - Rationalised 2024-2025/2026 NCERT curriculum alignment.
 * - Ready for remote updates via Firestore/cloud sync without UI modification.
 */

data class NcertClass(
    val id: String, // e.g. "class_11"
    val classNumber: Int, // 9, 10, 11, 12
    val displayName: String, // "Class 11"
    val tagLine: String = "Official NCERT textbooks for focused learning",
    val quote: String = "Strong basics create brighter futures. — NCERT",
    val displayOrder: Int
)

data class NcertSubject(
    val id: String, // e.g. "class11_physics"
    val classId: String, // "class_11"
    val classNumber: Int,
    val name: String, // "Physics"
    val hindiName: String? = null, // "भौतिक विज्ञान"
    val iconKey: String = "atom", // "atom", "flask", "leaf", "pi", "book", "hindi"
    val colorHex: String = "#38BDF8", // Cyan / Blue / Emerald accent
    val displayOrder: Int,
    val availableLanguages: List<String> = listOf("en", "hi")
)

data class NcertBook(
    val id: String, // e.g. "class11_physics_part1"
    val subjectId: String, // "class11_physics"
    val classId: String, // "class_11"
    val classNumber: Int,
    val title: String, // "Physics Part I"
    val hindiTitle: String? = null, // "भौतिकी भाग I"
    val partNumber: Int = 1,
    val bookCode: String, // Official NCERT textbook code, e.g. "keph1"
    val chaptersCount: Int,
    val edition: String = "Latest Edition",
    val medium: String = "English, Hindi",
    val officialBookUrl: String = "https://ncert.nic.in/textbook.php",
    val displayOrder: Int
) {
    /**
     * Builds the official portal web URL for this specific book.
     */
    fun getOfficialPortalUrl(): String {
        return "https://ncert.nic.in/textbook.php?$bookCode=0-$chaptersCount"
    }
}

data class NcertChapter(
    val id: String, // Stable ID e.g. "class11_physics_part1_ch01"
    val bookId: String, // "class11_physics_part1"
    val subjectId: String, // "class11_physics"
    val classNumber: Int, // 11
    val chapterNumber: Int, // 1
    val chapterNumberFormatted: String, // "01"
    val title: String, // "Units and Measurements"
    val hindiTitle: String? = null, // "मात्रक और मापन"
    val description: String = "", // Chapter overview / key concept preview
    val estimatedPages: Int = 28,
    val bookCode: String, // "keph1"
    val officialChapterUrl: String, // "https://ncert.nic.in/textbook.php?keph1=1-8"
    val officialPdfUrl: String, // "https://ncert.nic.in/textbook/pdf/keph101.pdf"
    val language: String = "en", // "en" or "hi"
    val displayOrder: Int,

    // =========================================================================
    // FUTURE PYQ & AI QUESTION GENERATOR MAPPING HOOKS
    // =========================================================================
    val syllabusTag: String = "", // e.g. "NEET_PHY_UNITS_MEASUREMENTS"
    val subtopics: List<String> = emptyList() // subtopics for precision AI practice
)

/**
 * Result model for search operations across the NCERT library.
 */
data class NcertSearchResult(
    val chapter: NcertChapter,
    val bookTitle: String,
    val subjectName: String,
    val classDisplayName: String,
    val matchType: String = "Chapter" // "Chapter", "Book", "Subject"
)

/**
 * Single source of truth catalog for official NCERT textbooks.
 * Can be refreshed or augmented from Firestore at runtime.
 */
object NcertCatalog {

    val classes: List<NcertClass> = listOf(
        NcertClass(id = "class_9", classNumber = 9, displayName = "Class 9", displayOrder = 1),
        NcertClass(id = "class_10", classNumber = 10, displayName = "Class 10", displayOrder = 2),
        NcertClass(id = "class_11", classNumber = 11, displayName = "Class 11", displayOrder = 3),
        NcertClass(id = "class_12", classNumber = 12, displayName = "Class 12", displayOrder = 4)
    )

    val subjects: List<NcertSubject> = listOf(
        // Class 11
        NcertSubject("class11_physics", "class_11", 11, "Physics", "भौतिक विज्ञान", "atom", "#38BDF8", 1),
        NcertSubject("class11_chemistry", "class_11", 11, "Chemistry", "रसायन विज्ञान", "flask", "#34D399", 2),
        NcertSubject("class11_biology", "class_11", 11, "Biology", "जीव विज्ञान", "leaf", "#10B981", 3),
        NcertSubject("class11_maths", "class_11", 11, "Mathematics", "गणित", "pi", "#A78BFA", 4),
        NcertSubject("class11_english", "class_11", 11, "English", "अंग्रेज़ी", "book", "#F59E0B", 5),
        NcertSubject("class11_hindi", "class_11", 11, "Hindi", "हिन्दी", "hindi", "#EC4899", 6),

        // Class 12
        NcertSubject("class12_physics", "class_12", 12, "Physics", "भौतिक विज्ञान", "atom", "#38BDF8", 1),
        NcertSubject("class12_chemistry", "class_12", 12, "Chemistry", "रसायन विज्ञान", "flask", "#34D399", 2),
        NcertSubject("class12_biology", "class_12", 12, "Biology", "जीव विज्ञान", "leaf", "#10B981", 3),
        NcertSubject("class12_maths", "class_12", 12, "Mathematics", "गणित", "pi", "#A78BFA", 4),
        NcertSubject("class12_english", "class_12", 12, "English", "अंग्रेज़ी", "book", "#F59E0B", 5),
        NcertSubject("class12_hindi", "class_12", 12, "Hindi", "हिन्दी", "hindi", "#EC4899", 6),

        // Class 10
        NcertSubject("class10_science", "class_10", 10, "Science", "विज्ञान", "atom", "#38BDF8", 1),
        NcertSubject("class10_maths", "class_10", 10, "Mathematics", "गणित", "pi", "#A78BFA", 2),
        NcertSubject("class10_english", "class_10", 10, "English", "अंग्रेज़ी", "book", "#F59E0B", 3),
        NcertSubject("class10_social", "class_10", 10, "Social Science", "सामाजिक विज्ञान", "leaf", "#10B981", 4),
        NcertSubject("class10_hindi", "class_10", 10, "Hindi", "हिन्दी", "hindi", "#EC4899", 5),

        // Class 9
        NcertSubject("class9_science", "class_9", 9, "Science", "विज्ञान", "atom", "#38BDF8", 1),
        NcertSubject("class9_maths", "class_9", 9, "Mathematics", "गणित", "pi", "#A78BFA", 2),
        NcertSubject("class9_english", "class_9", 9, "English", "अंग्रेज़ी", "book", "#F59E0B", 3),
        NcertSubject("class9_social", "class_9", 9, "Social Science", "सामाजिक विज्ञान", "leaf", "#10B981", 4),
        NcertSubject("class9_hindi", "class_9", 9, "Hindi", "हिन्दी", "hindi", "#EC4899", 5)
    )

    val books: List<NcertBook> = listOf(
        // Class 11 Physics
        NcertBook("class11_physics_part1", "class11_physics", "class_11", 11, "Physics Part I", "भौतिकी भाग I", 1, "keph1", 7, displayOrder = 1),
        NcertBook("class11_physics_part2", "class11_physics", "class_11", 11, "Physics Part II", "भौतिकी भाग II", 2, "keph2", 7, displayOrder = 2),

        // Class 11 Chemistry
        NcertBook("class11_chem_part1", "class11_chemistry", "class_11", 11, "Chemistry Part I", "रसायन विज्ञान भाग I", 1, "kech1", 6, displayOrder = 1),
        NcertBook("class11_chem_part2", "class11_chemistry", "class_11", 11, "Chemistry Part II", "रसायन विज्ञान भाग II", 2, "kech2", 3, displayOrder = 2),

        // Class 11 Biology
        NcertBook("class11_bio_book", "class11_biology", "class_11", 11, "Biology", "जीव विज्ञान", 1, "kebo1", 19, displayOrder = 1),

        // Class 11 Mathematics
        NcertBook("class11_math_book", "class11_maths", "class_11", 11, "Mathematics", "गणित", 1, "kemh1", 14, displayOrder = 1),

        // Class 11 English
        NcertBook("class11_eng_hornbill", "class11_english", "class_11", 11, "Hornbill", "हॉर्नबिल", 1, "kehb1", 8, displayOrder = 1),
        NcertBook("class11_eng_snapshots", "class11_english", "class_11", 11, "Snapshots", "स्नैपशॉट्स", 2, "kesp1", 5, displayOrder = 2),

        // Class 12 Physics
        NcertBook("class12_physics_part1", "class12_physics", "class_12", 12, "Physics Part I", "भौतिकी भाग I", 1, "leph1", 8, displayOrder = 1),
        NcertBook("class12_physics_part2", "class12_physics", "class_12", 12, "Physics Part II", "भौतिकी भाग II", 2, "leph2", 6, displayOrder = 2),

        // Class 12 Chemistry
        NcertBook("class12_chem_part1", "class12_chemistry", "class_12", 12, "Chemistry Part I", "रसायन विज्ञान भाग I", 1, "lech1", 5, displayOrder = 1),
        NcertBook("class12_chem_part2", "class12_chemistry", "class_12", 12, "Chemistry Part II", "रसायन विज्ञान भाग II", 2, "lech2", 5, displayOrder = 2),

        // Class 12 Biology
        NcertBook("class12_bio_book", "class12_biology", "class_12", 12, "Biology", "जीव विज्ञान", 1, "lebo1", 13, displayOrder = 1),

        // Class 12 Mathematics
        NcertBook("class12_math_part1", "class12_maths", "class_12", 12, "Mathematics Part I", "गणित भाग I", 1, "lemh1", 6, displayOrder = 1),
        NcertBook("class12_math_part2", "class12_maths", "class_12", 12, "Mathematics Part II", "गणित भाग II", 2, "lemh2", 7, displayOrder = 2),

        // Class 12 English
        NcertBook("class12_eng_flamingo", "class12_english", "class_12", 12, "Flamingo", "फ्लेमिंगो", 1, "lefl1", 13, displayOrder = 1),
        NcertBook("class12_eng_vistas", "class12_english", "class_12", 12, "Vistas", "विस्टा", 2, "levt1", 6, displayOrder = 2),

        // Class 10 Science, Maths, English & Social Science
        NcertBook("class10_science_book", "class10_science", "class_10", 10, "Science", "विज्ञान", 1, "jesc1", 13, displayOrder = 1),
        NcertBook("class10_math_book", "class10_maths", "class_10", 10, "Mathematics", "गणित", 1, "jemh1", 14, displayOrder = 1),
        NcertBook("class10_eng_firstflight", "class10_english", "class_10", 10, "First Flight", "फर्स्ट फ्लाइट", 1, "jeff1", 9, displayOrder = 1),
        NcertBook("class10_eng_footprints", "class10_english", "class_10", 10, "Footprints Without Feet", "फुटप्रिंट्स विदाउट फीट", 2, "jefp1", 9, displayOrder = 2),
        NcertBook("class10_sst_geography", "class10_social", "class_10", 10, "Contemporary India II", "समकालीन भारत II", 1, "jess1", 7, displayOrder = 1),
        NcertBook("class10_sst_economics", "class10_social", "class_10", 10, "Understanding Economic Development", "आर्थिक विकास की समझ", 2, "jess2", 5, displayOrder = 2),
        NcertBook("class10_sst_history", "class10_social", "class_10", 10, "India and the Contemporary World II", "भारत और समकालीन विश्व II", 3, "jess3", 5, displayOrder = 3),
        NcertBook("class10_sst_civics", "class10_social", "class_10", 10, "Democratic Politics II", "लोकतांत्रिक राजनीति II", 4, "jess4", 5, displayOrder = 4),

        // Class 9 Science, Maths, English & Social Science
        NcertBook("class9_science_book", "class9_science", "class_9", 9, "Science", "विज्ञान", 1, "iesc1", 13, displayOrder = 1),
        NcertBook("class9_math_book", "class9_maths", "class_9", 9, "Mathematics", "गणित", 1, "iemh1", 8, displayOrder = 1),
        NcertBook("class9_eng_beehive", "class9_english", "class_9", 9, "Beehive / Kaveri", "बिहाइव / कावेरी", 1, "iebe1", 8, displayOrder = 1),
        NcertBook("class9_sst_soc", "class9_social", "class_9", 9, "Understanding Society", "समाज की समझ", 1, "iest1", 9, displayOrder = 1)
    )

    val chapters: List<NcertChapter> = buildList {
        // =====================================================================
        // CLASS 11 — PHYSICS PART I (keph1)
        // =====================================================================
        val c11p1 = listOf(
            Triple("Units and Measurements", "मात्रक और मापन", "Understand the need for measurement, units, significant figures, and the SI system of units."),
            Triple("Motion in a Straight Line", "सरल रेखा में गति", "Position, path length, displacement, velocity, acceleration, and kinematic equations."),
            Triple("Motion in a Plane", "समतल में गति", "Scalars, vectors, projectile motion, uniform circular motion, and relative velocity."),
            Triple("Laws of Motion", "गति के नियम", "Newton's laws of motion, inertia, momentum, impulse, friction, and circular dynamics."),
            Triple("Work, Energy and Power", "कार्य, ऊर्जा और शक्ति", "Work-energy theorem, kinetic and potential energy, conservation of energy, collisions."),
            Triple("System of Particles and Rotational Motion", "कणों के निकाय तथा घूर्णी गति", "Centre of mass, torque, angular momentum, moment of inertia, and rolling motion."),
            Triple("Gravitation", "गुरुत्वाकर्षण", "Kepler's laws, universal law of gravitation, gravitational potential energy, escape speed.")
        )
        c11p1.forEachIndexed { i, (en, hi, desc) ->
            val num = i + 1
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class11_physics_part1_ch$numStr",
                    bookId = "class11_physics_part1",
                    subjectId = "class11_physics",
                    classNumber = 11,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = desc,
                    estimatedPages = 28,
                    bookCode = "keph1",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?keph1=$num-7",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/keph1$numStr.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_PHY_11_${en.uppercase().replace(" ", "_").replace(",", "")}",
                    subtopics = listOf("Concepts & Definitions", "Derivations", "NCERT Exemplar Problems", "Formula Sheet")
                )
            )
        }

        // =====================================================================
        // CLASS 11 — PHYSICS PART II (keph2)
        // =====================================================================
        val c11p2 = listOf(
            Triple("Mechanical Properties of Solids", "ठोसों के यांत्रिक गुण", "Stress, strain, Hooke's law, stress-strain curve, Young's modulus, shear modulus."),
            Triple("Mechanical Properties of Fluids", "तरलों के यांत्रिक गुण", "Pascal's law, Archimedes principle, viscosity, Stokes' law, Bernoulli's principle."),
            Triple("Thermal Properties of Matter", "द्रव्य के तापीय गुण", "Temperature, heat, thermal expansion, specific heat capacity, Newton's law of cooling."),
            Triple("Thermodynamics", "ऊष्मागतिकी", "Thermal equilibrium, zeroth law, first law, isothermal and adiabatic processes, second law."),
            Triple("Kinetic Theory", "अणुगति सिद्धांत", "Molecular nature of matter, behaviour of gases, kinetic interpretation of temperature."),
            Triple("Oscillations", "दोलन", "Periodic and oscillatory motions, simple harmonic motion, resonance, damped oscillations."),
            Triple("Waves", "तरंगें", "Transverse and longitudinal waves, speed of wave, superposition principle, beats, Doppler effect.")
        )
        c11p2.forEachIndexed { i, (en, hi, desc) ->
            val num = i + 8
            val fileIndex = String.format("%02d", i + 1)
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class11_physics_part2_ch$numStr",
                    bookId = "class11_physics_part2",
                    subjectId = "class11_physics",
                    classNumber = 11,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = desc,
                    estimatedPages = 26,
                    bookCode = "keph2",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?keph2=${i + 1}-7",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/keph2$fileIndex.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_PHY_11_${en.uppercase().replace(" ", "_").replace(",", "")}",
                    subtopics = listOf("Core Concepts", "NCERT Exercises", "Summary & Points to Ponder")
                )
            )
        }

        // =====================================================================
        // CLASS 11 — CHEMISTRY PART I (kech1)
        // =====================================================================
        val c11c1 = listOf(
            Triple("Some Basic Concepts of Chemistry", "रसायन विज्ञान की कुछ मूल अवधारणाएँ", "Mole concept, atomic and molecular masses, stoichiometry, molarity, molality."),
            Triple("Structure of Atom", "परमाणु की संरचना", "Bohr model, quantum mechanical model, Heisenberg principle, de Broglie relation."),
            Triple("Classification of Elements and Periodicity", "तत्वों का वर्गीकरण एवं गुणधर्मों में आवर्तिता", "Modern periodic law, periodic trends in radii, ionization enthalpy, electron gain enthalpy."),
            Triple("Chemical Bonding and Molecular Structure", "रासायनिक आबंधन तथा आण्विक संरचना", "Valence bond theory, VSEPR theory, hybridization, molecular orbital theory."),
            Triple("Thermodynamics", "ऊष्मागतिकी", "State functions, enthalpy, entropy, Gibbs free energy, spontaneity of reactions."),
            Triple("Equilibrium", "साम्यावस्था", "Physical and chemical equilibria, Le Chatelier's principle, ionic equilibrium, pH, buffer.")
        )
        c11c1.forEachIndexed { i, (en, hi, desc) ->
            val num = i + 1
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class11_chem_part1_ch$numStr",
                    bookId = "class11_chem_part1",
                    subjectId = "class11_chemistry",
                    classNumber = 11,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = desc,
                    estimatedPages = 30,
                    bookCode = "kech1",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?kech1=$num-6",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/kech1$numStr.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_CHEM_11_${en.uppercase().replace(" ", "_")}"
                )
            )
        }

        // =====================================================================
        // CLASS 11 — CHEMISTRY PART II (kech2)
        // =====================================================================
        val c11c2 = listOf(
            Triple("Redox Reactions", "अपचयोपचय अभिक्रियाएँ", "Oxidation number, balancing redox reactions, electrochemical cells."),
            Triple("Organic Chemistry – Basic Principles & Techniques", "कार्बनिक रसायन: कुछ आधारभूत सिद्धांत तथा तकनीकें", "IUPAC nomenclature, inductive effect, resonance, hyperconjugation, reaction mechanisms."),
            Triple("Hydrocarbons", "हाइड्रोकार्बन", "Alkanes, alkenes, alkynes, aromatic hydrocarbons, electrophilic substitution reactions.")
        )
        c11c2.forEachIndexed { i, (en, hi, desc) ->
            val num = i + 7
            val fileIndex = String.format("%02d", i + 1)
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class11_chem_part2_ch$numStr",
                    bookId = "class11_chem_part2",
                    subjectId = "class11_chemistry",
                    classNumber = 11,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = desc,
                    estimatedPages = 32,
                    bookCode = "kech2",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?kech2=${i + 1}-3",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/kech2$fileIndex.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_CHEM_11_${en.uppercase().replace(" ", "_")}"
                )
            )
        }

        // =====================================================================
        // CLASS 11 — BIOLOGY (kebo1)
        // =====================================================================
        val c11bio = listOf(
            Pair("The Living World", "जीव जगत"),
            Pair("Biological Classification", "जीव जगत का वर्गीकरण"),
            Pair("Plant Kingdom", "वनस्पति जगत"),
            Pair("Animal Kingdom", "प्राणि जगत"),
            Pair("Morphology of Flowering Plants", "पुष्पी पादपों की आकारिकी"),
            Pair("Anatomy of Flowering Plants", "पुष्पी पादपों का शारीर"),
            Pair("Structural Organisation in Animals", "प्राणियों में संरचनात्मक संगठन"),
            Pair("Cell: The Unit of Life", "कोशिका: जीवन की इकाई"),
            Pair("Biomolecules", "जैव अणु"),
            Pair("Cell Cycle and Cell Division", "कोशिका चक्र और कोशिका विभाजन"),
            Pair("Photosynthesis in Higher Plants", "उच्च पादपों में प्रकाश-संश्लेषण"),
            Pair("Respiration in Plants", "पादप में श्वसन"),
            Pair("Plant Growth and Development", "पादप वृद्धि एवं परिवर्धन"),
            Pair("Breathing and Exchange of Gases", "श्वसन और गैसों का विनिमय"),
            Pair("Body Fluids and Circulation", "शरीर द्रव तथा परिसंचरण"),
            Pair("Excretory Products and their Elimination", "उत्सर्जी उत्पाद एवं उनका निष्कासन"),
            Pair("Locomotion and Movement", "गमन एवं संचलन"),
            Pair("Neural Control and Coordination", "तंत्रिकीय नियंत्रण एवं समन्वय"),
            Pair("Chemical Coordination and Integration", "रासायनिक समन्वय तथा एकीकरण")
        )
        c11bio.forEachIndexed { i, (en, hi) ->
            val num = i + 1
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class11_biology_ch$numStr",
                    bookId = "class11_bio_book",
                    subjectId = "class11_biology",
                    classNumber = 11,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = "Official NCERT chapter for NEET Biology foundational mastery.",
                    estimatedPages = 24,
                    bookCode = "kebo1",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?kebo1=$num-19",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/kebo1$numStr.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_BIO_11_${en.uppercase().replace(" ", "_").replace(":", "")}"
                )
            )
        }

        // =====================================================================
        // CLASS 12 — PHYSICS PART I (leph1)
        // =====================================================================
        val c12p1 = listOf(
            Triple("Electric Charges and Fields", "वैद्युत आवेश तथा क्षेत्र", "Coulomb's law, electric field, flux, Gauss's law and its applications."),
            Triple("Electrostatic Potential and Capacitance", "स्थिरवैद्युत विभव तथा धारिता", "Potential, equipotential surfaces, capacitors in series and parallel, dielectrics."),
            Triple("Current Electricity", "विद्युत धारा", "Ohm's law, drift velocity, resistivity, Kirchhoff's rules, Wheatstone bridge."),
            Triple("Moving Charges and Magnetism", "गतिमान आवेश और चुंबकत्व", "Biot-Savart law, Ampere's circuital law, solenoid, magnetic force on moving charge."),
            Triple("Magnetism and Matter", "चुंबकत्व एवं द्रव्य", "Bar magnet, magnetic field lines, Earth's magnetism, dia, para and ferromagnetic substances."),
            Triple("Electromagnetic Induction", "वैद्युतचुंबकीय प्रेरण", "Faraday's laws, Lenz's law, eddy currents, self and mutual induction."),
            Triple("Alternating Current", "प्रत्यावर्ती धारा", "Peak and RMS values, reactance, impedance, LCR series circuit, resonance, power factor."),
            Triple("Electromagnetic Waves", "वैद्युतचुंबकीय तरंगें", "Displacement current, EM wave characteristics, electromagnetic spectrum.")
        )
        c12p1.forEachIndexed { i, (en, hi, desc) ->
            val num = i + 1
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class12_physics_part1_ch$numStr",
                    bookId = "class12_physics_part1",
                    subjectId = "class12_physics",
                    classNumber = 12,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = desc,
                    estimatedPages = 32,
                    bookCode = "leph1",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?leph1=$num-8",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/leph1$numStr.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_PHY_12_${en.uppercase().replace(' ', '_')}"
                )
            )
        }

        // =====================================================================
        // CLASS 12 — PHYSICS PART II (leph2)
        // =====================================================================
        val c12p2 = listOf(
            Triple("Ray Optics and Optical Instruments", "किरण प्रकाशिकी एवं प्रकाशिक यंत्र", "Refraction, total internal reflection, lens formula, prism, microscope, telescope."),
            Triple("Wave Optics", "तरंग-प्रकाशिकी", "Huygens principle, interference, Young's double slit experiment, diffraction."),
            Triple("Dual Nature of Radiation and Matter", "विकिरण तथा द्रव्य की द्वैत प्रकृति", "Photoelectric effect, Einstein's equation, de Broglie wavelength."),
            Triple("Atoms", "परमाणु", "Rutherford model, Bohr model, hydrogen spectrum, energy levels."),
            Triple("Nuclei", "नाभिक", "Nuclear composition, binding energy per nucleon, radioactivity, fission and fusion."),
            Triple("Semiconductor Electronics", "अर्धचालक इलेक्ट्रॉनिकी: पदार्थ, युक्तियाँ तथा सरल परिपथ", "p-n junction diode, rectifier, Zener diode, logic gates.")
        )
        c12p2.forEachIndexed { i, (en, hi, desc) ->
            val num = i + 9
            val fileIndex = String.format("%02d", i + 1)
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class12_physics_part2_ch$numStr",
                    bookId = "class12_physics_part2",
                    subjectId = "class12_physics",
                    classNumber = 12,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = desc,
                    estimatedPages = 28,
                    bookCode = "leph2",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?leph2=${i + 1}-6",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/leph2$fileIndex.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_PHY_12_${en.uppercase().replace(' ', '_')}"
                )
            )
        }

        // =====================================================================
        // CLASS 12 — CHEMISTRY PART I (lech1)
        // =====================================================================
        val c12c1 = listOf(
            Triple("Solutions", "विलयन", "Raoult's law, colligative properties, elevation of boiling point, depression of freezing point, van 't Hoff factor."),
            Triple("Electrochemistry", "वैद्युतरसायन", "Nernst equation, conductance, Kohlrausch's law, electrolysis, batteries, fuel cells."),
            Triple("Chemical Kinetics", "रासायनिक बलगतिकी", "Rate of reaction, integrated rate equations, pseudo first order reaction, Arrhenius equation."),
            Triple("The d- and f-Block Elements", "d- एवं f-ब्लॉक के तत्व", "Electronic configuration, transition elements, oxidation states, lanthanoid contraction."),
            Triple("Coordination Compounds", "उपसहसंयोजन यौगिक", "Werner's theory, IUPAC nomenclature, isomerism, crystal field theory (CFT).")
        )
        c12c1.forEachIndexed { i, (en, hi, desc) ->
            val num = i + 1
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class12_chem_part1_ch$numStr",
                    bookId = "class12_chem_part1",
                    subjectId = "class12_chemistry",
                    classNumber = 12,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = desc,
                    estimatedPages = 30,
                    bookCode = "lech1",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?lech1=$num-5",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/lech1$numStr.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_CHEM_12_${en.uppercase().replace(' ', '_')}"
                )
            )
        }

        // =====================================================================
        // CLASS 12 — CHEMISTRY PART II (lech2)
        // =====================================================================
        val c12c2 = listOf(
            Triple("Haloalkanes and Haloarenes", "हैलोऐल्केन तथा हैलोऐरीन", "Nomenclature, nature of C-X bond, SN1 and SN2 mechanisms, optical activity."),
            Triple("Alcohols, Phenols and Ethers", "ऐल्कोहॉल, फीनॉल एवं ईथर", "Preparation, physical properties, electrophilic substitution, Kolbe's reaction, Reimer-Tiemann."),
            Triple("Aldehydes, Ketones & Carboxylic Acids", "ऐल्डिहाइड, कीटोन एवं कार्बोक्सिलिक अम्ल", "Nucleophilic addition, aldol condensation, Cannizzaro reaction, acidic nature."),
            Triple("Amines", "ऐमीन", "Basicity of amines, Gabriel phthalimide synthesis, Hoffmann bromamide, carbylamine test."),
            Triple("Biomolecules", "जैव-अणु", "Carbohydrates, monosaccharides, proteins, amino acids, nucleic acids, DNA and RNA.")
        )
        c12c2.forEachIndexed { i, (en, hi, desc) ->
            val num = i + 6
            val fileIndex = String.format("%02d", i + 1)
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class12_chem_part2_ch$numStr",
                    bookId = "class12_chem_part2",
                    subjectId = "class12_chemistry",
                    classNumber = 12,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = desc,
                    estimatedPages = 32,
                    bookCode = "lech2",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?lech2=${i + 1}-5",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/lech2$fileIndex.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_CHEM_12_${en.uppercase().replace(' ', '_')}"
                )
            )
        }

        // =====================================================================
        // CLASS 12 — BIOLOGY (lebo1)
        // =====================================================================
        val c12bio = listOf(
            Pair("Sexual Reproduction in Flowering Plants", "पुष्पी पादपों में लैंगिक जनन"),
            Pair("Human Reproduction", "मानव जनन"),
            Pair("Reproductive Health", "जनन स्वास्थ्य"),
            Pair("Principles of Inheritance and Variation", "वंशागति तथा विविधता के सिद्धांत"),
            Pair("Molecular Basis of Inheritance", "वंशागति के आण्विक आधार"),
            Pair("Evolution", "विकास"),
            Pair("Human Health and Disease", "मानव स्वास्थ्य तथा रोग"),
            Pair("Microbes in Human Welfare", "मानव कल्याण में सूक्ष्मजीव"),
            Pair("Biotechnology: Principles and Processes", "जैव प्रौद्योगिकी - सिद्धांत व प्रक्रम"),
            Pair("Biotechnology and its Applications", "जैव प्रौद्योगिकी एवं उसके उपयोग"),
            Pair("Organisms and Populations", "जीव और समष्टियाँ"),
            Pair("Ecosystem", "पारितंत्र"),
            Pair("Biodiversity and Conservation", "जैव-विविधता एवं संरक्षण")
        )
        c12bio.forEachIndexed { i, (en, hi) ->
            val num = i + 1
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class12_biology_ch$numStr",
                    bookId = "class12_bio_book",
                    subjectId = "class12_biology",
                    classNumber = 12,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = "Official high-yield NCERT chapter for NEET Biology preparation.",
                    estimatedPages = 28,
                    bookCode = "lebo1",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?lebo1=$num-13",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/lebo1$numStr.pdf",
                    displayOrder = num,
                    syllabusTag = "NEET_BIO_12_${en.uppercase().replace(" ", "_").replace(":", "")}"
                )
            )
        }

        // =====================================================================
        // CLASS 10 — SCIENCE (jesc1)
        // =====================================================================
        val c10sci = listOf(
            Pair("Chemical Reactions and Equations", "रासायनिक अभिक्रियाएँ एवं समीकरण"),
            Pair("Acids, Bases and Salts", "अम्ल, क्षारक एवं लवण"),
            Pair("Metals and Non-metals", "धातु एवं अधातु"),
            Pair("Carbon and its Compounds", "कार्बन एवं उसके यौगिक"),
            Pair("Life Processes", "जैव प्रक्रम"),
            Pair("Control and Coordination", "नियंत्रण एवं समन्वय"),
            Pair("How do Organisms Reproduce?", "जीव जनन कैसे करते हैं?"),
            Pair("Heredity", "आनुवंशिकता"),
            Pair("Light – Reflection and Refraction", "प्रकाश – परावर्तन तथा अपवर्तन"),
            Pair("The Human Eye and Colourful World", "मानव नेत्र तथा रंगबिरंगा संसार"),
            Pair("Electricity", "विद्युत"),
            Pair("Magnetic Effects of Electric Current", "विद्युत धारा के चुंबकीय प्रभाव"),
            Pair("Our Environment", "हमारा पर्यावरण")
        )
        c10sci.forEachIndexed { i, (en, hi) ->
            val num = i + 1
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class10_science_ch$numStr",
                    bookId = "class10_science_book",
                    subjectId = "class10_science",
                    classNumber = 10,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = "Foundation science chapter from official NCERT Class 10 textbook.",
                    estimatedPages = 22,
                    bookCode = "jesc1",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?jesc1=$num-13",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/jesc1$numStr.pdf",
                    displayOrder = num,
                    syllabusTag = "FOUNDATION_SCI_10_${en.uppercase().replace(' ', '_')}"
                )
            )
        }

        // =====================================================================
        // CLASS 9 — SCIENCE (iesc1)
        // =====================================================================
        val c9sci = listOf(
            Pair("Matter in Our Surroundings", "हमारे आस-पास के पदार्थ"),
            Pair("Is Matter Around Us Pure?", "क्या हमारे आस-पास के पदार्थ शुद्ध हैं?"),
            Pair("Atoms and Molecules", "परमाणु एवं अणु"),
            Pair("Structure of the Atom", "परमाणु की संरचना"),
            Pair("The Fundamental Unit of Life", "जीवन की मौलिक इकाई"),
            Pair("Tissues", "ऊतक"),
            Pair("Motion", "गति"),
            Pair("Force and Laws of Motion", "बल तथा गति के नियम"),
            Pair("Gravitation", "गुरुत्वाकर्षण"),
            Pair("Work and Energy", "कार्य तथा ऊर्जा"),
            Pair("Sound", "ध्वनि"),
            Pair("Improvement in Food Resources", "खाद्य संसाधनों में सुधार")
        )
        c9sci.forEachIndexed { i, (en, hi) ->
            val num = i + 1
            val numStr = String.format("%02d", num)
            add(
                NcertChapter(
                    id = "class9_science_ch$numStr",
                    bookId = "class9_science_book",
                    subjectId = "class9_science",
                    classNumber = 9,
                    chapterNumber = num,
                    chapterNumberFormatted = numStr,
                    title = en,
                    hindiTitle = hi,
                    description = "Core foundational concepts from official NCERT Class 9 textbook.",
                    estimatedPages = 20,
                    bookCode = "iesc1",
                    officialChapterUrl = "https://ncert.nic.in/textbook.php?iesc1=$num-12",
                    officialPdfUrl = "https://ncert.nic.in/textbook/pdf/iesc1$numStr.pdf",
                    displayOrder = num,
                    syllabusTag = "FOUNDATION_SCI_9_${en.uppercase().replace(' ', '_')}"
                )
            )
        }
    }
}
