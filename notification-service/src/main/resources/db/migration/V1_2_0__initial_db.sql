IF NOT EXISTS
(SELECT * FROM sys.tables t
JOIN sys.schemas s
ON (t.schema_id = s.schema_id)
WHERE s.name = 'dbo'
AND t.name = 'message_template')
CREATE TABLE dbo.message_template
(
    id                      VARCHAR(255)    NOT NULL PRIMARY KEY,
    date_created            DATETIME2(7)    NULL,
    date_modified           DATETIME2(7)    NULL,
    deleted                 BIT  			NOT NULL,
    name                    VARCHAR(255)    NOT NULL,
    subject                 VARCHAR(255)    NOT NULL,
    text_template           TEXT            NOT NULL,
    email_template          TEXT            NOT NULL
)
    go

CREATE INDEX idx_message_template_id ON message_template (ID);

IF NOT EXISTS
(SELECT * FROM sys.tables t
JOIN sys.schemas s
ON (t.schema_id = s.schema_id)
WHERE s.name = 'dbo'
AND t.name = 'notification_history')
CREATE TABLE dbo.notification_history
(
    id                      VARCHAR(255)    NOT NULL PRIMARY KEY,
    subject                 VARCHAR(255)    NOT NULL,
    message                 TEXT            NOT NULL,
    recipient               VARCHAR(255)    NOT NULL,
    date_sent               DATETIME2(7)    NULL,
)
    go

CREATE INDEX idx_notification_history_id ON notification_history (ID);