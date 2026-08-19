INSERT INTO identity.users (
    id,
    email,
    password_hash,
    role,
    status,
    first_name,
    last_name,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'admin@nexora.com',
    '$2a$10$c7Cgq2C6sC7IIn/zY1wN6.G5O9D6O8V2kS3s4N5O6P7Q8R9S0T1U2',
    'admin',
    'active',
    'Nexora',
    'Admin',
    NOW(),
    NOW()
) 
ON CONFLICT (email) DO UPDATE 
SET password_hash = EXCLUDED.password_hash,
    status = 'active',
    failed_login_count = 0,
    locked_until = NULL,
    updated_at = NOW();
