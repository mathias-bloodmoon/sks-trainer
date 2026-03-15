import pdfplumber
import re
import json
import string
import os
from collections import Counter, defaultdict
import fitz  # PyMuPDF für Bilder

PDF_PATH = "Fragenkatalog-SKS.pdf"
OUTPUT_JSON = "sks.json"
PICTURES_DIR = "pictures"

# Offizielle Kapitel-Reihenfolge + Anzahl Fragen
CHAPTERS = [
    ("Navigation",         118),
    ("Schifffahrtsrecht",  110),
    ("Wetterkunde",        101),
    ("Seemannschaft I",    163),
    ("Seemannschaft II",   146),
]

CATEGORY_SHORT = {
    "Navigation":        "NAV",
    "Schifffahrtsrecht": "RECHT",
    "Wetterkunde":       "WET",
    "Seemannschaft I":   "SEEM1",
    "Seemannschaft II":  "SEEM2"
}

# Regex für Frage-Blöcke
pattern = re.compile(
    r"Nummer\s+(\d+):\s*(.*?)\s*(?=Nummer\s+\d+:|$)",
    re.DOTALL | re.IGNORECASE
)


def clean_text(text: str) -> str:
    if not text:
        return ""
    return " ".join(text.split())


def simple_extract_qa(block: str) -> tuple[str, str]:
    """Heuristik: Frage bis zum ersten Antwort-Indikator"""
    lines = [line.strip() for line in block.splitlines() if line.strip()]
    if not lines:
        return "", ""

    question_lines = []
    answer_lines = []
    collecting_answer = False

    for line in lines:
        lower = line.lower()
        if any(x in lower for x in ["antwort:", "lösung:", "richtig ist", "die richtige antwort", "korrekt ist"]):
            collecting_answer = True
        if collecting_answer:
            answer_lines.append(line)
        else:
            question_lines.append(line)

    q = clean_text(" ".join(question_lines))
    a = clean_text(" ".join(answer_lines))

    # Fallback: wenn keine klare Trennung → Hälfte nehmen
    if not a and len(lines) > 4:
        split_idx = len(lines) // 2 + 1
        q = clean_text(" ".join(lines[:split_idx]))
        a = clean_text(" ".join(lines[split_idx:]))

    return q, a


def extract_keywords(text: str, max_k: int = 5) -> list[str]:
    if not text:
        return []
    text = text.lower().translate(str.maketrans("", "", string.punctuation))
    words = [w for w in text.split() if len(w) > 3]
    stopwords = {"der", "die", "das", "und", "oder", "mit", "von", "auf", "in", "zu", "den", "dem", "ein", "eine", "ist", "sind", "als", "bei", "nach", "zu", "für"}
    words = [w for w in words if w not in stopwords]
    if not words:
        return []
    return [w for w, _ in Counter(words).most_common(max_k)]


def get_category(idx: int) -> str:
    """Index-basiert Kategorie zuweisen"""
    cumul = 0
    for cat_name, size in CHAPTERS:
        if idx < cumul + size:
            return cat_name
        cumul += size
    return "Unbekannt"


def main():
    os.makedirs(PICTURES_DIR, exist_ok=True)

    print("📘 PDF wird geladen (pdfplumber + PyMuPDF)...")

    results = []
    question_index = 0
    page_question_map = {}          # page_num → Liste der qids auf dieser Seite

    with pdfplumber.open(PDF_PATH) as pdf:
        with fitz.open(PDF_PATH) as doc:

            for page_num, plumber_page in enumerate(pdf.pages):
                page_text = plumber_page.extract_text() or ""
                matches_on_page = pattern.findall(page_text)

                qids_on_page = []

                for num_str, block in matches_on_page:
                    q_num = int(num_str)
                    cat = get_category(question_index)
                    short = CATEGORY_SHORT.get(cat, "XXX")
                    qid = f"{short}-{q_num:03d}"

                    question, answer = simple_extract_qa(block)
                    if not question:
                        continue

                    subcategory = question.split(maxsplit=1)[0] if question.split() else "Allgemein"

                    results.append({
                        "id":          qid,
                        "number":      q_num,
                        "category":    cat,
                        "subcategory": subcategory,
                        "question":    question,
                        "answer":      answer,
                        "keywords":    extract_keywords(answer),
                        "image":       None          # wird automatisch gefüllt
                    })

                    qids_on_page.append(qid)
                    question_index += 1

                if qids_on_page:
                    page_question_map[page_num] = qids_on_page

                # === BILDER DIESER SEITE AUTOMATISCH ZUORDNEN ===
                page = doc[page_num]
                images = page.get_images(full=True)

                for img_idx, img in enumerate(images):
                    xref = img[0]
                    base = doc.extract_image(xref)
                    if not base:
                        continue

                    ext = base.get("ext", "png")

                    # Automatische Zuordnung:
                    if len(qids_on_page) == 1:
                        used_qid = qids_on_page[0]          # eindeutig → perfekt
                        image_field = f"pictures/img_{used_qid}_p{page_num+1:03d}_{img_idx}.{ext}"
                    elif len(qids_on_page) > 1:
                        used_qid = qids_on_page[-1]         # Fallback: letzte Frage der Seite
                        image_field = f"pictures/img_{used_qid}_p{page_num+1:03d}_{img_idx}.{ext}"
                    else:
                        used_qid = "unknown"
                        image_field = f"pictures/img_unknown_p{page_num+1:03d}_{img_idx}.{ext}"

                    # Datei speichern
                    filename = f"img_{used_qid}_p{page_num+1:03d}_{img_idx}.{ext}"
                    path = os.path.join(PICTURES_DIR, filename)
                    with open(path, "wb") as f:
                        f.write(base["image"])

                    # JSON automatisch füllen (nur bei eindeutiger Zuordnung)
                    if len(qids_on_page) == 1:
                        for entry in results:
                            if entry["id"] == used_qid:
                                entry["image"] = image_field
                                break

    # === JSON SPEICHERN ===
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    # === STATISTIK ===
    cat_stats = Counter(r["category"] for r in results)
    image_count = sum(1 for r in results if r["image"] is not None)

    print("\n✅ FERTIG!")
    print(f"   Fragen verarbeitet : {len(results)}")
    print(f"   Bilder extrahiert  : {sum(len(v) for v in page_question_map.values())} Seiten mit Bildern")
    print(f"   Automatisch zugeordnet in JSON : {image_count} Fragen")
    print("\nKapitel-Statistik:")
    for cat, cnt in sorted(cat_stats.items()):
        print(f"   • {cat:18} → {cnt:3d} Fragen")

    print(f"\n📁 Dateien:")
    print(f"   • sks.json")
    print(f"   • pictures/ (Bilder mit Frage-ID im Namen, z. B. img_RECHT-021_p023_0.png)")
    print("\n🚀 Deine Flashcard-App kann jetzt direkt `question.image` verwenden!")


if __name__ == "__main__":
    main()