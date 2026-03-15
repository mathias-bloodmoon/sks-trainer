import pdfplumber
import re
import json
import string
from collections import Counter

PDF_PATH = "Fragenkatalog-SKS-2006.pdf"
OUTPUT_PATH = "sks-questions-2006.json"

# Categories and its Recognition markers
CATEGORY_MAP = {
    "Navigation": ["Navigation"],
    "Schifffahrtsrecht": ["Schifffahrtsrecht", "KVR", "SeeSchStrO"],
    "Wetterkunde": ["Wetterkunde"],
    "Seemannschaft I": ["Seemannschaft I"],
    "Seemannschaft II": ["Seemannschaft II"]
}

# Acronyms for ID-Creation
CATEGORY_SHORT = {
    "Navigation": "NAV",
    "Schifffahrtsrecht": "RECHT",
    "Wetterkunde": "WET",
    "Seemannschaft I": "SEEM1",
    "Seemannschaft II": "SEEM2"
}

# Regex to identify question blocks
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

def detect_category(page_text):
    for category, markers in CATEGORY_MAP.items():
        for m in markers:
            if m.lower() in page_text.lower():
                return category
    return "Unknown"

def extract_keywords(answer, max_keywords=5):
    # Remove punctuation marks
    text = answer.lower().translate(str.maketrans("", "", string.punctuation))
    words = text.split()

    # Delete stopwords
    stopwords = {"der", "die", "das", "und", "oder", "mit", "von", "auf", "in", "zu", "den", "dem", "ein", "eine"}
    words = [w for w in words if w not in stopwords and len(w) > 3]

    # Common words as keywords
    freq = Counter(words)
    return [w for w, _ in freq.most_common(max_keywords)]

def main():
    print("?? Open PDF")
    with pdfplumber.open(PDF_PATH) as pdf:
        pages = [page.extract_text() for page in pdf.pages]

    full_text = "\n".join(pages)

    print("?? Extracting question blocks")
    matches = pattern.findall(full_text)

    results = []
    current_category = "Unknown"
    subcategory = None

    for page_text in pages:
        detected = detect_category(page_text)
        if detected != "Unknown":
            current_category = detected

    for num, block in matches:
        question, answer = extract_question_answer(block)
        if not question:
            continue

        # Subcategory heuristic: first sentence of question
        subcategory = question.split()[0]

        # Generate ID
        cat_short = CATEGORY_SHORT.get(current_category, "GEN")
        qid = f"{cat_short}-{int(num):03d}"

        # Create keywords
        keywords = extract_keywords(answer)

        results.append({
            "id": qid,
            "number": int(num),
            "category": current_category,
            "subcategory": subcategory,
            "question": question,
            "answer": answer,
            "keywords": keywords
        })

    print(f"?? Export {len(results)} entries to JSON")
    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    print(f"? Completed! File saved as: {OUTPUT_PATH}")

if __name__ == "__main__":
    main()
