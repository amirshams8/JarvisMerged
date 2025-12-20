import os
import requests
from requests import HTTPError, RequestException
from scripts.logger import logger

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")

OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
OPENROUTER_MODEL = "kwaipilot/kat-coder-pro:free"


class OpenRouterProvider:
    def __init__(self):
        if not OPENROUTER_API_KEY:
            raise RuntimeError("OPENROUTER_API_KEY not set")

    def ask(self, prompt: str) -> str:
        payload = {
            "model": OPENROUTER_MODEL,
            "messages": [
                {"role": "user", "content": prompt}
            ],
            "temperature": 0.1,
            "max_tokens": 2000,
        }

        headers = {
            "Authorization": f"Bearer {OPENROUTER_API_KEY}",
            "Content-Type": "application/json",
        }

        r = requests.post(
            OPENROUTER_URL,
            headers=headers,
            json=payload,
            timeout=90,
        )

        try:
            r.raise_for_status()
        except HTTPError as e:
            logger.error(f"❌ OpenRouter HTTP {r.status_code}: {r.text}")
            raise RuntimeError("OpenRouter request failed") from e

        try:
            data = r.json()
        except ValueError:
            logger.error("❌ OpenRouter returned non-JSON response")
            logger.error(r.text[:500])
            raise RuntimeError("OpenRouter returned invalid JSON")

        try:
            return data["choices"][0]["message"]["content"]
        except (KeyError, IndexError):
            logger.error(f"❌ Malformed OpenRouter response: {data}")
            raise RuntimeError("OpenRouter response malformed")


class CompositeProvider:
    def __init__(self):
        self.provider = OpenRouterProvider()

    def ask(self, prompt: str) -> str:
        logger.info("🧠 Using OpenRouter (kat-coder-pro:free)")
        return self.provider.ask(prompt)


def get_llm_provider():
    logger.info("🧠 LLM provider initialized (OpenRouter only)")
    return CompositeProvider()
