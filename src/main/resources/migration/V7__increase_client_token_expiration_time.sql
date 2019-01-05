UPDATE oauth_client_details SET access_token_validity = '604800' WHERE client_id='client_id';
UPDATE oauth_client_details SET refresh_token_validity = '2592000' WHERE client_id='client_id';