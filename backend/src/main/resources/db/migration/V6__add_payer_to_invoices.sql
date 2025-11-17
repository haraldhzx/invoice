-- Add payer_id column to invoices table
ALTER TABLE invoices
ADD COLUMN payer_id UUID;

-- Add foreign key constraint
ALTER TABLE invoices
ADD CONSTRAINT fk_invoice_payer
FOREIGN KEY (payer_id) REFERENCES payers(id) ON DELETE SET NULL;

-- Create index on payer_id for faster lookups
CREATE INDEX idx_invoices_payer_id ON invoices(payer_id);

-- Add column comment
COMMENT ON COLUMN invoices.payer_id IS 'Reference to the payer who paid for this invoice (optional)';
