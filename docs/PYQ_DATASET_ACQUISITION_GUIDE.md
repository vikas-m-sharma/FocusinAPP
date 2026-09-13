# HISTORICAL AIPMT & NEET UG DATASET ACQUISITION GUIDE

## 1. Purpose & Scope
This guide defines the authoritative protocols for acquiring authentic, verifiable historical Previous Year Questions (PYQs) for AIPMT (2005–2012, 2014–2015) and NEET UG (2013, 2016–2025) for the FOCUSIN platform.

---

## 2. Ethical, Legal, and Compliance Directives

> [!CRITICAL]
> Under NO circumstances may synthetic, AI-generated, or fabricated questions be labeled as authentic historical examination questions. 

1. **No Scraping of Protected Sites**: Never execute automated scrapers against unauthorized websites, bypass Cloudflare/WAF protections, solve captchas, or breach Terms of Service.
2. **No Hallucinated Answer Keys**: Every answer key must originate from official public release keys (CBSE/NTA) or audited academic consensus keys with recorded provenance.
3. **No Automatic "VERIFIED" Elevation**: Questions imported from external datasets default to `UNVERIFIED` until primary-source evidence citations are linked and human-reviewed.

---

## 3. Legitimate Acquisition Sources

| Acquisition Channel | Authority / Origin | Permitted Use & Verification Level |
| :--- | :--- | :--- |
| **Official Examination Archives** | CBSE (AIPMT 2005–2015) / NTA (NEET 2016–2025) official question papers and official final answer keys | Eligible for `VERIFIED` status upon SHA-256 and URI attachment |
| **Public Government Libraries & Archives** | National Library of India, state archives, open government educational collections | Eligible for `VERIFIED` with documentary citation |
| **Licensed Historical Datasets** | Contracted, accredited educational publishers with formal copyright licenses | Marked `UNVERIFIED` until mapped, licensed attribution stored in `PyqSourceMetadata` |
| **Audited Open Educational Repositories** | CC-BY or public domain historical question collections | Marked `UNVERIFIED` pending manual cross-referencing |
| **User/Institutional Paper Submissions** | Verified physical question papers scanned by educators or institutional libraries | Requires administrative human review in the review queue |

---

## 4. Complete Historical Examination Matrix (2005–2025)

| Year | Examination | Administering Body | Paper Structure | Expected Total |
| :---: | :---: | :---: | :---: | :---: |
| **2005** | AIPMT Prelims / Mains | CBSE | 50 Phy / 50 Chem / 100 Bio | 200 |
| **2006** | AIPMT Prelims / Mains | CBSE | 50 Phy / 50 Chem / 100 Bio | 200 |
| **2007** | AIPMT Prelims / Mains | CBSE | 50 Phy / 50 Chem / 100 Bio | 200 |
| **2008** | AIPMT Prelims / Mains | CBSE | 50 Phy / 50 Chem / 100 Bio | 200 |
| **2009** | AIPMT Prelims / Mains | CBSE | 50 Phy / 50 Chem / 100 Bio | 200 |
| **2010** | AIPMT Prelims / Mains | CBSE | 50 Phy / 50 Chem / 100 Bio | 200 |
| **2011** | AIPMT Prelims / Mains | CBSE | 50 Phy / 50 Chem / 100 Bio | 200 |
| **2012** | AIPMT Prelims / Mains | CBSE | 50 Phy / 50 Chem / 100 Bio | 200 |
| **2013** | NEET UG (Inaugural) | CBSE | 45 Phy / 45 Chem / 90 Bio | 180 |
| **2014** | AIPMT | CBSE | 45 Phy / 45 Chem / 90 Bio | 180 |
| **2015** | AIPMT / Re-AIPMT | CBSE | 45 Phy / 45 Chem / 90 Bio | 180 |
| **2016** | NEET UG Phase I & II | CBSE | 45 Phy / 45 Chem / 90 Bio | 180 |
| **2017** | NEET UG | CBSE | 45 Phy / 45 Chem / 90 Bio | 180 |
| **2018** | NEET UG | CBSE | 45 Phy / 45 Chem / 90 Bio | 180 |
| **2019** | NEET UG | NTA | 45 Phy / 45 Chem / 90 Bio | 180 |
| **2020** | NEET UG (Phase 1 & 2) | NTA | 45 Phy / 45 Chem / 90 Bio | 180 |
| **2021** | NEET UG | NTA | 45 Phy / 45 Chem / 90 Bio (Section A/B) | 180 (of 200) |
| **2022** | NEET UG | NTA | 45 Phy / 45 Chem / 90 Bio (Section A/B) | 180 (of 200) |
| **2023** | NEET UG | NTA | 45 Phy / 45 Chem / 90 Bio (Section A/B) | 180 (of 200) |
| **2024** | NEET UG | NTA | 45 Phy / 45 Chem / 90 Bio (Section A/B) | 180 (of 200) |
| **2025** | NEET UG | NTA | 45 Phy / 45 Chem / 90 Bio (Section A/B) | 180 (of 200) |

---

## 5. Mandatory Provenance Schema (`PyqSourceMetadata`)

Every ingested file or submission batch must record:
```json
{
  "sourceType": "OFFICIAL_PUBLIC | LICENSED_DATASET | PUBLIC_DOMAIN | USER_PROVIDED",
  "sourceName": "AIPMT 2008 Official Paper Code A",
  "sourceLocator": "https://cbse.gov.in/archive/aipmt2008_prelims.pdf",
  "sourceDocumentId": "AIPMT-2008-CBSE-ARCHIVE",
  "licenseType": "Government Public Educational Document",
  "acquisitionDate": "2026-09-13",
  "rawChecksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "primaryEvidenceUri": "https://nta.ac.in/archive/keys/2008/code_a_key.pdf"
}
```

---

## 6. Current Reality Statement
**Actual historical PYQ dataset has not yet been imported. The ingestion framework is ready for legitimate source material.**
No synthetic or generated questions may substitute for missing years.
