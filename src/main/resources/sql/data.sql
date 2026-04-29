INSERT INTO collectivities VALUES
                               ('col-1', 1, 'Mpanorina', 'Ambatondrazaka', 'Riziculture'),
                               ('col-2', 2, 'Dobo voalohany', 'Ambatondrazaka', 'Pisciculture'),
                               ('col-3', 3, 'Tantely mamy', 'Brickaville', 'Apiculture');

INSERT INTO members VALUES
                        ('C1-M1','col-1','Nom membre 1','Prénom membre 1','1980-02-01','M','Lot II V M Ambato','Riziculteur','0341234567','member.1@fed-agri.mg','Président'),
                        ('C1-M2','col-1','Nom membre 2','Prénom membre 2','1982-03-05','M','Lot II F Ambato','Agriculteur','0321234567','member.2@fed-agri.mg','Vice président'),
                        ('C1-M3','col-1','Nom membre 3','Prénom membre 3','1992-03-10','M','Lot II J Ambato','Collecteur','0331234567','member.3@fed-agrimg','Secrétaire'),
                        ('C1-M4','col-1','Nom membre 4','Prénom membre 4','1988-05-22','F','Lot A K 50 Ambato','Distributeur','0381234567','member.4@fed-agri.mg','Trésorier'),
                        ('C1-M5','col-1','Nom membre 5','Prénom membre 5','1999-08-21','M','Lot UV 80 Ambato','Riziculteur','0373434567','member.5@fed-agri.mg','Confirmé'),
                        ('C1-M6','col-1','Nom membre 6','Prénom membre 6','1998-08-22','F','Lot UV 6 Ambato','Riziculteur','0372234567','member.6@fed-agri.mg','Confirmé'),
                        ('C1-M7','col-1','Nom membre 7','Prénom membre 7','1998-01-31','M','Lot UV 7 Ambato','Riziculteur','0374234567','member.7@fed-agri.mg','Confirmé'),
                        ('C1-M8','col-1','Nom membre 8','Prénom membre 8','1975-08-20','M','Lot UV 8 Ambato','Riziculteur','0370234567','member.8@fed-agri.mg','Confirmé');



INSERT INTO member_references VALUES
                                  ('C1-M3','C1-M1'), ('C1-M3','C1-M2'),
                                  ('C1-M4','C1-M1'), ('C1-M4','C1-M2'),
                                  ('C1-M5','C1-M1'), ('C1-M5','C1-M2'),
                                  ('C1-M6','C1-M1'), ('C1-M6','C1-M2'),
                                  ('C1-M7','C1-M1'), ('C1-M7','C1-M2'),
                                  ('C1-M8','C1-M6'), ('C1-M8','C1-M7');




INSERT INTO contributions VALUES
                              ('cot-1','col-1','Cotisation annuelle','ACTIVE','ANNUALLY','2026-01-01',100000),
                              ('cot-2','col-2','Cotisation annuelle','ACTIVE','ANNUALLY','2026-01-01',100000),
                              ('cot-3','col-3','Cotisation annuelle','ACTIVE','ANNUALLY','2026-01-01',50000);



INSERT INTO accounts VALUES
                         ('C1-A-CASH','col-1','CASH',0,NULL,NULL),
                         ('C1-A-MOBILE-1','col-1','ORANGE_MONEY',0,'Mpanorina','0370489612'),

                         ('C2-A-CASH','col-2','CASH',0,NULL,NULL),
                         ('C2-A-MOBILE-1','col-2','ORANGE_MONEY',0,'Dobo voalohany','0320489612'),

                         ('C3-A-CASH','col-3','CASH',0,NULL,NULL);





INSERT INTO payments (collectivity_id, member_id, amount, account_id, payment_method, payment_date) VALUES
                                                                                                        ('col-1','C1-M1',100000,'C1-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-1','C1-M2',100000,'C1-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-1','C1-M3',100000,'C1-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-1','C1-M4',100000,'C1-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-1','C1-M5',100000,'C1-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-1','C1-M6',100000,'C1-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-1','C1-M7',60000,'C1-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-1','C1-M8',90000,'C1-A-CASH','CASH','2026-01-01');



INSERT INTO transactions (collectivity_id, member_id, amount, account_id, method, created_at)
SELECT collectivity_id, member_id, amount, account_id, payment_method, payment_date
FROM payments WHERE collectivity_id='col-1';



INSERT INTO payments (collectivity_id, member_id, amount, account_id, payment_method, payment_date) VALUES
                                                                                                        ('col-2','C1-M1',60000,'C2-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-2','C1-M2',90000,'C2-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-2','C1-M3',100000,'C2-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-2','C1-M4',100000,'C2-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-2','C1-M5',100000,'C2-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-2','C1-M6',100000,'C2-A-CASH','CASH','2026-01-01'),
                                                                                                        ('col-2','C1-M7',40000,'C2-A-MOBILE-1','MOBILE MONEY','2026-01-01'),
                                                                                                        ('col-2','C1-M8',60000,'C2-A-MOBILE-1','MOBILE MONEY','2026-01-01');




INSERT INTO transactions (collectivity_id, member_id, amount, account_id, method, created_at)
SELECT collectivity_id, member_id, amount, account_id, payment_method, payment_date
FROM payments WHERE collectivity_id='col-2';