import subprocess
from scripts.logger import logger

def commit_changes(message: str, files: list[str]):
    """Stage and commit specified files."""
    logger.info("📦 Committing changes")

    subprocess.run(
        ["git", "add"] + files,
        check=True
    )

    subprocess.run(
        ["git", "commit", "-m", message],
        check=True
    )

    logger.info("✅ Commit created (not pushed)")
