IF NOT EXISTS
(SELECT * FROM sys.tables t
JOIN sys.schemas s
ON (t.schema_id = s.schema_id)
WHERE s.name = 'dbo'
AND t.name = 'app_user')
CREATE TABLE dbo.app_user
(
    id                      VARCHAR(255)    NOT NULL PRIMARY KEY,
    date_created            DATETIME2(7)    NULL,
    date_modified           DATETIME2(7)    NULL,
    deleted                 BIT  			NOT NULL,
    password                VARCHAR(255)    NOT NULL,
    username                VARCHAR(255)    NOT NULL
)
    go

CREATE INDEX idx_app_user_id ON app_user (ID);

IF NOT EXISTS
(SELECT * FROM sys.tables t
JOIN sys.schemas s
ON (t.schema_id = s.schema_id)
WHERE s.name = 'dbo'
AND t.name = 'role')
CREATE TABLE dbo.role
(
    id                      VARCHAR(255)    NOT NULL PRIMARY KEY,
    role                    VARCHAR(255)    NOT NULL,
    user_id                 VARCHAR(255)    NOT NULL
)
    go

CREATE INDEX idx_role_id ON role (ID);

INSERT INTO app_user(id, date_created, date_modified, deleted, password, username)
VALUES
    ('45552420-a8d3-4a9d-a53c-b3ea04b4b500','2022-02-16 18:03:48.1383780','2022-02-16 20:21:24.7228790',0,'$2a$10$jXM0Ju/3XYu7xE9f5v6hfeWZUgusHjgOnz0jXiVmWCQRWKEIthfbq','admin');


INSERT INTO role(id, role, user_id)
VALUES
    ('d9a20c9d-a771-4244-9e47-63112bb739af','ADMIN','45552420-a8d3-4a9d-a53c-b3ea04b4b500');