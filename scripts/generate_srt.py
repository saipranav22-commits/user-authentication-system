import os
import re

SCRIPT_PATH = os.path.join("docs", "demo", "scripts", "narration.txt")
SRT_PATH = os.path.join("docs", "demo", "final", "jwt-authentication-system-subtitles.srt")
TOTAL_DURATION = 332.35

def format_timestamp(seconds):
    millis = int((seconds - int(seconds)) * 1000)
    secs = int(seconds) % 60
    mins = (int(seconds) // 60) % 60
    hours = int(seconds) // 3600
    return f"{hours:02d}:{mins:02d}:{secs:02d},{millis:03d}"

def generate_srt():
    with open(SCRIPT_PATH, "r", encoding="utf-8") as f:
        content = f.read()

    # Extract scenes
    scenes = re.findall(r"\[(.*?)\]\s*\n(.*?)(?=\n\n\[|\Z)", content, re.DOTALL)
    
    # Break into natural sentence chunks
    all_chunks = []
    for scene_title, scene_text in scenes:
        sentences = re.split(r"(?<=[.!?])\s+", scene_text.strip())
        for sent in sentences:
            sent = sent.strip()
            if not sent:
                continue
            words = sent.split()
            # If sentence is long (>12 words), split into two halves for readable subtitles
            if len(words) > 12:
                mid = len(words) // 2
                all_chunks.append(" ".join(words[:mid]))
                all_chunks.append(" ".join(words[mid:]))
            else:
                all_chunks.append(sent)

    total_words = sum(len(c.split()) for c in all_chunks)
    time_per_word = TOTAL_DURATION / total_words

    current_time = 0.5  # start at 0.5s
    srt_lines = []

    for idx, chunk in enumerate(all_chunks, 1):
        num_words = len(chunk.split())
        dur = num_words * time_per_word
        start_t = current_time
        end_t = min(current_time + dur, TOTAL_DURATION - 0.2)

        srt_lines.append(f"{idx}\n{format_timestamp(start_t)} --> {format_timestamp(end_t)}\n{chunk}\n")
        current_time = end_t + 0.15

    with open(SRT_PATH, "w", encoding="utf-8") as f:
        f.write("\n".join(srt_lines) + "\n")

    print(f"Generated {len(all_chunks)} subtitle cues in {SRT_PATH}")

if __name__ == "__main__":
    generate_srt()
