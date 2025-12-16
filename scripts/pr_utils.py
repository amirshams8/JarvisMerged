import os
import subprocess
import requests
from logger import logger


def create_branch(branch_name: str):
    subprocess.run(["git", "checkout", "-b", branch_name], check=True)


def push_branch(branch_name: str):
    subprocess.run(
        ["git", "push", "-u", "origin", branch_name],
        check=True
    )


def open_pull_request(branch_name: str, title: str, body: str):
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
