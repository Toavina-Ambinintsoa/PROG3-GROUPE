CREATE TABLE collectivities (
                                id VARCHAR(10) PRIMARY KEY,
                                number INT,
                                name VARCHAR(100),
                                locality VARCHAR(100),
                                specialization VARCHAR(100)
);

CREATE TABLE members (
                         id VARCHAR(10) PRIMARY KEY,
                         collectivity_id VARCHAR(10),
                         last_name VARCHAR(100),
                         first_name VARCHAR(100),
                         birth_date DATE,
                         gender CHAR(1),
                         address TEXT,
                         profession VARCHAR(100),
                         phone VARCHAR(20),
                         email VARCHAR(100),
                         occupation VARCHAR(50),
                         FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE member_references (
                                   member_id VARCHAR(10),
                                   reference_id VARCHAR(10),
                                   PRIMARY KEY (member_id, reference_id),
                                   FOREIGN KEY (member_id) REFERENCES members(id),
                                   FOREIGN KEY (reference_id) REFERENCES members(id)
);

CREATE TABLE contributions (
                               id VARCHAR(10) PRIMARY KEY,
                               collectivity_id VARCHAR(10),
                               label VARCHAR(100),
                               status VARCHAR(20),
                               frequency VARCHAR(20),
                               eligible_since DATE,
                               amount INT,
                               FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE accounts (
                          id VARCHAR(20) PRIMARY KEY,
                          collectivity_id VARCHAR(10),
                          type VARCHAR(50),
                          initial_balance INT,
                          holder VARCHAR(100),
                          phone VARCHAR(20),
                          FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE payments (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          collectivity_id VARCHAR(10),
                          member_id VARCHAR(10),
                          amount INT,
                          account_id VARCHAR(20),
                          payment_method VARCHAR(50),
                          payment_date DATE,
                          FOREIGN KEY (collectivity_id) REFERENCES collectivities(id),
                          FOREIGN KEY (member_id) REFERENCES members(id),
                          FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE TABLE transactions (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              collectivity_id VARCHAR(10),
                              member_id VARCHAR(10),
                              amount INT,
                              account_id VARCHAR(20),
                              method VARCHAR(50),
                              created_at DATE,
                              FOREIGN KEY (collectivity_id) REFERENCES collectivities(id),
                              FOREIGN KEY (member_id) REFERENCES members(id),
                              FOREIGN KEY (account_id) REFERENCES accounts(id)
);