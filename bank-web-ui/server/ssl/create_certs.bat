@echo off
setlocal
pushd "%~dp0"

echo.
echo NOTE: You must have openssl.exe installed and in PATH for this to work.
echo Also - make sure you have the latest openssl.
echo.

set /p common_name="Enter domain name (localhost, latestCommit-tool-abc.owex.oliverwyman.com, etc): "

REM Build config.txt with the right options
if exist config.txt del /q config.txt
echo [req] >> config.txt
echo default_bits = 2048 >> config.txt
echo default_keyfile = private_key.pem >> config.txt
echo encrypt_key = no >> config.txt
echo prompt = no >> config.txt
echo distinguished_name = req_distinguished_name >> config.txt
echo. >> config.txt
echo [req_distinguished_name] >> config.txt
echo CN=%common_name% >> config.txt

openssl req -sha256 -new -x509 -out ca_certificate.pem -days 1095 -config config.txt

if exist config.txt del /q config.txt

REM Show the certificate details:
REM openssl x509 -in ca_certificate.pem -inform pem -noout -text

echo Private key generated:      private_key.pem
echo Public key / CA generated:  ca_certificate.pem

popd
endlocal