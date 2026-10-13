# Existing inventory database export

This folder contains an export of the existing `inventory_db` MongoDB data.
The export is refreshed automatically before each commit when the hook is installed.

`inventory_db/products.json` uses canonical MongoDB Extended JSON to preserve
document IDs, dates, numeric types, and decimal prices.
`inventory_db/products.metadata.json` records collection options and indexes,
including the unique SKU index.

## Include future data in commits

Install the dependency and hook once on each checkout:

```powershell
python -m pip install pymongo
powershell -ExecutionPolicy Bypass -File database/install-hook.ps1
```

Keep MongoDB running when committing. The hook exports all collections in
`inventory_db` and stages the current records and indexes in the same commit.
New records, updates, and deletions are reflected in the export. A push then
uploads the committed export to GitHub. Changes made after a commit require
another commit before they can be pushed.

The connection defaults to `mongodb://127.0.0.1:27017/inventory_db` and respects
the `MONGODB_URI` environment variable used by the application. Credentials are
never written into the export. An export failure blocks the commit.
The hook is local to this checkout and must be installed again after cloning.

To refresh the export manually:

```powershell
python database/export.py --stage
```

## Restore

Start MongoDB locally, then run these commands from the project root:

```powershell
python -m pip install pymongo
python database/restore.py
```

To restore into a separate database for inspection:

```powershell
python database/restore.py --database inventory_db_review
```

The script restores indexes and inserts exported records. If a record with the
same `_id` already exists, it replaces that record. Other records remain in place.
The default connection is `mongodb://127.0.0.1:27017`; use `--uri` to select
another MongoDB server.
