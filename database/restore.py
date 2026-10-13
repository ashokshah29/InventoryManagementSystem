"""Restore the exported inventory collections and indexes."""

import argparse
from pathlib import Path

from bson import json_util
from pymongo import MongoClient


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--uri", default="mongodb://127.0.0.1:27017")
    parser.add_argument("--database", default="inventory_db")
    args = parser.parse_args()
    source = Path(__file__).resolve().parent / "inventory_db"

    with MongoClient(args.uri, serverSelectionTimeoutMS=5000) as client:
        database = client[args.database]
        for metadata_path in sorted(source.glob("*.metadata.json")):
            metadata = json_util.loads(metadata_path.read_text(encoding="utf-8"))
            name = metadata["collection"]
            documents = json_util.loads(
                (source / f"{name}.json").read_text(encoding="utf-8")
            )
            if name not in database.list_collection_names():
                database.create_collection(name, **metadata["options"])
            collection = database[name]
            for index in metadata["indexes"]:
                if index["name"] == "_id_":
                    continue
                options = {
                    key: value for key, value in index.items()
                    if key not in {"key", "v", "ns"}
                }
                collection.create_index(list(index["key"].items()), **options)
            for document in documents:
                collection.replace_one({"_id": document["_id"]}, document, upsert=True)
            print(f"Restored {len(documents)} documents to {args.database}.{name}")


if __name__ == "__main__":
    main()
