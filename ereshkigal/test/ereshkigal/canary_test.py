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

import pytest
from urllib.parse import urljoin
import requests
import ereshkigal as e
from selenium.webdriver.common.by import By


@pytest.mark.skipif(e.ENKI_SERVER_URL is None,
                    reason="$ENKI_SERVER_URL not specified")
def test_enki_core_healthcheck():
    enki_server_healthcheck = urljoin(e.ENKI_SERVER_URL, "/api/healthcheck")
    # We need to use requests not webdriver, as Chrome tries to download the
    # JSON file
    r = requests.get(enki_server_healthcheck, verify=False, auth=('enki', 'Veyhyptucid5'))
    print((r.status_code, r.headers, r.text))
    r.raise_for_status()
    assert r.json()["status"] == "ok", r.text


@pytest.mark.skipif(not e.BANK_URLS, reason="$BANK_URLS not specified")
@pytest.mark.parametrize("bank_url", e.BANK_URLS)
def test_bank_healthchecks(webdriver, bank_url):
    webdriver.get(bank_url)
    webdriver.screenshot()
    root = webdriver.find_element(By.CSS_SELECTOR, "html")
    assert "Enki" in root.text or \
        "Lorem ipsum" in root.text or \
        "Bank" in root.text


@pytest.mark.skipif(e.HYDRAS == {},
                    reason="HYDRAS is empty")
@pytest.mark.parametrize("hydra_url", e.HYDRA_URLS)
def test_hydra_healthcheck(webdriver, hydra_url):
    hydra_healthcheck = urljoin(hydra_url, "/health")
    webdriver.get(hydra_healthcheck)
    root = webdriver.find_element(By.CSS_SELECTOR, "html")
    assert "ok" in root.text
