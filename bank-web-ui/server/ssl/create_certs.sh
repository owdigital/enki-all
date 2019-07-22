#!/bin/bash
read -p "Enter domain name (localhost, latestCommit-tool-abc.owex.oliverwyman.com, etc): " COMMON_NAME
if [ -z "$COMMON_NAME" ]; then
  >&2 echo "Domain name must not be empty."
  exit 1
fi

rm -f config.txt
echo "[req]" >> config.txt
echo "default_bits = 2048" >> config.txt
echo "default_keyfile = private_key.pem" >> config.txt
echo "encrypt_key = no" >> config.txt
echo "prompt = no" >> config.txt
echo "distinguished_name = req_distinguished_name" >> config.txt
echo "" >> config.txt
echo "[req_distinguished_name]" >> config.txt
echo "CN=$COMMON_NAME" >> config.txt

openssl req -new -x509 -sha256 -out ca_certificate.pem -days 1095 -config config.txt
rm -f config.txt

# To show certificate details:
# openssl x509 -in ca_certificate.pem -inform pem -noout -text
echo "Generated private_key.pem"
echo "Generated ca_certificate.pem"
echo "Done"