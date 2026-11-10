# Java ETL Validator

A small ETL (Extract, Transform, Load) pipeline in Java that reads a transactions CSV, validates each row, and writes three files: clean rows, rows flagged for review, and rejected rows with the reason.

## Rules
- Missing `txn_id` or `customer_id` is rejected
- Amount must be a positive number
- Currency must be a known code (USD, EUR, GBP, INR)
- Date must be a real date
- Duplicate `txn_id` is rejected
- Amounts over 10,000 are flagged for review
- One bad row never stops the run; it is logged with its reason

## Run it
```bash
mkdir out && javac -d out $(find src -name '*.java')
java -cp out dev.saisree.etl.PipelineTest
java -cp out dev.saisree.etl.Pipeline data/transactions.csv output
```

Sample output: `Read 10 rows: 2 clean, 2 flagged for review, 6 rejected`. The data is made-up sample data.
