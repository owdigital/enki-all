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

from datetime import datetime
import selenium
import os.path
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support.ui import Select
import selenium.webdriver.support.expected_conditions as EC
import os
import time
from urllib.parse import urlparse

# Used to pause for demonstration configurations
pause = int(os.environ.get("DEMO_MODE", "0"))

SCREENSHOTS_FOLDER = os.path.join(os.path.dirname(os.path.realpath(__file__)), "screenshots")


class LoggingElement:
    def __init__(self, driver, element, selector):
        self.driver = driver
        self.element = element
        self.selector = selector

    def click(self, sleep=True):
        self.driver.log("Clicking on \"%s\" element" % self.selector)
        try:
            self.element.click()
            if sleep and (pause > 0):
                time.sleep(pause)
        except selenium.common.exceptions.ElementNotVisibleException:
            fname = self.driver.screenshot()
            raise Exception("Element for '%s' isn't visible. Screenshot is %s"
                            % (self.selector, fname))

    @property
    def text(self):
        return self.element.text

    def set_text(self, text):
        self.driver.log("Entering '%s' on '%s'" % (text, self.selector))
        self.element.clear()
        self.element.send_keys(text)

    def find_elements(self, kind, selector):
        self.driver.log("Finding multiple %s: %s" % (kind.title(), selector))
        return self.element.find_elements(kind, selector)

    def get_attribute(self, name):
        return self.element.get_attribute(name)


class LoggingAlert:
    def __init__(self, driver, alert):
        self.driver = driver
        self.alert = alert

    def dismiss(self):
        self.driver.log("Dismissing alert: '%s'" % self.alert.text)
        time.sleep(pause)
        self.alert.dismiss()


class FindMultiple:
    def __init__(self, selectors):
        self.selectors = selectors

    def __call__(self, loggingDriver):
        for (kind, selector) in self.selectors:
            elements = loggingDriver.driver.find_elements(kind, selector)
            if len(elements) > 0:
                return loggingDriver._single_element(selector, elements)
        return False


class WaitForURLPrefix:
    # Waits until the current url starts with the specified prefix
    def __init__(self, url):
        self.url = url

    def __call__(self, driver):
        return driver.current_url.startswith(self.url)


class element_attribute_has_value(object):
    def __init__(self, locator, attribute_name):
        self.locator = locator
        self.attribute_name = attribute_name

    def __call__(self, driver):
        element = driver.find_element(*self.locator)   # Finding the referenced element
        return element.get_attribute(self.attribute_name) != ""


class LoggingDriver:
    def __init__(self, driver, request):
        self.driver = driver
        self.start = datetime.now()
        self._no_logging_yet = True
        self.request = request

    def log(self, msg):
        if self._no_logging_yet:
            print("")
            self._no_logging_yet = False
        print("%.2f: %s" % ((datetime.now()-self.start).total_seconds(), msg))

    def screenshot(self):
        filename = os.path.join(SCREENSHOTS_FOLDER, "%s-%s.png"
                                % (datetime.now().isoformat(), self.request._pyfuncitem.name))
        self.driver.get_screenshot_as_file(filename)
        self.log("Took screenshot as %s at %s" % (filename, self.driver.current_url))
        return filename

    def get(self, url):
        self.log("Loading %s" % url)
        self.driver.get(url)
        if self.driver.current_url != url:
            self.log("Now at %s (not %s)" % (self.driver.current_url, url))
        time.sleep(pause)

    def close(self):
        self.log("Closing %s" % self.driver)
        return self.driver.close()

    def _single_element(self, selector, elements):
        if len(elements) == 0:
            self.screenshot()
            raise Exception("No element found for %s" % selector)
        elif len(elements) != 1:
            self.screenshot()
            raise Exception("Found %s elements for %s" % (len(elements), selector))
        else:
            return LoggingElement(self, elements[0], selector)

    def find_element(self, kind, selector):
        self.log("Finding %s: %s" % (kind.title(), selector))
        return self._single_element(selector, self.driver.find_elements(kind, selector))

    def find_elements(self, kind, selector):
        self.log("Finding multiple %s: %s" % (kind.title(), selector))
        return self.driver.find_elements(kind, selector)

    def find_select_elements(self, kind, selector):
        self.log("Finding multiple select %s: %s" % (kind.title(), selector))
        return [Select(x) for x in self.driver.find_elements(kind, selector)]

    def wait_elements(self, options, timeout_seconds=5):
        self.log("Waiting for one of: %s" % options)
        try:
            element = WebDriverWait(self, timeout_seconds).until(FindMultiple(options))
        except selenium.common.exceptions.TimeoutException:
            self.screenshot()
            raise
        self.log("Now on %s" % self.driver.current_url)
        return element

    def wait_element(self, kind, selector, timeout_seconds=5):
        self.log("Waiting for %s: %s" % (kind.title(), selector))
        try:
            element = WebDriverWait(self.driver, timeout_seconds).until(
                EC.presence_of_element_located((kind, selector))
            )
        except selenium.common.exceptions.TimeoutException:
            self.screenshot()
            raise
        self.log("Now on %s" % self.driver.current_url)
        return LoggingElement(self, element, selector)

    def wait_for_alert(self, timeout_seconds=5):
        self.log("Waiting for alert")
        try:
            WebDriverWait(self.driver, timeout_seconds).until(EC.alert_is_present())
            return LoggingAlert(self, self.driver.switch_to.alert)
        except selenium.common.exceptions.TimeoutException:
            self.screenshot()
            raise

    def wait_for_attribute_to_have_value(self, kind, selector, name, timeout_seconds=5):
        self.log("Waiting for the attribute '%s' of %s: %s to have a non-empty value" %
                 (name, kind.title(), selector))
        try:
            WebDriverWait(self.driver, timeout_seconds).until(
                element_attribute_has_value((kind, selector), name)
            )
        except selenium.common.exceptions.TimeoutException:
            self.screenshot()
            raise

    # Waits until the current url starts with the specified prefix
    def wait_for_url_prefix(self, url, timeout_seconds=5):
        parsed = urlparse(url)

        if parsed.port:
            rebuilt = "%s://%s:%s/%s" % (parsed.scheme, parsed.hostname, parsed.port, parsed.path)
        else:
            rebuilt = "%s://%s/%s" % (parsed.scheme, parsed.hostname, parsed.path)

        if rebuilt != url:
            self.log("Rebuilt %s as %s (no login)" % (url, rebuilt))
            url = rebuilt
        self.log("Waiting for URL to start with %s (currently %s)" % (url, self.url))
        try:
            WebDriverWait(self.driver, timeout_seconds).until(WaitForURLPrefix(url))
        except selenium.common.exceptions.TimeoutException:
            self.screenshot()
            self.log("Now at %s" % self.url)
            raise

    @property
    def url(self):
        return self.driver.current_url

    def delete_all_cookies(self):
        self.driver.delete_all_cookies()

    def assert_true(self, value):
        if not value:
            self.screenshot()
            raise Exception("Assertion failure")
