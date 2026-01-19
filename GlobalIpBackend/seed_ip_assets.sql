-- Check for existing data
SELECT id, title, asset_type, application_date, jurisdiction, assignee, keywords
FROM ip_assets
ORDER BY application_date DESC
LIMIT 10;

-- Insert sample data for dashboard graphs
INSERT INTO ip_assets (external_id, title, description, asset_type, application_date, jurisdiction, assignee, keywords, created_at, updated_at)
VALUES
('EX123', 'AI Patent Example', 'A new AI invention', 'PATENT', '2025-12-01', 'US', 'OpenAI', 'AI', NOW(), NOW()),
('EX124', 'Blockchain Trademark', 'A blockchain brand', 'TRADEMARK', '2025-11-15', 'EP', 'Blockchain Inc', 'Blockchain', NOW(), NOW()),
('EX125', 'IoT Security Patent', 'IoT security system', 'PATENT', '2025-10-20', 'CN', 'IoT Corp', 'IoT', NOW(), NOW()),
('EX126', 'AI/ML Patent', 'Machine learning method', 'PATENT', '2025-09-10', 'US', 'OpenAI', 'AI', NOW(), NOW()),
('EX127', 'Blockchain Patent', 'Blockchain protocol', 'PATENT', '2025-08-05', 'EP', 'Blockchain Inc', 'Blockchain', NOW(), NOW());

-- You can run this script in your PostgreSQL client to seed test data for the dashboard.
