#
#   This file is part of Enki.
#
#   Copyright © 2016 - 2019 Oliver Wyman Ltd.
#
#   Enki is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   Enki is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with Enki.  If not, see <https://www.gnu.org/licenses/>
#

import os
from urllib.parse import urlparse, quote


def with_auth(url):
    basic_username = os.environ.get('BASIC_USERNAME', None)
    basic_password = os.environ.get('BASIC_PASSWORD', None)

    if url and basic_username and basic_password:
        parsed = urlparse(url)
        assert '@' not in parsed.netloc, "@ should not be in %r" % parsed.netloc
        url = parsed._replace(
            netloc="{}:{}@{}".format(
                quote(basic_username),
                quote(basic_password),
                parsed.netloc)).geturl()
    return url


ENKI_SERVER_URL = with_auth(os.environ.get('ENKI_SERVER_URL', None))
BANK_URLS = [
    with_auth(b) for b in os.environ.get(
        'BANK_URLS',
        '').split() if b]
print("Bank Web UI urls: %s" % BANK_URLS)
HYDRAS = {}
# Hacks because we can't tell the right one
BANKS = {}
if len(BANK_URLS) > 0:
    BANKS['bank-a'] = BANK_URLS[0]
if len(BANK_URLS) > 1:
    BANKS['bank-b'] = BANK_URLS[1]

print("Core url: %s" % ENKI_SERVER_URL)
print("Hydra urls: %s" % HYDRAS)

HYDRA_URLS = list(HYDRAS.values())
ALL_URLS = [u for u in [ENKI_SERVER_URL] + BANK_URLS + HYDRA_URLS if u]
HTTPS_URLS = [x for x in ALL_URLS if x.startswith("https")]
