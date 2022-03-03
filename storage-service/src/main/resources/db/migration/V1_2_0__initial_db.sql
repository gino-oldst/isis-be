INSERT INTO app_user(id, date_created, date_modified, deleted, password, username)
VALUES
('45552420-a8d3-4a9d-a53c-b3ea04b4b500','2022-02-16 18:03:48.1383780','2022-02-16 20:21:24.7228790',0,'$2a$10$jXM0Ju/3XYu7xE9f5v6hfeWZUgusHjgOnz0jXiVmWCQRWKEIthfbq','admin');

INSERT INTO role(id, role, user_id)
VALUES
('d9a20c9d-a771-4244-9e47-63112bb739af','ADMIN','45552420-a8d3-4a9d-a53c-b3ea04b4b500');