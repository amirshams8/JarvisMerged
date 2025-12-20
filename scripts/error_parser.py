import re
import os
from scripts.logger import logger


def parse_build_errors(build_output: str) -> list[dict]:
    """
    Parse Gradle/Kotlin build output.
    Returns list of {file, line, message}
    """
    errors = []

    patterns = [
        # Kotlin compiler (Gradle 7.x common)
        r"e:\s*(file://)?(.+?\.(kt|java)):(\d+):\s*(.+)",
        # Java style
        r"(.+?\.(kt|java|xml)):(\d+):\s*error:\s*(.+)",
        # XML/AAPT
        r"(.+?\.xml):(\d+):\s*(.+error.+)",
    ]

    for line in build_output.splitlines():
        for pattern in patterns:
            match = re.search(pattern, line)
            if not match:
                continue

            groups = match.groups()

            # Normalize groups
            if "file://" in line:
                file_path = groups[1]
                line_num = int(groups[3])
                message = groups[4]
            else:
                file_path = groups[0]
                line_num = int(groups[2])
                message = groups[3]

            file_path = file_path.replace("file://", "").strip()

            if os.path.exists(file_path):
                errors.append({
                    "file": file_path,
                    "line": line_num,
                    "message": message.strip()
                })
                logger.debug(f"Parsed error: {file_path}:{line_num}")
            break

    # Deduplicate
    seen = set()
    unique = []
    for e in errors:
        key = (e["file"], e["line"], e["message"])
        if key not in seen:
            seen.add(key)
            unique.append(e)

    logger.info(f"📋 Parsed {len(unique)} build errors")
    return unique


def extract_code_snippet(
    file_path: str,
    error_line: int,
    context_lines: int = 15
) -> dict | None:
    """
    BEST-EFFORT snippet extraction.
    NEVER throws. Returns None if anything fails.
    """
    try:
        if not os.path.exists(file_path):
            return None

        with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
            lines = f.readlines()

        total = len(lines)
        if error_line < 1 or error_line > total:
            return None

        start = max(1, error_line - context_lines)
        end = min(total, error_line + context_lines)

        snippet = []
        for i in range(start - 1, end):
            ln = i + 1
            prefix = ">>> " if ln == error_line else "    "
            snippet.append(f"{ln:4d}{prefix}{lines[i].rstrip()}")

        return {
            "code": "\n".join(snippet),
            "start_line": start,
            "end_line": end
        }

    except Exception as e:
        logger.debug(f"Snippet skipped for {file_path}:{error_line} → {e}")
        return None
