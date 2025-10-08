-- 👤 USERS
-- Password je BCrypt hash za: test1234

INSERT INTO users (username, password, email, full_name, phone, created_at)
VALUES ('guest', '$2a$10$K08edeVjDecfJsdKeV8lYOafJ2fEpXKQeWh7fkw/2t0D69.iH1trK', 'guest@hotel.com', 'John Guest',
        '+385 91 123 4567', CURRENT_TIMESTAMP),
       ('receptionist', '$2a$10$K08edeVjDecfJsdKeV8lYOafJ2fEpXKQeWh7fkw/2t0D69.iH1trK', 'receptionist@hotel.com',
        'Ana Kovač', '+385 91 234 5678', CURRENT_TIMESTAMP),
       ('manager', '$2a$10$K08edeVjDecfJsdKeV8lYOafJ2fEpXKQeWh7fkw/2t0D69.iH1trK', 'manager@hotel.com', 'Marko Horvat',
        '+385 91 345 6789', CURRENT_TIMESTAMP),
       ('admin', '$2a$10$K08edeVjDecfJsdKeV8lYOafJ2fEpXKQeWh7fkw/2t0D69.iH1trK', 'admin@hotel.com', 'Super Admin',
        '+385 91 999 9999', CURRENT_TIMESTAMP);

-- 🔐 USER ROLES
INSERT INTO user_roles (user_id, role)
VALUES (1, 'GUEST'),
       (2, 'RECEPTIONIST'),
       (3, 'MANAGER'),
       (4, 'GUEST'),
       (4, 'RECEPTIONIST'),
       (4, 'MANAGER');

-- 🏠 ROOMS
INSERT INTO rooms (room_number, room_type, price_per_night, capacity, status, description)
VALUES ('101', 'SINGLE', 49.99, 1, 'AVAILABLE', 'Udobna jednokrevetna soba sa pogledom na grad'),
       ('102', 'SINGLE', 49.99, 1, 'AVAILABLE', 'Kompaktna soba idealna za poslovne putnike'),
       ('201', 'DOUBLE', 79.99, 2, 'AVAILABLE', 'Prostrana dvokrevetna soba sa balkonom'),
       ('202', 'DOUBLE', 79.99, 2, 'AVAILABLE', 'Moderna soba sa king size krevetom'),
       ('203', 'DOUBLE', 79.99, 2, 'OCCUPIED', 'Soba trenutno zauzeta'),
       ('301', 'SUITE', 149.99, 4, 'AVAILABLE', 'Luksuzni apartman sa dnevnim boravkom i jacuzzijem'),
       ('302', 'SUITE', 149.99, 4, 'MAINTENANCE', 'Penthouse apartman sa panoramskim pogledom (trenutno u održavanju)');