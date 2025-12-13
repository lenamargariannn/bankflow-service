-- V3__update_schema_for_entities.sql
-- Update schema to match JPA entities:
-- 1. Rename transactions table to transaction_records
-- 2. Update account_number length to VARCHAR(50)
-- 3. Update status and type columns to VARCHAR(20)
-- 4. Add sequences for ID generation
-- 5. Update foreign key constraint names

-- Step 1: Check if transactions table exists and rename it to transaction_records
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'transactions') THEN
        -- Rename the table
        ALTER TABLE transactions RENAME TO transaction_records;

        -- Rename the primary key constraint
        ALTER INDEX transactions_pkey RENAME TO transaction_records_pkey;

        -- Rename the sequence
        ALTER SEQUENCE transactions_id_seq RENAME TO transaction_records_id_seq;

        -- Rename indexes
        ALTER INDEX IF EXISTS idx_transactions_type RENAME TO idx_transaction_records_type;
        ALTER INDEX IF EXISTS idx_transactions_from_account_id RENAME TO idx_transaction_records_from_account_id;
        ALTER INDEX IF EXISTS idx_transactions_to_account_id RENAME TO idx_transaction_records_to_account_id;
        ALTER INDEX IF EXISTS idx_transactions_timestamp RENAME TO idx_transaction_records_timestamp;
        ALTER INDEX IF EXISTS idx_transactions_account_activity RENAME TO idx_transaction_records_account_activity;

        -- Rename constraints
        ALTER TABLE transaction_records RENAME CONSTRAINT check_transaction_amount TO check_transaction_amount_old;
        ALTER TABLE transaction_records DROP CONSTRAINT IF EXISTS check_transaction_amount_old;
        ALTER TABLE transaction_records ADD CONSTRAINT check_transaction_amount CHECK (amount > 0);

        -- Update type column length
        ALTER TABLE transaction_records ALTER COLUMN type TYPE VARCHAR(20);

        -- Make from_account_id NOT NULL if it isn't already
        ALTER TABLE transaction_records ALTER COLUMN from_account_id SET NOT NULL;

        -- Update foreign key constraint names
        ALTER TABLE transaction_records DROP CONSTRAINT IF EXISTS transactions_from_account_id_fkey;
        ALTER TABLE transaction_records DROP CONSTRAINT IF EXISTS transactions_to_account_id_fkey;

        ALTER TABLE transaction_records
            ADD CONSTRAINT fk_transaction_from_account
            FOREIGN KEY (from_account_id) REFERENCES accounts(id) ON DELETE SET NULL;

        ALTER TABLE transaction_records
            ADD CONSTRAINT fk_transaction_to_account
            FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Step 2: Update accounts table
DO $$
BEGIN
    -- Update account_number column length
    ALTER TABLE accounts ALTER COLUMN account_number TYPE VARCHAR(50);

    -- Update status column length
    ALTER TABLE accounts ALTER COLUMN status TYPE VARCHAR(20);

    -- Update foreign key constraint name if needed
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'accounts_customer_id_fkey') THEN
        ALTER TABLE accounts DROP CONSTRAINT accounts_customer_id_fkey;
        ALTER TABLE accounts ADD CONSTRAINT fk_account_customer
            FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Step 3: Create sequences if they don't exist and update ID generation
DO $$
BEGIN
    -- Create customers_id_seq if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM pg_sequences WHERE schemaname = 'public' AND sequencename = 'customers_id_seq') THEN
        -- Get the current max ID
        EXECUTE 'CREATE SEQUENCE customers_id_seq START WITH ' ||
                (SELECT COALESCE(MAX(id), 0) + 1 FROM customers);

        -- Update the default value for id column
        ALTER TABLE customers ALTER COLUMN id SET DEFAULT nextval('customers_id_seq');

        -- Update sequence ownership
        ALTER SEQUENCE customers_id_seq OWNED BY customers.id;
    END IF;

    -- Create accounts_id_seq if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM pg_sequences WHERE schemaname = 'public' AND sequencename = 'accounts_id_seq') THEN
        -- Get the current max ID
        EXECUTE 'CREATE SEQUENCE accounts_id_seq START WITH ' ||
                (SELECT COALESCE(MAX(id), 0) + 1 FROM accounts);

        -- Update the default value for id column
        ALTER TABLE accounts ALTER COLUMN id SET DEFAULT nextval('accounts_id_seq');

        -- Update sequence ownership
        ALTER SEQUENCE accounts_id_seq OWNED BY accounts.id;
    END IF;
END $$;

