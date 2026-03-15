import fitz  # PyMuPDF – für Text + Bold-Erkennung + Bilder
import re
import json
import string
import os
from collections import Counter

PDF_PATH = "Fragenkatalog-SKS.pdf"
OUTPUT_JSON = "sks.json"
PICTURES_DIR = "pictures"

# Offizielle Kapitel (wie bisher)
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


def clean_text(text: str) -> str:
    if not text:
        return ""
    return " ".join(text.split())


def extract_keywords(text: str, max_k: int = 5) -> list[str]:
    if not text:
        return []
    text = text.lower().translate(str.maketrans("", "", string.punctuation))
    words = [w for w in text.split() if len(w) > 3]
    stopwords = {"der", "die", "das", "und", "oder", "mit", "von", "auf", "in", "zu", "den", "dem", "ein", "eine", "ist", "sind", "als", "bei", "nach", "für"}
    words = [w for w in words if w not in stopwords]
    if not words:
        return []
    return [w for w, _ in Counter(words).most_common(max_k)]


def get_category(idx: int) -> str:
    cumul = 0
    for cat_name, size in CHAPTERS:
        if idx < cumul + size:
            return cat_name
        cumul += size
    return "Unbekannt"


def main():
    os.makedirs(PICTURES_DIR, exist_ok=True)
    results = []
    question_index = 0

    print("📘 PDF wird mit PyMuPDF geladen (Bold-Erkennung + Bilder)...")

    all_spans = []
    all_images = []

    with fitz.open(PDF_PATH) as doc:
        for page_num, page in enumerate(doc):
            # === Alle Text-Spans mit Bold-Flag holen ===
            text_dict = page.get_text("dict")
            spans = []
            for block in text_dict.get("blocks", []):
                for line in block.get("lines", []):
                    for span in line.get("spans", []):
                        span_data = {
                            "text": span["text"].strip(),
                            "x0": span["bbox"][0],
                            "y0": span["bbox"][1],      # obere Kante
                            "y1": span["bbox"][3],      # untere Kante
                            "is_bold": bool(span["flags"] & 16),  # Bit 4 = bold
                            "page_num": page_num,
                            "y_global": page_num * 10000 + span["bbox"][1]
                        }
                        spans.append(span_data)
                        all_spans.append(span_data)

            # === Bilder dieser Seite sammeln ===
            images = page.get_images(full=True)
            for img_idx, img_info in enumerate(images):
                xref = img_info[0]
                base = doc.extract_image(xref)
                if not base:
                    continue

                # Position des Bildes holen
                rects = page.get_image_rects(xref)
                if not rects:
                    continue
                img_rect = rects[0]
                img_y_global = page_num * 10000 + img_rect.y0

                all_images.append({
                    "xref": xref,
                    "base": base,
                    "img_y_global": img_y_global,
                    "page_num": page_num,
                    "img_idx": img_idx,
                    "ext": base.get("ext", "png")
                })

    # === Alle Spans sammeln (in PDF-Reihenfolge) ===
    all_spans = []
    all_images = []

    with fitz.open(PDF_PATH) as doc:
        for page_num, page in enumerate(doc):
            # === Alle Text-Spans mit Bold-Flag holen ===
            text_dict = page.get_text("dict")
            spans = []
            for block in text_dict.get("blocks", []):
                for line in block.get("lines", []):
                    for span in line.get("spans", []):
                        span_data = {
                            "text": span["text"].strip(),
                            "x0": span["bbox"][0],
                            "y0": span["bbox"][1],      # obere Kante
                            "y1": span["bbox"][3],      # untere Kante
                            "is_bold": bool(span["flags"] & 16),  # Bit 4 = bold
                            "page_num": page_num,
                            "y_global": page_num * 10000 + span["bbox"][1]
                        }
                        spans.append(span_data)
                        all_spans.append(span_data)

            # === Bilder dieser Seite sammeln ===
            images = page.get_images(full=True)
            for img_idx, img_info in enumerate(images):
                xref = img_info[0]
                base = doc.extract_image(xref)
                if not base:
                    continue

                # Position des Bildes holen
                rects = page.get_image_rects(xref)
                if not rects:
                    continue
                img_rect = rects[0]
                img_y_global = page_num * 10000 + img_rect.y0

                all_images.append({
                    "xref": xref,
                    "base": base,
                    "img_y_global": img_y_global,
                    "page_num": page_num,
                    "img_idx": img_idx,
                    "ext": base.get("ext", "png")
                })

    # === Parsing ohne Sortierung (in PDF-Reihenfolge) ===
    results = []
    question_index = 0
    i = 0
    while i < len(all_spans):
        span = all_spans[i]
        if re.search(r"Nummer\s+\d+", span["text"]):
            match = re.search(r"Nummer\s+(\d+)", span["text"])
            if not match:
                i += 1
                continue

            q_num = int(match.group(1))
            cat = get_category(question_index)
            short = CATEGORY_SHORT.get(cat, "GEN")
            qid = f"{short}-{q_num:03d}"

            q_text_parts = []
            q_y_min = None
            q_y_max = None

            a_text_parts = []
            a_y_min = None
            a_y_max = None

            i += 1  # skip the "Nummer" span
            # Sammle bis zur nächsten "Nummer"
            while i < len(all_spans):
                next_span = all_spans[i]
                if re.search(r"Nummer\s+\d+", next_span["text"]):
                    break  # nächste Frage beginnt

                if next_span["is_bold"]:
                    q_text_parts.append(next_span["text"])
                    if q_y_min is None:
                        q_y_min = next_span["y_global"]
                    q_y_max = max(q_y_max or next_span["y_global"] + (next_span["y1"] - next_span["y0"]), next_span["y_global"] + (next_span["y1"] - next_span["y0"]))
                else:
                    a_text_parts.append(next_span["text"])
                    if a_y_min is None:
                        a_y_min = next_span["y_global"]
                    a_y_max = max(a_y_max or next_span["y_global"] + (next_span["y1"] - next_span["y0"]), next_span["y_global"] + (next_span["y1"] - next_span["y0"]))
                i += 1

            question = clean_text(" ".join(q_text_parts))
            answer = clean_text(" ".join(a_text_parts))
            answer = answer.replace(" - ", " ")  # Remove hyphenation dashes

            if not question:
                continue

            question_data = {
                "id": qid,
                "number": q_num,
                "category": cat,
                "subcategory": question.split(maxsplit=1)[0] if question else "Allgemein",
                "question": question,
                "answer": answer,
                "keywords": extract_keywords(answer),
                "question_image": None,
                "answer_image": None,
                "q_y_min": q_y_min,
                "q_y_max": q_y_max,
                "a_y_min": a_y_min,
                "a_y_max": a_y_max
            }

            # === Bilder zuordnen ===
            for img in all_images:
                img_y = img["img_y_global"]
                typ = None
                dist = float('inf')

                # Bild unter fett gedruckter Frage?
                if question_data["q_y_min"] is not None and question_data["q_y_min"] - 80 <= img_y <= question_data["q_y_max"] + 150:
                    d = abs(img_y - question_data["q_y_max"])
                    if d < dist:
                        dist = d
                        typ = "question"

                # Bild in normaler Antwort?
                if question_data["a_y_min"] is not None and question_data["a_y_min"] - 80 <= img_y <= (question_data["a_y_max"] or img_y + 300):
                    d = abs(img_y - question_data["a_y_min"])
                    if d < dist:
                        dist = d
                        typ = "answer"

                if typ and dist < 400:  # Toleranz-Schwelle
                    filename = f"img_{question_data['id']}_{typ}_p{img['page_num']+1:03d}_{img['img_idx']}.{img['ext']}"
                    path = os.path.join(PICTURES_DIR, filename)

                    with open(path, "wb") as f:
                        f.write(img["base"]["image"])

                    if typ == "question":
                        question_data["question_image"] = f"pictures/{filename}"
                    else:
                        question_data["answer_image"] = f"pictures/{filename}"

            results.append(question_data)
            question_index += 1
            continue
        i += 1

    # === JSON speichern ===
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    # === Statistik ===
    assigned_q = sum(1 for r in results if r.get("question_image"))
    assigned_a = sum(1 for r in results if r.get("answer_image"))
    total = len(results)

    print(f"\n✅ FERTIG – Bold-Erkennung aktiv!")
    print(f"   Fragen gesamt:          {total}")
    print(f"   Mit Question-Image:     {assigned_q}")
    print(f"   Mit Answer-Image:       {assigned_a}")
    print(f"   Bilder-Ordner:          ./{PICTURES_DIR}/")
    print("\nJSON-Felder:")
    print("   • question_image → Bild unter fett gedruckter Frage")
    print("   • answer_image   → Bild in normaler Antwort (falls vorhanden)")
    print("\nDeine Vibe-App kann jetzt einfach `question.question_image` und `question.answer_image` verwenden!")
    print("   Tipp: In der Flashcard das Frage-Bild immer anzeigen, das Antwort-Bild optional beim Umblättern.")


if __name__ == "__main__":
    main()