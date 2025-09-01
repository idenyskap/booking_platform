-- Create businesses table
CREATE TABLE businesses (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    business_type VARCHAR(100) NOT NULL,
    registration_number VARCHAR(100),
    tax_id VARCHAR(100),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    website VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_business_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT chk_verification_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);

-- Create business_addresses table
CREATE TABLE business_addresses (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL,
    address_type VARCHAR(50) DEFAULT 'PRIMARY',
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_primary BOOLEAN DEFAULT false,
    
    CONSTRAINT fk_business_addresses_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT chk_address_type CHECK (address_type IN ('PRIMARY', 'BILLING', 'SHIPPING', 'BRANCH'))
);

-- Create business_hours table
CREATE TABLE business_hours (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL,
    day_of_week INTEGER NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6), -- 0=Sunday, 6=Saturday
    opening_time TIME,
    closing_time TIME,
    is_closed BOOLEAN DEFAULT false,
    
    CONSTRAINT fk_business_hours_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT unique_business_day UNIQUE (business_id, day_of_week)
);

-- Create business_services table
CREATE TABLE business_services (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    price DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'USD',
    duration_minutes INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_business_services_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
);

-- Create business_images table
CREATE TABLE business_images (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_type VARCHAR(50) DEFAULT 'GALLERY',
    alt_text VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT false,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_business_images_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT chk_image_type CHECK (image_type IN ('LOGO', 'COVER', 'GALLERY', 'CERTIFICATE'))
);

-- Create business_social_media table
CREATE TABLE business_social_media (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    profile_url VARCHAR(255) NOT NULL,
    follower_count INTEGER DEFAULT 0,
    is_verified BOOLEAN DEFAULT false,
    
    CONSTRAINT fk_business_social_media_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT unique_business_platform UNIQUE (business_id, platform)
);

-- Create indexes for better performance
CREATE INDEX idx_businesses_owner_id ON businesses(owner_id);
CREATE INDEX idx_businesses_business_type ON businesses(business_type);
CREATE INDEX idx_businesses_status ON businesses(status);
CREATE INDEX idx_businesses_verification_status ON businesses(verification_status);
CREATE INDEX idx_businesses_created_at ON businesses(created_at);

CREATE INDEX idx_business_addresses_business_id ON business_addresses(business_id);
CREATE INDEX idx_business_addresses_city ON business_addresses(city);
CREATE INDEX idx_business_addresses_primary ON business_addresses(is_primary);

CREATE INDEX idx_business_hours_business_id ON business_hours(business_id);
CREATE INDEX idx_business_hours_day ON business_hours(day_of_week);

CREATE INDEX idx_business_services_business_id ON business_services(business_id);
CREATE INDEX idx_business_services_category ON business_services(category);
CREATE INDEX idx_business_services_active ON business_services(is_active);

CREATE INDEX idx_business_images_business_id ON business_images(business_id);
CREATE INDEX idx_business_images_type ON business_images(image_type);
CREATE INDEX idx_business_images_primary ON business_images(is_primary);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_businesses_updated_at BEFORE UPDATE ON businesses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_business_services_updated_at BEFORE UPDATE ON business_services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();