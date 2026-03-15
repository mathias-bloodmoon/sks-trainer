import pdfplumber
import re
import json
import string
import os
from collections import Counter
import fitz  # PyMuPDF für Bilder

PDF_PATH = "Fragenkatalog-SKS.pdf"
OUTPUT_PATH = "sks.json"
PICTURES_DIR = "pictures"

# === OFFIZIELLE KAPITEL (wie zuvor) ===
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

pattern = re.compile(r"Nummer\s+(\d+):\s*(.*?)\s*(?=Nummer\s+\d+:|$)", re.DOTALL)

def clean_text(text):
    return text.replace("\n", " ").replace("  ", " ").strip()

def extract_question_answer(block_text):
    """Verbesserte Version: Aufzählungen (1., 2., a), b) etc.) gehören jetzt zur FRAGE"""
    lines = [l.strip() for l in block_text.strip().split("\n") if l.strip()]
    if not lines:
        return None, None

    question_lines = [lines[0]]
    answer_lines = []
    i = 1

    while i < len(lines):
        line = lines[i]
        # Aufzählung → gehört zur Frage (z. B. Multiple-Choice-Optionen oder Beispiele in der Frage)
        if re.match(r'^\d+\.', line) or re.match(r'^[a-d]\)', line, re.IGNORECASE):
            question_lines.append(line)
        # Wenn wir "Antwort", "Lösung", "richtig" etc. sehen → Rest ist Antwort
        elif any(word in line.lower() for word in ["antwort", "lösung", "richtig", "korrekt", "die richtige"]):
            answer_lines.append(line)
            answer_lines.extend(lines[i+1:])
            break
        else:
            question_lines.append(line)
        i += 1

    question = clean_text(" ".join(question_lines))
    answer = clean_text(" ".join(answer_lines)) if answer_lines else ""
    return question, answer

def extract_keywords(answer, max_keywords=5):
    text = answer.lower().translate(str.maketrans("", "", string.punctuation))
    words = text.split()
    stopwords = {"der", "die", "das", "und", "oder", "mit", "von", "auf", "in", "zu", "den", "dem", "ein", "eine"}
    words = [w for w in words if w not in stopwords and len(w) > 3]
    freq = Counter(words)
    return [w for w, _ in freq.most_common(max_keywords)]

def get_category_by_index(index):
    cumul = 0
    for cat_name, size in CHAPTERS:
        if index < cumul + size:
            return cat_name
        cumul += size
    return "Unbekannt"

def main():
    print("📘 Öffne PDF…")
    with pdfplumber.open(PDF_PATH) as pdf:
        pages = [page.extract_text() for page in pdf.pages]

    full_text = "\n".join(pages)
    matches = pattern.findall(full_text)

    results = []
    print("🔍 Extrahiere Fragen + korrigiere Aufzählungen…")
    for num, block in matches:
        question, answer = extract_question_answer(block)
        if not question:
            continue

        category = get_category_by_index(len(results))
        subcategory = question.split()[0] if question.split() else "Allgemein"
        cat_short = CATEGORY_SHORT.get(category, "GEN")
        qid = f"{cat_short}-{int(num):03d}"

        keywords = extract_keywords(answer)

        results.append({
            "id": qid,
            "number": int(num),
            "category": category,
            "subcategory": subcategory,
            "question": question,
            "answer": answer,
            "keywords": keywords,
            "image": None   # ← hier später Pfad eintragen, z. B. "pictures/image_page23_img0.png"
        })

    # === BILDER EXTRAKTIEREN ===
    os.makedirs(PICTURES_DIR, exist_ok=True)
    print("🖼️ Extrahiere alle Bilder aus dem PDF…")
    doc = fitz.open(PDF_PATH)
    image_count = 0
    for page_num in range(len(doc)):
        page = doc[page_num]
        image_list = page.get_images(full=True)
        for img_index, img in enumerate(image_list):
            xref = img[0]
            base_image = doc.extract_image(xref)
            image_bytes = base_image["image"]
            ext = base_image["ext"]
            filename = f"image_page{page_num+1}_img{img_index}.{ext}"
            path = os.path.join(PICTURES_DIR, filename)
            with open(path, "wb") as f:
                f.write(image_bytes)
            image_count += 1
    doc.close()

    print(f"📦 {len(results)} Fragen + {image_count} Bilder gespeichert!")

    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    # Statistik
    from collections import Counter
    cat_count = Counter(r["category"] for r in results)
    print("\n✅ Kapitel-Statistik:")
    for cat, count in cat_count.items():
        print(f"   • {cat}: {count} Fragen")

    print(f"\n🎉 Fertig! sks.json + Ordner 'pictures/' sind bereit.")
    print("   → Bei Fragen mit Bild (z. B. RECHT-021) einfach in sks.json den 'image'-Pfad eintragen.")

if __name__ == "__main__":
    main()