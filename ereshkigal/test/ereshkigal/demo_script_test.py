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

import diceware
# import itertools
import pytest
import ereshkigal as e
from selenium.webdriver.common.by import By

# We don't want to include phone number as it is not common for both banks
BANK_B_FIELDS = {"First Name", "Last Name", "Email", "Document number", "Nationality",
                 "City", "Document type", "Residence address", "Gender",
                 "Post code", "Birth date", "Province"}
BANK_A_FIELDS = BANK_B_FIELDS

preId = "enki-su-"


def passphrase():
    return diceware.get_passphrase(diceware.handle_options(["--delimiter", "-", "--no-caps"]))


def create_non_common_fields(wd):
    username = passphrase()
    password = passphrase()
    wd.find_element(By.ID, preId + "username").set_text(username)
    wd.find_element(By.CSS_SELECTOR, "div#password-wrapper input.form-control").set_text(password)
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click()

    wd.wait_element(By.ID, preId + "residenceAddress")
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click()

    wd.wait_element(By.ID, preId + "companyName")
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click()

    wd.wait_element(By.CSS_SELECTOR, "label[for=\"" + preId + "depositChecks\"]")
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click()

    wd.wait_element(By.ID, "data-use-consent")
    wd.find_element(By.ID, "checkBox").click()
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click()

    wd.wait_element(By.CSS_SELECTOR, "button[name=noButton]").click()

    wd.wait_element(By.ID, "thank-you")
    wd.find_element(By.CSS_SELECTOR, "button#finish-button").click()

    return {
        "username": username,
        "password": password}


def create_bank_user(wd, url):
    username = passphrase()
    password = passphrase()

    firstName = passphrase()
    lastName = passphrase()
    email = "%s@example.org" % (firstName, )
    docNo = "123456789"
    nationality = "Spanish"
    province = "Madrid"
    currentCity = "Madrid"
    documentType = "Passport"
    currentAddress = "Some Address"
    gender = "Female"
    postCode = "AA1 1AA"
    wd.get(url)
    wd.wait_element(By.CSS_SELECTOR, "li#signup > a").click(False)

    # Signup Start form
    wd.wait_element(By.ID, preId + "username").set_text(username)
    wd.find_element(By.CSS_SELECTOR, "div#password-wrapper input.form-control").set_text(password)
    wd.find_element(By.ID, preId + "firstName").set_text(firstName)
    wd.find_element(By.ID, preId + "lastName").set_text(lastName)
    wd.find_element(By.ID, preId + "email").set_text(email)
    wd.find_element(By.ID, preId + "docno").set_text(docNo)
    wd.find_select_elements(By.ID, preId + "nationality")[0].select_by_visible_text(nationality)
    wd.find_element(By.CSS_SELECTOR, "input[name=\"documentType\"][value=\"Passport\"]")\
      .click()
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click(False)

    # Your personal data form
    wd.wait_element(By.ID, preId + "residenceAddress").set_text(currentAddress)
    wd.find_select_elements(By.ID, preId + "province")[0].select_by_visible_text(province)
    wd.find_element(By.ID, preId + "zipCode").set_text(postCode)
    wd.find_element(By.ID, preId + "city").set_text(currentCity)
    wd.find_element(By.CSS_SELECTOR, "input[name=\"gender\"][value=\"Female\"]").click(False)

    #  Handle Birthdata field
    wd.find_element(By.CSS_SELECTOR, ".vdp-datepicker input").click(False)
    wd.wait_element(By.CSS_SELECTOR, ".cell.year").click()
    wd.wait_element(By.CSS_SELECTOR, ".cell.month").click()
    wd.wait_element(By.CSS_SELECTOR, ".cell.day:not(.blank)").click()
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click(False)

    # Activity information form
    wd.wait_element(By.ID, preId + "companyName")
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click()

    # Purpose of relationship form
    wd.wait_element(By.CSS_SELECTOR, "label[for=\"" + preId + "depositChecks\"]")
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click()

    # T&C form
    wd.wait_element(By.ID, "data-use-consent")
    wd.find_element(By.ID, "checkBox").click(False)
    wd.find_element(By.CSS_SELECTOR, "button#next-button").click(False)
    wd.wait_element(By.CSS_SELECTOR, "button[name=noButton]").click()

    # Done form
    wd.wait_element(By.ID, "thank-you")
    wd.find_element(By.CSS_SELECTOR, "button#finish-button").click()

    ret = {"username": username, "password": password}
    ret["firstName"] = firstName
    ret["lastName"] = lastName
    ret["email"] = email
    ret["documentNumber"] = docNo
    ret['nationality'] = nationality
    ret['province'] = province
    ret['documentType'] = documentType
    ret['currentAddress'] = currentAddress
    ret['gender'] = gender
    ret['postCode'] = postCode
    ret['currentCity'] = currentCity
    return ret


def login_to_enki(wd, user):
    # Might already be logged in, so need to clear cookies
    wd.get(e.ENKI_SERVER_URL)
    wd.delete_all_cookies()
    # ... and then reload to get non-logged in page
    wd.get(e.ENKI_SERVER_URL)

    login_link = wd.find_element(By.ID, "navbar-login-link")
    assert 'Sign in' == login_link.text
    login_link.click()
    wd.wait_element(By.ID, "username").set_text(user["username"])
    wd.find_element(By.ID, "inputPassword").set_text(user["password"])
    wd.find_element(By.ID, "submit").click()


def connect_bank_to_enki(wd, user):
    wd.find_element(By.CSS_SELECTOR, "li#login-link > a").click()
    wd.wait_element(By.ID, "inputUsername").set_text(user["username"])
    wd.find_element(By.ID, "inputPassword").set_text(user["password"])
    wd.find_element(By.ID, "submit").click()
    wd.wait_element(By.ID, "welcome-info")

    wd.find_element(By.CSS_SELECTOR, "li#profile-view > a").click()
    wd.wait_element(By.ID, "enki-connect").click()
    element = wd.wait_elements(options=[
        (By.ID, "enki-connect-success"),
        (By.ID, "enki-connect-fail")],
        timeout_seconds=10)
    if element.selector != "enki-connect-success":
        wd.screenshot()
        raise Exception

    # So that we can re-login later on
    wd.delete_all_cookies()


def create_enki_user(wd):
    username = passphrase()
    password = passphrase()

    wd.get(e.ENKI_SERVER_URL)
    wd.find_element(By.ID, "register-link").click()

    wd.wait_element(By.ID, "username").set_text(username)
    wd.find_element(By.ID, "inputPassword").set_text(password)
    wd.find_element(By.ID, "inputPasswordAgain").set_text(password)
    wd.find_element(By.ID, "submit").click()
    return {"username": username, "password": password}


def connect_enki_user_to_bank_user(wd, bank_user, enki_user, bank, bank_url):
    login_to_enki(wd, enki_user)
    wd.wait_element(By.ID, "link-bank-" + bank).click()
    wd.wait_for_url_prefix(bank_url)
    wd.wait_element(By.ID, "inputUsername").set_text(bank_user["username"])
    wd.find_element(By.ID, "inputPassword").set_text(bank_user["password"])
    wd.find_element(By.ID, "submit").click()

    wd.wait_element(By.ID, "consent-request-display")

    # Because otherwise Vue hasn't filled it in yet sometimes
    wd.wait_for_attribute_to_have_value(By.NAME, "_csrf", "value")

    alerts = wd.find_elements(By.CLASS_NAME, "alert-danger")
    wd.assert_true(len(alerts) == 0)

    wd.find_element(By.ID, "submit").click()

    wd.wait_for_url_prefix(e.ENKI_SERVER_URL)
    wd.assert_true(wd.driver.current_url.find("error") == -1)


def extract_vue_table(table):
    items = []
    headers = [h.text for h in
               table.find_elements(By.CSS_SELECTOR, "thead tr:first-of-type th span")]
    rows = table.find_elements(By.CSS_SELECTOR, "tbody tr")
    for r in rows:
        columns = r.find_elements(By.CSS_SELECTOR, "td")
        mapped = dict(zip(headers, [c.text for c in columns]))
        items.append(mapped)
    return items


def show_all_entries_in_table(wd):
    items = wd.find_select_elements(By.CSS_SELECTOR, ".table-footer select")
    for item in items:
        item.select_by_visible_text("All")


def check_one_bank_assertions(wd):
    wd.get(e.ENKI_SERVER_URL)
    wd.wait_element(By.ID, "profile-view-navbar").click()

    wd.wait_element(By.ID, "bank-table")
    wd.find_select_elements(By.ID, "bank-dropdown")[0].select_by_visible_text('bank-a')
    show_all_entries_in_table(wd)

    bank_table = extract_vue_table(wd.find_element(By.ID, "bank-table"))
    assert set([x["Detail"] for x in bank_table]) == BANK_A_FIELDS, bank_table

    wd.wait_element(By.ID, "pii-type-table")
    for pii_type in BANK_A_FIELDS:
        wd.find_select_elements(By.ID, "pii-type-dropdown")[0].select_by_visible_text(pii_type)
        show_all_entries_in_table(wd)
        pii_type_table = extract_vue_table(wd.find_element(By.ID, "pii-type-table"))
        assert len(pii_type_table) == 1, pii_type_table

        for p in pii_type_table:
            assert p['Who'] == 'bank-a', p
            assert p['Purpose'] == 'For a bank account', p


def check_two_banks_assertions(wd):
    wd.get(e.ENKI_SERVER_URL)
    wd.wait_element(By.ID, "profile-view-navbar").click()

    wd.wait_element(By.ID, "bank-table")

    wd.find_select_elements(By.ID, "bank-dropdown")[0].select_by_visible_text('bank-a')
    show_all_entries_in_table(wd)
    bank_table = extract_vue_table(wd.find_element(By.ID, "bank-table"))
    assert len(bank_table) == len(BANK_A_FIELDS)
    assert set([x["Detail"] for x in bank_table]) == BANK_A_FIELDS, bank_table

    wd.find_select_elements(By.ID, "bank-dropdown")[0].select_by_visible_text('bank-b')
    show_all_entries_in_table(wd)
    bank_table = extract_vue_table(wd.find_element(By.ID, "bank-table"))
    assert len(bank_table) == len(BANK_B_FIELDS)
    assert set([x["Detail"] for x in bank_table]) == BANK_B_FIELDS, bank_table

    wd.wait_element(By.ID, "pii-type-table")
    for pii_type in BANK_A_FIELDS:
        wd.find_select_elements(By.ID, "pii-type-dropdown")[0].select_by_visible_text(pii_type)
        show_all_entries_in_table(wd)
        pii_type_table = extract_vue_table(wd.find_element(By.ID, "pii-type-table"))
        assert len(pii_type_table) == 2, pii_type_table

        for p in pii_type_table:
            assert (p['Who'] == 'bank-a') or (p['Who'] == 'bank-b'), p
            assert p['Purpose'] == 'For a bank account', p


def use_existing_enki_data(wd, bank_user, url):
    wd.get(url)
    wd.find_element(By.CSS_SELECTOR, "li#signup > a").click()
    button = wd.wait_element(By.ID, "use-enki-data-btn")
    print("OAuth URL: {}".format(button.get_attribute("href")))
    button.click()
    consent_container = wd.wait_element(By.CLASS_NAME, "consent-container")
    sel = "ul.consent-fields li.fieldSource input[type='radio']:not([value='']):not([disabled])"
    consents = consent_container.find_elements(By.CSS_SELECTOR, sel)
    assert len(consents) > 0, "Should have found some consent fields?"
    for bank_option in consents:
        print("option: {} → {}".format(
            bank_option.get_attribute("name"),
            bank_option.get_attribute("value")))
        if bank_option.get_attribute("disabled"):
            print("Skipping disabled option: {} → {}".format(
                bank_option.get_attribute("name"),
                bank_option.get_attribute("value")))
            continue

        assert bank_option.get_attribute("value") != ""
        bank_option.click()
    wd.find_element(By.CSS_SELECTOR, "input[type=submit]").click()

    wd.wait_for_url_prefix(url)
    firstNameId = preId + "firstName"
    lastNameId = preId + "lastName"
    assert wd.wait_element(By.ID, firstNameId).get_attribute("value") == bank_user["firstName"]
    assert wd.find_element(By.ID, lastNameId).get_attribute("value") == bank_user["lastName"]
    assert wd.wait_element(By.ID, preId + "email").get_attribute("value") == bank_user["email"]

    ret = create_non_common_fields(wd)
    ret["firstName"] = bank_user["firstName"]
    ret["lastName"] = bank_user["lastName"]
    return ret


@pytest.mark.skipif(not e.BANKS.get('bank-a'), reason="BANK-A URL is not specified.")
@pytest.mark.skipif(not e.BANKS.get('bank-b'), reason="BANK-B URL is not specified.")
@pytest.mark.skipif(not e.ENKI_SERVER_URL, reason="Enki-core URL is not specified.")
def test_demo_script(webdriver):
    bank_a_url = e.BANKS['bank-a']
    bank_b_url = e.BANKS['bank-b']
    bank_a_user = create_bank_user(webdriver, bank_a_url)
    connect_bank_to_enki(webdriver, bank_a_user)
    enki_user = create_enki_user(webdriver)
    connect_enki_user_to_bank_user(webdriver, bank_a_user, enki_user, "a", bank_a_url)
    check_one_bank_assertions(webdriver)

    bank_b_user = use_existing_enki_data(webdriver, bank_a_user, bank_b_url)
    connect_bank_to_enki(webdriver, bank_b_user)
    connect_enki_user_to_bank_user(webdriver, bank_b_user, enki_user, "b", bank_b_url)
    check_two_banks_assertions(webdriver)
