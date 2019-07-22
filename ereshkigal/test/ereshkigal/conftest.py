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
import pytest

from datetime import datetime
from selenium import webdriver as wd
from selenium.webdriver.support.events import AbstractEventListener, EventFiringWebDriver

from . import logging_driver

CHROME_DRIVER_PATH = os.environ.get('CHROME_DRIVER_PATH', None)
SCREENSHOT_PATH = os.environ.get('SCREENSHOT_PATH', None)
ABOUT_BLANK = "about:blank"


class ScreenshotListener(AbstractEventListener):

    def screenshot(self, driver):
        short_filename = "%s-%s.png" % (
            datetime.now().isoformat(), driver.title)
        filename = os.path.join(SCREENSHOT_PATH, short_filename)
        driver.get_screenshot_as_file(filename)

    def before_click(self, element, driver):
        self.screenshot(driver)

    def after_click(self, element, driver):
        self.screenshot(driver)


@pytest.fixture(scope="module")
def webdriver_core():
    options = wd.ChromeOptions()
    options.add_argument('--no-sandbox')
    options.add_argument('--incognito')
    options.add_argument('--verbose')
    # Fix https://github.com/GoogleChrome/puppeteer/issues/1834
    options.add_argument('--disable-dev-shm-usage')

    if os.environ.get("DEMO_MODE", None) is None:
        options.headless = True

    kwargs = dict(
        options=options,
        service_args=["--log-level=ALL", "--log-path=chromedriver.log"]
    )
    if CHROME_DRIVER_PATH:
        kwargs['executable_path'] = CHROME_DRIVER_PATH

    if SCREENSHOT_PATH:
        driver = EventFiringWebDriver(
            wd.Chrome(**kwargs), ScreenshotListener())
    else:
        driver = wd.Chrome(**kwargs)

    print("Browser Version: {0}".format(driver.capabilities['version']))
    driver.set_window_size(1800, 1200)
    driver.get(ABOUT_BLANK)
    yield driver
    try:
        driver.close()
    except Exception as e:
        print("Ignoring exception while closing Chrome: %s" % e)


# This is split out mainly to reset the "start" timer, but keep using the
# same webdriver instance
@pytest.fixture(scope="function")
def webdriver(webdriver_core, request):
    driver = logging_driver.LoggingDriver(webdriver_core, request)
    yield driver
