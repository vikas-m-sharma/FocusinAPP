# HISTORICAL PYQ CONVERSION & INGESTION PIPELINE

## 1. Overview
The FOCUSIN Historical PYQ Conversion Pipeline provides a deterministic, idempotent, and error-tolerant system for converting diverse archival raw inputs (JSON, CSV, Plain Text/OCR) into canonical question structures.

```
┌─────────────────┐     ┌──────────────────────┐     ┌────────────────────────┐
│ Archival Source │ ──> │   PyqSourceParser    │ ──> │ PyqConversionPipeline  │
│ (JSON/CSV/TXT)  │     │ (Format Recognition) │     │ (Normalization & Map)  │
└─────────────────┘     └──────────────────────┘     └───────────┬────────────┘
                                                                 │
                                ┌────────────────────────────────┴───────────────┐
                                │                                                │
                                ▼                                                ▼
                     ┌──────────────────────┐                         ┌──────────────────────┐
                     │ Clean & Verified     │                         │ Flagged / Unmapped   │
                     │ (Has Primary Evid.)  │                         │ Missing Answer / Key │
                     └──────────┬───────────┘                         └──────────┬───────────┘
                                │                                                │
                                ▼                                                ▼
                     ┌──────────────────────┐                         ┌──────────────────────┐
                     │ Room Database        │                         │ PyqReviewQueueManager│
                     │ (LearningDao)        │ <────────────────────── │ (Human-in-the-Loop)  │
                     └──────────────────────┘     Admin Review Action └──────────────────────┘
```

---

## 2. Ingestion Stages & Responsibilities

### Stage 1: Source Document Ingestion
- Accepts raw text content with `PyqSourceMetadata` (source name, source type, primary evidence URI, document ID).
- Accepts optional separate answer key string (e.g., `1: B, 2: D, 3: A...`).

### Stage 2: Parsing (`PyqSourceParser`)
- **JSON**: Parses array of JSON question objects with loose attribute aliases (`questionText`, `question`, `stem`, `options`, `choices`, `answer`, `key`, `subject`, `chapter`).
- **CSV**: Parses comma/semicolon-delimited rows with RFC-4180 quote escaping.
- **TXT / OCR Text**: Regex-based tokenization extracting question headers (`1.`, `Q2:`, `[3]`), standard option labels `(A)...(D)`, inline answer keys (`Ans: (B)` or `Answer - Option 2`), and explanation blocks.

### Stage 3: Normalization & Answer Mapping
- Formats options into a strict 4-element array.
- Translates answers from characters (`A`, `B`, `C`, `D`), digits (`1`, `2`, `3`, `4`), or direct matching text into a zero-based index (`0..3`).
- Reconciles separate external answer keys when provided.
- Marks `isMissingAnswer = true` if no unambiguous answer key can be assigned.

### Stage 4: Subject & Chapter Mapping (`PyqChapterMapper`)
- Maps raw chapter text to canonical NEET syllabus identifiers (`neet_phy_...`, `neet_chem_...`, `neet_bio_...`).
- Applies fallback keyword analysis across question stem if raw chapter is absent or ambiguous.
- Questions that cannot be mapped are assigned `chapterId = "UNMAPPED"` and routed to the Human Review Queue.

### Stage 5: Deduplication & Fingerprinting
- **Natural Key Check**: Checks for collision on `${exam}-${year}-${session}-${questionNumber}`.
- **Text Fingerprint Check**: Normalizes question text (lowercased alphanumeric tokens) and compares against existing database records to detect unnumbered duplicates.

### Stage 6: Verification Status Determination
- `VERIFIED`: Assigned ONLY if `primaryEvidenceUri` is present OR `sourceType == OFFICIAL_PUBLIC`.
- `UNVERIFIED`: Assigned if question is syntactically valid but lacks primary documentary evidence.
- `SAMPLE`: Reserved for local demonstration data.

---

## 3. Human-in-the-Loop Review Queue (`PyqReviewQueueManager`)
Questions flagged for any of the following reasons are held in the Review Queue:
- Unmapped chapter (`chapterId == "UNMAPPED"`)
- Missing or ambiguous answer index (`correctOptionIndex !in 0..3`)
- Duplicate warning flagged
- Unverified questions requiring official citation entry before elevation to `VERIFIED`

### Review Actions:
1. `Approve as VERIFIED`: Requires entering a verified evidence citation (e.g., official NTA answer key document ID).
2. `Keep UNVERIFIED`: Commits the question with `sourceVerificationStatus = "UNVERIFIED"`.
3. `Update Chapter`: Allows assigning the canonical NEET subject, chapter, and topic.
4. `Update Answer`: Allows assigning the correct option index (0 to 3).
5. `Reject`: Discards the invalid or corrupt question record.

---

## 4. Current Status
**Actual historical PYQ dataset has not yet been imported. The ingestion framework is ready for legitimate source material.**
All 21 years (2005–2025) are tracked in `manifest.json` and in the Settings Dataset Audit view.
