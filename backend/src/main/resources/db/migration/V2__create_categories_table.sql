-- Create categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    parent_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    icon VARCHAR(50),
    color VARCHAR(7),  -- Hex color code
    is_custom BOOLEAN DEFAULT FALSE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on user_id for faster user-specific queries
CREATE INDEX idx_categories_user_id ON categories(user_id);

-- Create index on parent_id for hierarchy queries
CREATE INDEX idx_categories_parent_id ON categories(parent_id);

-- Create index on type for filtering
CREATE INDEX idx_categories_type ON categories(type);

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert default expense categories
INSERT INTO categories (name, type, icon, color, is_custom, user_id, description) VALUES
    ('Food & Dining', 'EXPENSE', 'restaurant', '#FF6B6B', FALSE, NULL, 'Restaurants, groceries, and food delivery'),
    ('Transportation', 'EXPENSE', 'car', '#4ECDC4', FALSE, NULL, 'Gas, public transit, rideshare, parking'),
    ('Shopping', 'EXPENSE', 'shopping_cart', '#95E1D3', FALSE, NULL, 'Clothing, electronics, and general shopping'),
    ('Entertainment', 'EXPENSE', 'movie', '#F38181', FALSE, NULL, 'Movies, concerts, games, and hobbies'),
    ('Housing', 'EXPENSE', 'home', '#AA96DA', FALSE, NULL, 'Rent, mortgage, and home maintenance'),
    ('Utilities', 'EXPENSE', 'bolt', '#FCBAD3', FALSE, NULL, 'Electricity, water, gas, internet'),
    ('Healthcare', 'EXPENSE', 'medical_services', '#A8D8EA', FALSE, NULL, 'Doctor visits, medications, insurance'),
    ('Education', 'EXPENSE', 'school', '#FFD93D', FALSE, NULL, 'Tuition, books, courses, and training'),
    ('Personal Care', 'EXPENSE', 'spa', '#6BCB77', FALSE, NULL, 'Haircuts, beauty products, gym'),
    ('Travel', 'EXPENSE', 'flight', '#4D96FF', FALSE, NULL, 'Flights, hotels, vacation expenses'),
    ('Insurance', 'EXPENSE', 'shield', '#9D84B7', FALSE, NULL, 'Health, auto, home, life insurance'),
    ('Financial', 'EXPENSE', 'account_balance', '#FFA07A', FALSE, NULL, 'Bank fees, interest, investments'),
    ('Kids', 'EXPENSE', 'child_care', '#FFB6C1', FALSE, NULL, 'Children expenses, daycare, activities'),
    ('Pets', 'EXPENSE', 'pets', '#98D8C8', FALSE, NULL, 'Pet food, vet, supplies'),
    ('Gifts & Donations', 'EXPENSE', 'card_giftcard', '#F7DC6F', FALSE, NULL, 'Gifts, charity, donations'),
    ('Subscriptions', 'EXPENSE', 'subscriptions', '#BB8FCE', FALSE, NULL, 'Streaming, software, memberships'),
    ('Other', 'EXPENSE', 'more_horiz', '#BDC3C7', FALSE, NULL, 'Miscellaneous expenses');

-- Insert default income categories
INSERT INTO categories (name, type, icon, color, is_custom, user_id, description) VALUES
    ('Salary', 'INCOME', 'work', '#52C41A', FALSE, NULL, 'Regular salary and wages'),
    ('Freelance', 'INCOME', 'business_center', '#73D13D', FALSE, NULL, 'Freelance work and consulting'),
    ('Investment', 'INCOME', 'trending_up', '#95DE64', FALSE, NULL, 'Dividends, interest, capital gains'),
    ('Rental', 'INCOME', 'apartment', '#B7EB8F', FALSE, NULL, 'Rental property income'),
    ('Business', 'INCOME', 'store', '#D9F7BE', FALSE, NULL, 'Business revenue'),
    ('Other Income', 'INCOME', 'attach_money', '#F6FFED', FALSE, NULL, 'Other sources of income');

-- Insert subcategories for Food & Dining
INSERT INTO categories (name, type, parent_id, icon, color, is_custom, user_id)
SELECT
    subcategory.name,
    'EXPENSE',
    c.id,
    subcategory.icon,
    '#FF6B6B',
    FALSE,
    NULL
FROM categories c
CROSS JOIN (
    VALUES
        ('Groceries', 'shopping_basket'),
        ('Restaurants', 'restaurant'),
        ('Fast Food', 'fastfood'),
        ('Coffee & Tea', 'local_cafe'),
        ('Sweets & Desserts', 'cake')
) AS subcategory(name, icon)
WHERE c.name = 'Food & Dining';

-- Insert subcategories for Transportation
INSERT INTO categories (name, type, parent_id, icon, color, is_custom, user_id)
SELECT
    subcategory.name,
    'EXPENSE',
    c.id,
    subcategory.icon,
    '#4ECDC4',
    FALSE,
    NULL
FROM categories c
CROSS JOIN (
    VALUES
        ('Gas & Fuel', 'local_gas_station'),
        ('Public Transit', 'directions_bus'),
        ('Rideshare', 'local_taxi'),
        ('Parking', 'local_parking'),
        ('Car Maintenance', 'build')
) AS subcategory(name, icon)
WHERE c.name = 'Transportation';

-- Insert subcategories for Housing
INSERT INTO categories (name, type, parent_id, icon, color, is_custom, user_id)
SELECT
    subcategory.name,
    'EXPENSE',
    c.id,
    subcategory.icon,
    '#AA96DA',
    FALSE,
    NULL
FROM categories c
CROSS JOIN (
    VALUES
        ('Rent', 'vpn_key'),
        ('Mortgage', 'account_balance'),
        ('Home Maintenance', 'handyman'),
        ('Furniture', 'weekend'),
        ('Home Improvement', 'construction')
) AS subcategory(name, icon)
WHERE c.name = 'Housing';
