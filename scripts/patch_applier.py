import os
import re
import subprocess
import tempfile
from typing import List


# --------------------------------------------------
# Structural corruption detector
# --------------------------------------------------

def is_structurally_corrupt(content: str) -> bool:
    return (
        content.count("class ") > 3
        or "override fun onCreate" in content and "class" not in content
        or re.search(r"^\s*override\s+fun", content, re.MULTILINE)
        or content.count("package ") > 1
    )


# --------------------------------------------------
# Helpers
# --------------------------------------------------

def run(cmd: List[str]):
    p = subprocess.run(cmd, capture_output=True, text=True)
    return p.returncode, p.stdout + p.stderr


def write_file(path: str, content: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)


# --------------------------------------------------
# Diff handling
# --------------------------------------------------

def extract_diff(text: str) -> str:
    lines = []
    in_diff = False
    for line in text.splitlines():
        if line.startswith("diff --git"):
            in_diff = True
        if in_diff:
            lines.append(line)
    return "\n".join(lines).strip()


def apply_diff(diff: str) -> List[str]:
    with tempfile.NamedTemporaryFile("w+", delete=False) as tf:
        tf.write(diff)
        name = tf.name

    try:
        code, _ = run(["git", "apply", "--check", name])
        if code != 0:
            return []

        run(["git", "apply", name])
        _, out = run(["git", "diff", "--name-only"])
        return out.splitlines()
    finally:
        os.unlink(name)


# --------------------------------------------------
# Overwrite fallback (SAFE)
# --------------------------------------------------

def extract_code(text: str) -> str:
    fence = re.search(r"```(?:kotlin|java|groovy)?\s*(.*?)```", text, re.DOTALL)
    return fence.group(1).strip() if fence else text.strip()


def looks_like_code(content: str) -> bool:
    return (
        ("class " in content or "fun " in content or "dependencies {" in content)
        and len(content.splitlines()) > 20
    )


# --------------------------------------------------
# Main entry
# --------------------------------------------------

def apply_patch(ai_response: str, build_log: str) -> List[str]:
    diff = extract_diff(ai_response)
    if diff:
        files = apply_diff(diff)
        if files:
            return files

    content = extract_code(ai_response)
    if not looks_like_code(content):
        return []

    match = re.search(r"File:\s*(.*)", build_log)
    if not match:
        return []

    target = match.group(1)
    if not os.path.isfile(target):
        return []

    write_file(target, content)
    return [target]
