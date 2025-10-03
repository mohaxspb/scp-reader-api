rm -rf /data/scp-reader.com/ssl/*.pfx

openssl pkcs12 \
-export -out /data/scp-reader.com/ssl/bundle.pfx \
-inkey /etc/letsencrypt/live/scp-reader.com/privkey.pem \
-in /etc/letsencrypt/live/scp-reader.com/cert.pem \
-certfile /etc/letsencrypt/live/scp-reader.com/chain.pem \
-password pass:password

chown tomcat /data/scp-reader.com/ssl/*.pfx