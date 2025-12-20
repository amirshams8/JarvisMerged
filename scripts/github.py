import os
import subprocess
import requests
from scripts.logger import logger

def create_branch(branch_name):
    import subprocess

    # Check if branch exists
    result = subprocess.run(
        ["git", "rev-parse", "--verify", branch_name],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )

    if result.returncode == 0:
        subprocess.run(["git", "checkout", branch_name], check=True)
    else:
        subprocess.run(["git", "checkout", "-b", branch_name], check=True)


def push_branch(branch_name: str):
    """Push branch to origin."""
    subprocess.run(
        ["git", "push", "-u", "origin", branch_name, "--force"],
        check=True
    )
    logger.info(f"⬆️ Pushed branch: {branch_name}")

def open_pull_request(branch_name: str, title: str, body: str):
    """Create a pull request on GitHub."""
    repo = os.getenv("GITHUB_REPOSITORY")
    token = os.getenv("GITHUB_TOKEN")

    if not repo or not token:
        raise RuntimeError("GITHUB_TOKEN or GITHUB_REPOSITORY not set")

    url = f"https://api.github.com/repos/{repo}/pulls"

    payload = {
        "title": title,
        "head": branch_name,
        "base": "main",
        "body": body
    }

    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json"
    }

    r = requests.post(url, headers=headers, json=payload)
    r.raise_for_status()

    pr_url = r.json()["html_url"]
    logger.info(f"🔀 PR created: {pr_url}")
