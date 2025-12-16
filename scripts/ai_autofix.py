import subprocess
import sys
import os
from pathlib import Path
from logger import logger
from llm import get_llm_provider
from git_utils import commit_and_push

BUILD_CMD = ["./gradlew", "build"]
MAX_FIX_ATTEMPTS = 3

def run_build():
    logger.info("🔨 Running build...")
    p = subprocess.run(
        BUILD_CMD,
        capture_output=True,
        text=True
    )
    return p.returncode == 0, p.stdout + "\n" + p.stderr


def build_prompt(logs: str):
    return f"""
Build failed.

You are an Android build debugger.
Analyze the error and output ONLY a unified git diff.
Do NOT explain anything.

BUILD LOGS:
{logs}
"""


def apply_patch(diff_text: str):
    if "--- a/" not in diff_text or "+++ b/" not in diff_text:
        raise RuntimeError("Invalid diff from LLM")

    p = subprocess.run(
        ["git", "apply", "-"],
        input=diff_text,
        text=True
    )
    if p.returncode != 0:
        raise RuntimeError("Failed to apply patch")


def get_changed_files():
    p = subprocess.run(
        ["git", "diff", "--name-only"],
        capture_output=True,
        text=True
    )
    return [f for f in p.stdout.splitlines() if f.strip()]


def main():
    for attempt in range(1, MAX_FIX_ATTEMPTS + 1):
        success, logs = run_build()
        if success:
            logger.info("✅ Build successful")
            return

        logger.warning(f"❌ Build failed (attempt {attempt})")

        provider = get_llm_provider()
        prompt = build_prompt(logs)

        diff = provider.ask(prompt)
        apply_patch(diff)

        files = get_changed_files()
        if files:
            commit_and_push(
                message=f"AI autofix attempt {attempt}",
                files=files
            )

    logger.error("🛑 Max attempts reached. Manual fix required.")
    sys.exit(1)


if __name__ == "__main__":
    main()
