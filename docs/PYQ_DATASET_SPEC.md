# FOCUSIN Historical PYQ Dataset Specification (v1.0)

## 1. Overview & Zero-Synthetic Policy

This document defines the strict specification, schema, and verification contract for historical AIPMT (2005–2012, 2014, 2015) and NEET UG (2013, 2016–2025) question datasets ingested into the FOCUSIN learning platform.

### Core Policy Mandates
1. **Decoupling of Synthetic Questions**: Under Phase 3.2 and Phase 3.3, generated, paraphrased, or AI-synthesized questions are strictly forbidden from bearing historical exam or PYQ metadata.
2. **Real Evidence Requirement**: A question can only carry `sourceVerificationStatus: "VERIFIED"` when verified against official primary-source exam papers and official answer keys.
3. **No Automatic Promotion**: Unverified questions (`UNVERIFIED`) must never be automatically promoted to `VERIFIED`.
4. **Authenticity Over Completeness**: The application explicitly reports missing years and actual database counts rather than disguising gaps with synthetic questions or inflated coverage statistics.

---

## 2. Dataset Contract Schema

Every question entry in an ingested JSON file MUST conform to the following JSON schema:

```json
{
  "id": "AIPMT_2008_PHY_001",
  "sourceExam": "AIPMT",
  "examYear": 2008,
  "paperSession": "MAIN",
  "historicalPaperId": "AIPMT_2008_PRELIMS",
  "originalQuestionNumber": 1,

  "subjectId": "PHYSICS",
  "chapterId": "neet_phy_current_electricity",
  "topicName": "Ohm's Law & Resistance",

  "questionText": "A wire of resistance 12 Ω is bent in the form of a circle. The effective resistance between the ends of any diameter is:",
  "options": [
    "3 Ω",
    "6 Ω",
    "12 Ω",
    "24 Ω"
  ],

  "correctOptionIndex": 0,
  "explanation": "When the wire is bent into a circle, the two semicircular portions each have a resistance of 6 Ω in parallel. Equivalent resistance = (6 * 6)/(6 + 6) = 3 Ω.",

  "difficulty": "EASY",

  "syllabusStatus": "CURRENT",

  "isOfficialPYQ": true,

  "sourceVerificationStatus": "VERIFIED",

  "sourceReference": {
    "sourceType": "OFFICIAL_EXAM_PAPER",
    "sourceName": "CBSE AIPMT 2008 Preliminary Question Paper Code A",
    "sourceLocator": "Page 2, Question 1; Answer Key Item 1"
  }
}
```

---

## 3. Field Definitions & Value Constraints

| Field | Type | Required | Allowed Values / Constraints | Description |
|---|---|---|---|---|
| `id` | String | Yes | Non-blank, format: `{EXAM}_{YEAR}_{SUBJ}_{NUM}` | Globally unique question identifier. |
| `sourceExam` | String | Yes | `"AIPMT"`, `"NEET_UG"`, `"NEET_RE"`, `"SAMPLE"` | The conducting exam framework. |
| `examYear` | Integer | Yes | `2005` to `2025` | The historical examination year. |
| `paperSession` | String | Yes | `"MAIN"`, `"PRELIMS"`, `"MAINS"`, `"PHASE_1"`, `"PHASE_2"`, `"RE_EXAM"`, `"ODISHA"`, `"MANIPUR"` | Specific exam session or phase. |
| `historicalPaperId` | String | Optional | e.g., `"AIPMT_2008_PRELIMS"` | Reference ID for the complete historical question paper. |
| `originalQuestionNumber` | Integer | Optional | `1` to `200` | Question index as printed in original test booklet. |
| `subjectId` | String | Yes | `"PHYSICS"`, `"CHEMISTRY"`, `"BIOLOGY"` | Normalized canonical subject identifier. |
| `chapterId` | String | Yes | Must match `NeetChapter.id` in FOCUSIN catalog | Validated chapter slug. Cannot be unmapped. |
| `topicName` | String | Yes | Non-blank string | Curricular topic descriptor. |
| `questionText` | String | Yes | Non-blank string | Exact verbatim text of the question. |
| `options` | Array of Strings | Yes | Exactly 4 non-blank strings | Option choices [Option A, Option B, Option C, Option D]. |
| `correctOptionIndex` | Integer | Yes | `0`, `1`, `2`, or `3` | Zero-based index of the correct option (0 = A, 1 = B, 2 = C, 3 = D). |
| `explanation` | String | Yes | Non-blank string | Pedagogical step-by-step solution. |
| `difficulty` | String | Yes | `"EASY"`, `"MEDIUM"`, `"HARD"` | Difficulty classification. |
| `syllabusStatus` | String | Yes | `"CURRENT"`, `"RATIONALIZED"`, `"OUT_OF_CURRENT_SYLLABUS"`, `"UNKNOWN"` | Syllabus relevance under current NTA curriculum. |
| `isOfficialPYQ` | Boolean | Yes | `true` or `false` | Must be `true` only if verified official past exam question. |
| `sourceVerificationStatus` | String | Yes | `"VERIFIED"`, `"UNVERIFIED"`, `"SAMPLE"`, `"GENERATED"` | Authoritative verification level. |
| `sourceReference` | Object | Conditional | Contains `sourceType`, `sourceName`, `sourceLocator` | Required if `sourceVerificationStatus == "VERIFIED"`. |

---

## 4. Verification Rules & Proof Criteria

### VERIFIED Criteria
A question is marked `VERIFIED` if and only if:
1. It is matched against an official paper archive published by CBSE (pre-2019) or NTA (2019–present).
2. It has an explicit `sourceReference` object containing:
   - `sourceType`: `"OFFICIAL_EXAM_PAPER"` or `"PUBLIC_GAZETTE"`
   - `sourceName`: Title and booklet code of the primary paper.
   - `sourceLocator`: Page and question number cross-referenced against the official master answer key.
3. If bundled offline, a matching PDF page or text hash exists in the project assets.

### UNVERIFIED Criteria
Any question:
- Lacking primary-source document citation.
- Sourced from secondary compilations without answer key verification.
- Curated for development testing.
Unverified questions MUST NOT be displayed as verified official past questions.

---

## 5. Historical Exam & Year Catalog (2005–2025)

The historical archive spans 21 exam cycles across two eras:

### Era 1: AIPMT (Central Board of Secondary Education)
- **2005**: AIPMT Prelims (Expected: 200 questions — 50 Phy, 50 Chem, 100 Bio)
- **2006**: AIPMT Prelims (Expected: 200 questions)
- **2007**: AIPMT Prelims (Expected: 200 questions)
- **2008**: AIPMT Prelims (Expected: 200 questions)
- **2009**: AIPMT Prelims (Expected: 200 questions)
- **2010**: AIPMT Prelims (Expected: 200 questions)
- **2011**: AIPMT Prelims (Expected: 200 questions)
- **2012**: AIPMT Prelims (Expected: 200 questions)
- **2014**: AIPMT (Expected: 180 questions — 45 Phy, 45 Chem, 90 Bio)
- **2015**: AIPMT / Re-AIPMT (Expected: 180 questions each)

### Era 2: NEET UG (MCI / NTA)
- **2013**: NEET UG (First edition — Expected: 180 questions)
- **2016**: NEET UG Phase 1 & Phase 2 (Expected: 180 questions each)
- **2017**: NEET UG (Expected: 180 questions)
- **2018**: NEET UG (Expected: 180 questions)
- **2019**: NEET UG (National & Odisha — Expected: 180 questions)
- **2020**: NEET UG (Phase 1 & Phase 2 — Expected: 180 questions)
- **2021**: NEET UG (Section A/B format — Expected: 200 questions)
- **2022**: NEET UG (Section A/B format — Expected: 200 questions)
- **2023**: NEET UG (National & Manipur — Expected: 200 questions)
- **2024**: NEET UG (National & Re-test — Expected: 200 questions)
- **2025**: NEET UG (Expected: 200 questions)

---

## 6. Syllabus Evolution & Rationalized Topics

Historical questions are never deleted when NCERT rationalizes or drops a chapter. Instead, they are preserved with `syllabusStatus`:
- `"CURRENT"`: Retained in the active NEET UG syllabus.
- `"RATIONALIZED"`: Dropped or reduced in recent NCERT editions (e.g., Solid State, Surface Chemistry, Digestion, Hydrogen, P-block details).
- `"OUT_OF_CURRENT_SYLLABUS"`: Not included in current NTA information bulletin.
- `"UNKNOWN"`: Awaiting syllabus classification review.
Students can filter by syllabus status during practice sessions.

---

## 7. Manifest Specification (`manifest.json`)

Dataset bundles are cataloged in `assets/pyq/manifest.json`:

```json
{
  "version": "1.0",
  "lastUpdated": "2026-09-13",
  "datasets": [
    {
      "id": "aipmt_2008_phy",
      "exam": "AIPMT",
      "year": 2008,
      "session": "MAIN",
      "subject": "PHYSICS",
      "file": "aipmt/2008/aipmt_2008_physics.json",
      "expectedCount": 50,
      "status": "PARTIAL",
      "verificationStatus": "UNVERIFIED"
    }
  ]
}
```

Status lifecycle:
1. `NOT_IMPORTED`: Dataset file not yet provisioned.
2. `PARTIAL`: Incomplete question set for the session/subject.
3. `IMPORTED_UNVERIFIED`: Ingested into database, but pending primary source verification.
4. `VERIFIED`: Complete primary-source-verified dataset.
