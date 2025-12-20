import subprocess
import traceback
from pathlib import Path

from scripts.logger import logger
from scripts.llm import get_llm_provider
from scripts.error_parser import parse_build_errors, extract_code_snippet
from scripts.patch_applier import apply_patch, is_structurally_corrupt
from scripts.git_utils import commit_changes
from scripts.github import create_branch, push_branch

MAX_RETRIES = 3
DEBUG_DIR = Path(".ai_debug")
CONTEXT_DIR = Path(".ai_context")


# --------------------------------------------------
# Context builders
# --------------------------------------------------

def build_file_tree() -> str:
    result = subprocess.run(
        ["bash", "-lc", "find android -type f | sort"],
        capture_output=True,
        text=True,
    )
    return result.stdout.strip()


def read_file_safe(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8", errors="ignore")
    except Exception:
        return ""


def prepare_context():
    CONTEXT_DIR.mkdir(exist_ok=True)

    (CONTEXT_DIR / "file_tree.txt").write_text(build_file_tree())
    (CONTEXT_DIR / "AndroidManifest.xml").write_text(
        read_file_safe(Path("android/app/src/main/AndroidManifest.xml"))
    )
    (CONTEXT_DIR / "app_build.gradle").write_text(
        read_file_safe(Path("android/app/build.gradle"))
    )


# --------------------------------------------------
# Build helpers
# --------------------------------------------------

def read_build_log():
    try:
        output = Path("build.log").read_text(encoding="utf-8", errors="ignore")
        return "BUILD FAILED" not in output, output
    except FileNotFoundError:
        return False, ""


# --------------------------------------------------
# Autofix attempt
# --------------------------------------------------

def run_autofix_attempt(attempt: int) -> bool:
    logger.info(f"🔁 Autofix attempt {attempt}/{MAX_RETRIES}")

    build_ok, build_log = read_build_log()
    if build_ok:
        logger.info("✅ Build already successful")
        return True

    errors = parse_build_errors(build_log)
    logger.info(f"📋 Parsed {len(errors)} build errors")

    if not errors:
        return False

    error = errors[0]
    file_path = error.get("file")
    line = error.get("line")
    message = error.get("message")

    if not file_path or not line:
        return False

    full_file = read_file_safe(Path(file_path))
    snippet = extract_code_snippet(file_path, line, 12)

    # 🚨 Structural corruption detection BEFORE LLM
    if is_structurally_corrupt(full_file):
        logger.error("🚨 Structural corruption detected — aborting autofix")
        DEBUG_DIR.mkdir(exist_ok=True)
        (DEBUG_DIR / "structural_failure.txt").write_text(
            f"File: {file_path}\n\n{full_file}"
        )

        create_branch("ai-autofix/structural-failure")
        commit_changes(
            "debug: structural corruption detected",
            [str(DEBUG_DIR / "structural_failure.txt")]
        )
        push_branch("ai-autofix/structural-failure")
        return True  # STOP further attempts safely

    prepare_context()

    prompt = f"""SYSTEM:
You are an automated build-fixing agent.

RULES (MANDATORY):
- Output ONLY a valid unified git diff.
- Do NOT include explanations, markdown, comments, or code fences.
- Do NOT repeat build errors.
- Every change MUST be in diff format.
- If unsure, still output a best-effort diff.

The output MUST start with:
diff --git

Example (format only):
diff --git a/app/src/main/kotlin/Example.kt b/app/src/main/kotlin/Example.kt
index 1111111..2222222 100644
--- a/app/src/main/kotlin/Example.kt
+++ b/app/src/main/kotlin/Example.kt
@@ -1,3 +1,4 @@
 package example
+// fix
"""

=== BUILD ERROR ===
File: {file_path}
Line: {line}
Error: {message}

=== CODE SNIPPET ===
{snippet.get('code') if snippet else ''}

=== FULL FILE ===
{full_file}

=== PROJECT FILE TREE ===
{read_file_safe(CONTEXT_DIR / "file_tree.txt")}

=== ANDROID MANIFEST ===
{read_file_safe(CONTEXT_DIR / "AndroidManifest.xml")}

=== MODULE GRADLE ===
{read_file_safe(CONTEXT_DIR / "app_build.gradle")}
"""

    provider = get_llm_provider()
    response = provider.ask(prompt)

    DEBUG_DIR.mkdir(exist_ok=True)
    debug_file = DEBUG_DIR / f"attempt_{attempt}.txt"
    debug_file.write_text(response)

    patched = apply_patch(response, build_log)
    if not patched:
        logger.warning("⚠️ Patch failed — committing AI output")

        create_branch("ai-autofix/debug-output")
        commit_changes(
            f"debug: AI output attempt {attempt}",
            [str(debug_file)]
        )
        push_branch("ai-autofix/debug-output")
        return False

    subprocess.run(["./gradlew", "build"], check=False)
    ok, _ = read_build_log()

    if not ok:
        subprocess.run(["git", "checkout", "--"] + patched)
        return False

    create_branch("ai-autofix/build-fix")
    commit_changes("fix: AI autofix build error", patched)
    push_branch("ai-autofix/build-fix")

    logger.info("🎉 Autofix successful")
    return True


# --------------------------------------------------
# Main
# --------------------------------------------------

def main():
    logger.info("🚀 AI Autofix starting")

    try:
        for i in range(1, MAX_RETRIES + 1):
            if run_autofix_attempt(i):
                return
        logger.error("❌ Autofix failed after max retries")
    except Exception as e:
        logger.error(f"💥 Autofix crashed: {e}")
        traceback.print_exc()


if __name__ == "__main__":
    main()
