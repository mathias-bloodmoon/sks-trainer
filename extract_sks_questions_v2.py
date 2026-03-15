import pdfplumber
import re
import json
import string
from collections import Counter

PDF_PATH = "Fragenkatalog-SKS-2006.pdf"
OUTPUT_PATH = "sks-questions-2006.json"

# === OFFIZIELLE KAPITEL-GRÖSSEN (Reihenfolge im PDF) ===
CHAPTERS = [
    ("Navigation", 118),
    ("Schifffahrtsrecht", 110),
    ("Wetterkunde", 101),
    ("Seemannschaft I", 163),
    ("Seemannschaft II", 146),
]

CATEGORY_SHORT = {
    "Navigation": "NAV",
    "Schifffahrtsrecht": "RECHT",
    "Wetterkunde": "WET",
    "Seemannschaft I": "SEEM1",
    "Seemannschaft II": "SEEM2"
}

# Regex (bleibt gleich)
pattern = re.compile(
    r"Nummer\s+(\d+):\s*(.*?)\s*(?=Nummer\s+\d+:|$)",
    re.DOTALL
)

def clean_text(text):
    text = text.replace("\n", " ").replace("  ", " ").strip()
    return text

def extract_question_answer(block_text):
    lines = block_text.strip().split("\n")
    lines = [l.strip() for l in lines if l.strip()]
    if len(lines) == 0:
        return None, None
    question = lines[0]
    answer = " ".join(lines[1:]) if len(lines) > 1 else ""
    return clean_text(question), clean_text(answer)

def extract_keywords(answer, max_keywords=5):
    text = answer.lower().translate(str.maketrans("", "", string.punctuation))
    words = text.split()
    stopwords = {"der", "die", "das", "und", "oder", "mit", "von", "auf", "in", "zu", "den", "dem", "ein", "eine"}
    words = [w for w in words if w not in stopwords and len(w) > 3]
    freq = Counter(words)
    return [w for w, _ in freq.most_common(max_keywords)]

def get_category_by_index(index):
    """Index-basiert (0-basiert) → Kapitel zuweisen"""
    cumul = 0
    for cat_name, size in CHAPTERS:
        if index < cumul + size:
            return cat_name
        cumul += size
    return "Unbekannt"  # falls mehr Fragen als erwartet

def main():
    print("📘 Öffne PDF…")
    with pdfplumber.open(PDF_PATH) as pdf:
        pages = [page.extract_text() for page in pdf.pages]

    full_text = "\n".join(pages)

    print("🔍 Extrahiere Frageblöcke…")
    matches = pattern.findall(full_text)

    results = []

    print("📊 Weise Kapitel zu (index-basiert)…")
    for num, block in matches:
        question, answer = extract_question_answer(block)
        if not question:
            continue

        # Kategorie jetzt 100% korrekt über Position
        category = get_category_by_index(len(results))

        # Subkategorie (erster Satz)
        subcategory = question.split()[0] if question.split() else "Allgemein"

        # ID generieren
        cat_short = CATEGORY_SHORT.get(category, "GEN")
        qid = f"{cat_short}-{int(num):03d}"

        # Keywords
        keywords = extract_keywords(answer)

        results.append({
            "id": qid,
            "number": int(num),
            "category": category,
            "subcategory": subcategory,
            "question": question,
            "answer": answer,
            "keywords": keywords
        })

    print(f"📦 Exportiere {len(results)} Einträge nach JSON…")
    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    # Bonus: Statistik zur Kontrolle
    from collections import Counter
    cat_count = Counter(r["category"] for r in results)
    print("\n✅ Kapitel-Statistik:")
    for cat, count in cat_count.items():
        print(f"   • {cat}: {count} Fragen")

    print(f"\n🎉 Fertig! Datei gespeichert als: {OUTPUT_PATH}")

if __name__ == "__main__":
    main()