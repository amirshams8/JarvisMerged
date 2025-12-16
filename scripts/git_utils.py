import subprocess
from logger import logger


def commit_and_push(message: str, files: list[str]):
    logger.info("📦 Committing changes")

    subprocess.run(
        ["git", "add"] + files,
        check=True
    )

    subprocess.run(
        ["git", "commit", "-m", message],
        check=True
    )

    subprocess.run(
        ["git", "push"],
        check=True
    )

    logger.info("🚀 Changes pushed to repository")
