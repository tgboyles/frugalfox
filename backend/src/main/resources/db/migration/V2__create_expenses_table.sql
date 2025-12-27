-- Create expenses table
CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expense_date DATE NOT NULL,
    merchant VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    bank VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for common query patterns
CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date DESC);
CREATE INDEX idx_expenses_user_category ON expenses(user_id, category);
CREATE INDEX idx_expenses_merchant ON expenses(merchant);
CREATE INDEX idx_expenses_date ON expenses(expense_date DESC);

-- Comments for documentation
COMMENT ON TABLE expenses IS 'Stores user expense transactions';
COMMENT ON COLUMN expenses.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN expenses.expense_date IS 'Date when the expense occurred';
COMMENT ON COLUMN expenses.merchant IS 'Merchant name or expense description';
COMMENT ON COLUMN expenses.amount IS 'Expense amount in local currency';
COMMENT ON COLUMN expenses.bank IS 'Bank or payment source';
COMMENT ON COLUMN expenses.category IS 'Expense category for budgeting';
