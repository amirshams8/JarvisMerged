from pathlib import Path

OUTPUT_FILE = "repo_source_dump.txt"

INCLUDE_EXTENSIONS = {
    ".py", ".kt", ".java", ".xml", ".gradle", ".groovy",
    ".js", ".ts", ".json", ".yml", ".yaml",
    ".md", ".txt", ".sh", ".properties"
}

EXCLUDE_DIRS = {
    ".git", ".github", "__pycache__", "build", "dist",
    ".gradle", ".idea", "node_modules"
}


def is_excluded(path: Path) -> bool:
    return any(p in EXCLUDE_DIRS for p in path.parts)


def generate_tree(root: Path) -> str:
    lines = []

    def walk(dir_path: Path, prefix=""):
        entries = sorted(
            [p for p in dir_path.iterdir() if not is_excluded(p)],
            key=lambda x: (x.is_file(), x.name.lower())
        )

        for index, path in enumerate(entries):
            connector = "└── " if index == len(entries) - 1 else "├── "
            lines.append(prefix + connector + path.name)

            if path.is_dir():
                extension = "    " if index == len(entries) - 1 else "│   "
                walk(path, prefix + extension)

    lines.append(".")
    walk(root)
    return "\n".join(lines)


def dump_source(root: Path, out):
    for path in sorted(root.rglob("*")):
        if path.is_file() and not is_excluded(path) and path.suffix in INCLUDE_EXTENSIONS:
            out.write(f"\n===== FILE: {path} =====\n")
            try:
                out.write(path.read_text(encoding="utf-8"))
            except UnicodeDecodeError:
                out.write("[BINARY OR ENCODING ERROR]")
            out.write("\n")


def main():
    root = Path(".").resolve()

    with open(OUTPUT_FILE, "w", encoding="utf-8") as out:
        out.write("==============================\n")
        out.write("FILE TREE\n")
        out.write("==============================\n\n")
        out.write(generate_tree(root))

        out.write("\n\n==============================\n")
        out.write("SOURCE CODE\n")
        out.write("==============================\n")

        dump_source(root, out)

    print(f"✅ File tree + source dumped to {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
