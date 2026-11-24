import os
import subprocess
import re
import imageio_ffmpeg

FFMPEG_EXE = imageio_ffmpeg.get_ffmpeg_exe()
RAW_VIDEO = os.path.join("docs", "demo", "recordings", "raw_demo.webm")
AUDIO_FILE = os.path.join("docs", "demo", "voiceover", "narration.wav")
SRT_FILE = os.path.join("docs", "demo", "final", "jwt-authentication-system-subtitles.srt")
FINAL_MP4 = os.path.join("docs", "demo", "final", "jwt-authentication-system-demo.mp4")

def get_duration(file_path):
    cmd = [FFMPEG_EXE, "-i", file_path]
    res = subprocess.run(cmd, stderr=subprocess.PIPE, text=True)
    match = re.search(r"Duration:\s*(\d+):(\d+):(\d+\.\d+)", res.stderr)
    if match:
        h, m, s = match.groups()
        return float(h) * 3600 + float(m) * 60 + float(s)
    return 0.0

def assemble():
    if not os.path.exists(RAW_VIDEO):
        raise FileNotFoundError(f"Raw video {RAW_VIDEO} not found!")
    if not os.path.exists(AUDIO_FILE):
        raise FileNotFoundError(f"Audio file {AUDIO_FILE} not found!")

    video_dur = get_duration(RAW_VIDEO)
    audio_dur = get_duration(AUDIO_FILE)
    print(f"Raw Video Duration: {video_dur:.2f}s | Audio Duration: {audio_dur:.2f}s")

    speed_factor = video_dur / audio_dur
    print(f"Aligning video to audio (scale factor: {speed_factor:.4f})...")

    # Command: encode 1080p 30fps H.264 + AAC audio matching audio duration
    cmd = [
        FFMPEG_EXE, "-y",
        "-i", RAW_VIDEO,
        "-i", AUDIO_FILE,
        "-filter_complex",
        f"[0:v]setpts={1.0 / speed_factor:.6f}*PTS,scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2,fps=30[v]",
        "-map", "[v]",
        "-map", "1:a",
        "-c:v", "libx264",
        "-preset", "fast",
        "-crf", "22",
        "-pix_fmt", "yuv420p",
        "-c:a", "aac",
        "-b:a", "192k",
        "-t", f"{audio_dur:.2f}",
        FINAL_MP4
    ]

    print("Running FFmpeg video assembly...")
    subprocess.run(cmd, check=True)

    final_dur = get_duration(FINAL_MP4)
    file_size_mb = os.path.getsize(FINAL_MP4) / (1024 * 1024)
    print(f"Final MP4 assembled: {FINAL_MP4}")
    print(f"Resolution: 1920x1080 | Duration: {final_dur:.2f}s ({final_dur/60:.2f} mins) | Size: {file_size_mb:.2f} MB")

if __name__ == "__main__":
    assemble()
