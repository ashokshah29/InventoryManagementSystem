# Existing inventory database export

This folder contains the existing `inventory_db` MongoDB data exported on
October 13, 2026. The `products` collection contains one product record.

`inventory_db/products.json` uses canonical MongoDB Extended JSON to preserve
document IDs, dates, numeric types, and decimal prices.
`inventory_db/products.metadata.json` records collection options and indexes,
including the unique SKU index.

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
