IF NOT EXISTS
(SELECT * FROM sys.tables t
JOIN sys.schemas s
ON (t.schema_id = s.schema_id)
WHERE s.name = 'dbo'
AND t.name = 'oauth_client_details')
    CREATE TABLE dbo.oauth_client_details
    (
        client_id               VARCHAR(255) NOT NULL PRIMARY KEY,
        client_secret               VARCHAR(255) NOT NULL,
        resource_ids            VARCHAR(255)  DEFAULT NULL,
        scope                   VARCHAR(255)  DEFAULT NULL,
        authorized_grant_types  VARCHAR(255)  DEFAULT NULL,
        web_server_redirect_uri VARCHAR(255)  DEFAULT NULL,
        authorities             VARCHAR(255)  DEFAULT NULL,
        access_token_validity   INT           DEFAULT NULL,
        refresh_token_validity  INT           DEFAULT NULL,
        additional_information  VARCHAR(4096) DEFAULT NULL,
        autoapprove             VARCHAR(255)  DEFAULT NULL
    )
go

CREATE INDEX idx_oauth_client_details_client_id ON oauth_client_details (CLIENT_ID);

INSERT INTO oauth_client_details(client_id, client_secret, resource_ids, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove)
VALUES
('INTERNAL', '$2b$12$B6VRl8VhEz7n.d2v3FcBcuzspnHYhmaAKvzmtaiSyF.F.nahTwqcm', 'INTERNAL_RESOURCE', 'INTERNAL', 'client_credentials', NULL, NULL, '86400', '0', '{}', NULL),
('WEB_APP', '$2b$12$B6VRl8VhEz7n.d2v3FcBcuzspnHYhmaAKvzmtaiSyF.F.nahTwqcm', 'INTERNAL_RESOURCE', 'WEB_APP', 'password,client_credentials', NULL, NULL, '86400', '0', '{}', NULL);