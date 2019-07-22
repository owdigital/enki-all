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

import ereshkigal as e
import pytest
import http.client
from urllib.parse import urlparse


@pytest.mark.parametrize("https_url", e.HTTPS_URLS)
def test_ssl(https_url):
    parsed = urlparse(https_url)
    c = http.client.HTTPSConnection(parsed.hostname, port=parsed.port)
    c.request("GET", "/")
