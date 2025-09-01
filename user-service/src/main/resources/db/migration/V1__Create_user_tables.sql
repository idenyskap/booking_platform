-- Create users table with inheritance
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    user_type VARCHAR(31) NOT NULL,
    keycloak_id VARCHAR(255) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    
    -- Customer specific fields
    date_of_birth DATE,
    gender VARCHAR(10),
    notification_preferences VARCHAR(255),
    
    -- Provider specific fields
    bio TEXT,
    experience_years INTEGER,
    hourly_rate DECIMAL(10,2),
    availability_schedule TEXT,
    rating DECIMAL(3,2),
    total_bookings INTEGER DEFAULT 0,
    verified BOOLEAN DEFAULT false,
    verification_documents TEXT,
    
    -- BusinessOwner specific fields
    business_name VARCHAR(255),
    business_registration_number VARCHAR(100),
    tax_id VARCHAR(100),
    business_type VARCHAR(100),
    business_description TEXT,
    website_url VARCHAR(255),
    social_media_links TEXT,
    
    CONSTRAINT chk_user_type CHECK (user_type IN ('Customer', 'Provider', 'BusinessOwner'))
);

-- Create indexes for better performance
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Provider specific indexes
CREATE INDEX idx_users_provider_rating ON users(rating) WHERE user_type = 'Provider';
CREATE INDEX idx_users_provider_verified ON users(verified) WHERE user_type = 'Provider';
CREATE INDEX idx_users_provider_hourly_rate ON users(hourly_rate) WHERE user_type = 'Provider';

-- Business owner specific indexes
CREATE INDEX idx_users_business_name ON users(business_name) WHERE user_type = 'BusinessOwner';

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();