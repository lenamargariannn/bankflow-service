-- V4__update_customer_user_relationship.sql
-- Add user_id foreign key to customers table and remove redundant fields

-- Step 1: Create a sequence for users if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_sequences WHERE schemaname = 'public' AND sequencename = 'users_id_seq') THEN
        CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 1;
        ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq');
        ALTER SEQUENCE users_id_seq OWNED BY users.id;

        -- Update the sequence to start from the current max ID + 1
        PERFORM setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 0) + 1, false);
    END IF;
END $$;

-- Step 2: Add user_id column to customers table (nullable initially for migration)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'customers' AND column_name = 'user_id') THEN
        ALTER TABLE customers ADD COLUMN user_id BIGINT;
    END IF;
END $$;

-- Step 3: Migrate existing customer data to create corresponding user records
DO $$
DECLARE
    customer_record RECORD;
    new_user_id BIGINT;
    username_counter INT := 1;
BEGIN
    FOR customer_record IN SELECT id, full_name, email, phone_number, created_at, updated_at FROM customers WHERE user_id IS NULL
    LOOP
        -- Create a user for each existing customer
        INSERT INTO users (username, password, enabled, email, full_name, created_at, updated_at)
        VALUES (
            'customer' || customer_record.id, -- Generate username from customer id
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/PFm', -- Default password (change on first login)
            TRUE,
            customer_record.email,
            customer_record.full_name,
            customer_record.created_at,
            customer_record.updated_at
        )
        RETURNING id INTO new_user_id;

        -- Assign ROLE_CUSTOMER to the new user
        INSERT INTO authorities (username, authority, created_at)
        VALUES ('customer' || customer_record.id, 'ROLE_CUSTOMER', customer_record.created_at);

        -- Update customer with the new user_id
        UPDATE customers SET user_id = new_user_id WHERE id = customer_record.id;

        username_counter := username_counter + 1;
    END LOOP;
END $$;

-- Step 4: Make user_id NOT NULL and add foreign key constraint
ALTER TABLE customers ALTER COLUMN user_id SET NOT NULL;

-- Add unique constraint on user_id
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_customers_user_id') THEN
        ALTER TABLE customers ADD CONSTRAINT uk_customers_user_id UNIQUE (user_id);
    END IF;
END $$;

-- Add foreign key constraint
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_customer_user') THEN
        ALTER TABLE customers ADD CONSTRAINT fk_customer_user
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Step 5: Remove redundant columns from customers table
DO $$
BEGIN
    -- Drop email column if it exists
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'customers' AND column_name = 'email') THEN
        -- Drop the index first
        DROP INDEX IF EXISTS idx_customers_email;
        ALTER TABLE customers DROP COLUMN email;
    END IF;

    -- Drop full_name column if it exists
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'customers' AND column_name = 'full_name') THEN
        ALTER TABLE customers DROP COLUMN full_name;
    END IF;
END $$;

-- Step 6: Create index on user_id for efficient lookups
CREATE INDEX IF NOT EXISTS idx_customers_user_id ON customers(user_id);

