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

def create_branch(branch_name: str):
    """Create or switch to a new branch."""
    logger.info(f"🌿 Creating/switching to branch '{branch_name}'")
    subprocess.run(
        ["git", "checkout", "-B", branch_name],
        check=True
    )
    logger.info(f"✅ Branch '{branch_name}' ready")

def push_branch(branch_name: str):
    """Push the current branch to origin."""
    logger.info(f"⬆️ Pushing branch '{branch_name}' to origin")
    subprocess.run(
        ["git", "push", "-u", "origin", branch_name, "--force"],
        check=True
    )
    logger.info(f"✅ Branch '{branch_name}' pushed")
