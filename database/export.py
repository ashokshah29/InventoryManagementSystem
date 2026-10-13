"""Export current inventory records before committing."""

import argparse
import os
from pathlib import Path
import re
import subprocess
import sys

from bson import json_util
from pymongo import MongoClient


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--uri", default=os.environ.get("MONGODB_URI", "mongodb://127.0.0.1:27017/inventory_db"))
    parser.add_argument("--stage", action="store_true")
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    destination = root / "database" / "inventory_db"
    exports = {}
    counts = {}

    with MongoClient(args.uri, serverSelectionTimeoutMS=5000) as client:
        database = client.get_default_database(default="inventory_db")
        if database.name != "inventory_db":
            raise ValueError("The connection must select inventory_db.")
        if database.name not in client.list_database_names():
            raise ValueError("inventory_db does not exist on the selected server.")
        for name in sorted(database.list_collection_names()):
            if not re.fullmatch(r"[A-Za-z0-9_-]+", name):
                raise ValueError("A collection name cannot be safely exported as a filename.")
            collection = database[name]
            documents = list(collection.find().sort("_id", 1))
            metadata = {
                "collection": name,
                "options": collection.options(),
                "indexes": list(collection.list_indexes()),
            }
            for filename, data in ((f"{name}.json", documents), (f"{name}.metadata.json", metadata)):
                exports[filename] = json_util.dumps(
                    data, json_options=json_util.CANONICAL_JSON_OPTIONS, indent=2
                ) + "\n"
            counts[name] = len(documents)

    destination.mkdir(parents=True, exist_ok=True)
    for filename, content in exports.items():
        path = destination / filename
        if not path.exists() or path.read_text(encoding="utf-8") != content:
            temporary = path.with_suffix(path.suffix + ".tmp")
            temporary.write_text(content, encoding="utf-8", newline="\n")
            temporary.replace(path)
    for path in destination.glob("*.json"):
        if path.name not in exports:
            path.unlink()
    if args.stage:
        subprocess.run(["git", "add", "-A", "--", "database/inventory_db"], cwd=root, check=True)
    for name, count in counts.items():
        print(f"Exported inventory_db.{name}: {count} records")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"Database export failed ({type(error).__name__}). Check MongoDB connectivity, permissions, and dependencies.", file=sys.stderr)
        sys.exit(1)
