INSERT INTO users (id, email, name, password, is_blocked) VALUES
(1,  'john.doe@email.com',      'John Doe',      '$2a$12$kJLNVqUw9LRiNGsopmgjxObeObBX9oWjiqW1XftdQIXcnuLn8Y/Jq', FALSE), -- pass123
(2,  'jane.smith@email.com',    'Jane Smith',    '$2a$12$LdkByBQdq.kjCoB15N.kAuAVtf4S8Yh2cSv8LRW7Mey1/Vgdt6Uqy',       FALSE), --abc456
(3,  'bob.jones@email.com',     'Bob Jones',     '$2a$12$5kRKxQGiVU3c8dgXBeNC0OCSLjTKC5BYm8UXNpHDagPW/K2fzSPEu',    FALSE), --qwerty789
(4,  'alice.white@email.com',   'Alice White',   '$2a$12$8h19YlJ8kkXGjjg9gBUaQeg/zJDxhe/qvvGpjswmMkI2fEWSjzfCq',    FALSE), --secret567
(5,  'mike.wilson@email.com',   'Mike Wilson',   '$2a$12$saOEwavhrthLtONsJnTJHOJRCucKDoTphHQfWagtvmbnPtU63S4cq',   FALSE), --mypassword
(6,  'sara.brown@email.com',    'Sara Brown',    '$2a$12$jnDHWKX906m3REKenlDwyueB5M.ZU21LihkQARG5..gkZUjTwTBuu',   FALSE), --letmein123
(7,  'tom.jenkins@email.com',   'Tom Jenkins',   '$2a$12$2VFUF.lSdjJDE/kCtJIE4.TzJPWh2GuPovSjcqI4nHhDhKrBC2NOm',     FALSE), --pass4321
(8,  'lisa.taylor@email.com',   'Lisa Taylor',   '$2a$12$22uIkYfpkfmn8z8hJxPZfOoPhV5yy./KBQNsHBHcASzrHEsUyvg7K',    FALSE), --securepwd
(9,  'david.wright@email.com',  'David Wright',  '$2a$12$zYgX9ulBDAi6iCJMOF3hXu1D9fEQ37JwRuh9rfSyUV/tjJOG6NTZi',    FALSE), --access123
(10, 'emily.harris@email.com',  'Emily Harris',  '$2a$12$sg9lYlpFLOOVL/hcUWOcr.Q22lJtMQh2d2DytLtUaUd.Yuki40YCS',     FALSE); --1234abcd

INSERT INTO employees (id, birth_date, phone) VALUES
(1,  '1990-05-15', '+380501234567'),
(2,  '1985-09-20', '+380671234567'),
(3,  '1978-03-08', '+380931234567'),
(4,  '1982-11-25', '+380631234567'),
(5,  '1995-07-12', '+380971234567'),
(6,  '1989-01-30', '+380661234567'),
(7,  '1975-06-18', '+380509876543'),
(8,  '1987-12-04', '+380679876543'),
(9,  '1992-08-22', '+380939876543'),
(10, '1980-04-10', '+380639876543');

INSERT INTO users (id, email, name, password, is_blocked) VALUES
(11, 'client1@example.com',  'Medelyn Wright',   '$2a$12$mjZ8AM5RqrAdfj/vPmnzL.hHomovWc0BZ9aesLe5/OUozbL4NyKKy', FALSE), -- password123
(12, 'client2@example.com',  'Landon Phillips',  '$2a$12$C.nW3ETJdqlIPvbR6C4jseqBm4fWtaXzWMAFZ8GtkMs0qvzfQHdSO',   FALSE), --securepass
(13, 'client3@example.com',  'Harmony Mason',    '$2a$12$9cSGVPa7HxX1WrJtBMcOaOTUsFGwbo5q5lkTv0IQf1ZLMBfNyevem',       FALSE), --abc123
(14, 'client4@example.com',  'Archer Harper',    '$2a$12$7dF8svZ/.SuzbycUMfQh1u0df0R0q7ZJty3nK.y72TJyIio9oYuTC',      FALSE), --pass456
(15, 'client5@example.com',  'Kira Jacobs',      '$2a$12$GVGIL04bysBZZ4b94k0mbufeA08h9x.zfCboOwH8q8dYwNOMYW5aO',   FALSE), --letmein789
(16, 'client6@example.com',  'Maximus Kelly',    '$2a$12$09LzRgJOKgEEHjwar1aRi.2rmir7fF96/TpN1GIsOAMUVXjtR8P..',    FALSE), --adminpass
(17, 'client7@example.com',  'Sierra Mitchell',  '$2a$12$f0medtUHxd2sZAE/XI/Bm.jkL2yAxu06n5hrdMd8IFW6GaT3RDueO',   FALSE), --mypassword
(18, 'client8@example.com',  'Quinton Saunders', '$2a$12$UpXl4YVmn9VeaUpoAIy8EevsLMLIpEyUAoMywoRikAU5V4UICEhri',      FALSE), --test123
(19, 'client9@example.com',  'Amina Clarke',     '$2a$12$HZH/9ewQmQOTBVw2aVPA5eBpWnB00/yQgGpvqyH7X1Ukklm3bN6.6',    FALSE), --qwerty123
(20, 'client10@example.com', 'Bryson Chavez',    '$2a$12$OEAdnMuGqoJZGqTusMnUQ.Ah3h4ZzBDEbtNUD9dj8.4PZ/1T7eNQ2',      FALSE); --pass789

INSERT INTO clients (id, balance) VALUES
(11, 1000.00),
(12, 1500.50),
(13, 800.75),
(14, 1200.25),
(15, 900.80),
(16, 1100.60),
(17, 1300.45),
(18, 950.30),
(19, 1050.90),
(20, 880.20);

INSERT INTO books (name, genre, age_group, price, publication_date, author, pages, characteristics, description, language, image_path)
VALUES
('Harry Potter and the Philosopher''s Stone', 'Fantasy', 'TEEN', 450.00, '1997-06-26', 'J.K. Rowling', 223, 'Hardcover, Magic, Wizards', 'Harry Potter thinks he is an ordinary boy - until he is rescued by a beetle-eyed giant of a man, enrolls at Hogwarts School of Witchcraft and Wizardry, learns to play Quidditch and does battle in a deadly duel.', 'ENGLISH', '/img/1.jpg'),
('Тіні забутих предків', 'Classic', 'ADULT', 150.00, '1911-01-01', 'Mykhailo Kotsiubynsky', 180, 'Carpathian Mountains, Hutsul culture, Tragedy', 'A tragic love story of Ivan and Marichka from rival families in the Carpathian Mountains. Often described as the Ukrainian Romeo and Juliet, it delves deep into Hutsul mythology and folklore.', 'UKRAINIAN', '/img/2.jpg'),
('1984', 'Dystopian', 'ADULT', 320.00, '1949-06-08', 'George Orwell', 328, 'Totalitarianism, Surveillance, Political Fiction', 'Among the seminal texts of the 20th century, Nineteen Eighty-Four is a rare work that grows more haunting as its futuristic purgatory becomes more real. A startiling and haunting novel about the surveillance state.', 'ENGLISH', '/img/3.jpg'),
('Le Petit Prince', 'Fable', 'CHILD', 220.00, '1943-04-06', 'Antoine de Saint-Exupéry', 96, 'Illustrations, Philosophy, Classic', 'A pilot stranded in the desert meets a young prince fallen to Earth from a tiny asteroid. The story is philosophical and includes social criticism, remarking on the strangeness of the adult world.', 'FRENCH', '/img/4.jpg'),
('Dune', 'Science Fiction', 'ADULT', 550.00, '1965-08-01', 'Frank Herbert', 412, 'Space Opera, Epic, Politics', 'Set on the desert planet Arrakis, Dune is the story of the boy Paul Atreides, heir to a noble family tasked with ruling an inhospitable world where the only thing of value is the "spice" melange.', 'ENGLISH', '/img/5.jpg'),
('Кобзар', 'Poetry', 'ADULT', 400.00, '1840-04-18', 'Taras Shevchenko', 700, 'Poetry, History, Patriotism', 'A collection of poems by Taras Shevchenko, a literary monument to freedom and the Ukrainian spirit. It is considered the foundation of modern Ukrainian literature.', 'UKRAINIAN', '/img/6.jpg'),
('The Last Wish', 'Fantasy', 'ADULT', 380.00, '1993-01-31', 'Andrzej Sapkowski', 288, 'Monsters, Magic, Adventure', 'Geralt of Rivia is a witcher. A cunning sorcerer. A merciless assassin. And a cold-blooded killer. His sole purpose: to destroy the monsters that plague the world.', 'ENGLISH', '/img/7.jpg'),
('Лісова пісня', 'Drama', 'TEEN', 180.00, '1911-02-01', 'Lesya Ukrainka', 120, 'Mythology, Nature, Romance', 'A poetic play dealing with the interaction between the human world and nature. A story of Mavka, a forest nymph, who falls in love with a human boy, exploring themes of betrayal and eternal love.', 'UKRAINIAN', '/img/8.jpg'),
('Faust', 'Tragedy', 'ADULT', 300.00, '1808-01-01', 'Johann Wolfgang von Goethe', 500, 'Philosophy, Drama, Classic', 'The story of a scholar who makes a deal with the Devil, exchanging his soul for unlimited knowledge and worldly pleasures. A masterpiece of German literature.', 'GERMAN', '/img/9.jpg'),
('Kafka on the Shore', 'Magical Realism', 'ADULT', 420.00, '2002-09-12', 'Haruki Murakami', 505, 'Surrealism, Cats, Mystery', 'The book tells the stories of Kafka Tamura, a runaway 15-year-old boy, and Nakata, an aging and illiterate simpleton who has the ability to converse with cats. Their paths align in mysterious ways.', 'JAPANESE', '/img/10.jpg');

ALTER TABLE users ALTER COLUMN id RESTART WITH 21;