-- Create payers table
CREATE TABLE IF NOT EXISTS payers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(20),
    description VARCHAR(500),
    color VARCHAR(50),
    icon VARCHAR(50),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_payer_name_per_user UNIQUE (user_id, name)
);

-- Create index on user_id for faster lookups
CREATE INDEX idx_payers_user_id ON payers(user_id);

-- Create index on user_id and active for faster active payer lookups
CREATE INDEX idx_payers_user_id_active ON payers(user_id, active);

-- Create index on user_id and is_default for faster default payer lookups
CREATE INDEX idx_payers_user_id_default ON payers(user_id, is_default);

-- Add comment to table
COMMENT ON TABLE payers IS 'Stores information about individuals who pay for invoices (e.g., family members, team members)';

-- Add column comments
COMMENT ON COLUMN payers.id IS 'Unique identifier for the payer';
COMMENT ON COLUMN payers.user_id IS 'Reference to the user who owns this payer';
COMMENT ON COLUMN payers.name IS 'Display name of the payer';
COMMENT ON COLUMN payers.email IS 'Email address of the payer (optional)';
COMMENT ON COLUMN payers.phone_number IS 'Phone number of the payer (optional)';
COMMENT ON COLUMN payers.description IS 'Additional description or notes about the payer';
COMMENT ON COLUMN payers.color IS 'Color code for UI display (e.g., #FF5722)';
COMMENT ON COLUMN payers.icon IS 'Icon identifier for UI display';
COMMENT ON COLUMN payers.is_default IS 'Whether this is the default payer for the user';
COMMENT ON COLUMN payers.active IS 'Whether this payer is active (soft delete)';
COMMENT ON COLUMN payers.created_at IS 'Timestamp when the payer was created';
COMMENT ON COLUMN payers.updated_at IS 'Timestamp when the payer was last updated';
