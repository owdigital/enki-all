<template>
  <div class="row">
  <div class="col-xs-12">
    <fieldset>
    <legend>Terms and conditions</legend>
    </fieldset>
  </div>
  <div class="col-xs-8">
    <div id="data-use-consent" class="well">
        <h2>Data use consent</h2>
        <p>These Terms will be governed by and interpreted in accordance with the Spanish law 15/99 on the protection
        of personal data</p>
        <h3>What will your data be used for?</h3>
        <p>Data facilitated during your application purposes. We may also use your information to help us develop and
        improve our products and services.</p>

        <h3>Details on verifying your identity and running anti-money laundering checks</h3>
        <p>Before we can open your account(s), in order to prevent or detect fraud, we will check the information
        provided during the application process with fraud prevention agencies, and may make searches at credit
        reference agencies who will supply us with information about your identity. Any data provided after the
        account opening will also be used to monitor your activity so as to prevent or detect fraud.</p>

        <p>If false or inaccurate information is provided and fraud identified, details will be shared with fraud
        prevention agencies. Additionally, in line with regulatory requirements, Government institutions and law
        enforcement agencies (such as the police force) may access and use this information to further investigate any
        suspicious activity detected. </p>

        <p>Further information on the credit reference agencies and fraud prevention agencies we use is available by
        calling at +XX XXXX XXX XXX</p>

        <h3>How will your data be protected?</h3>
        <p>We will make sure this information is only used in line with our instructions and will be kept safe. If we
        transfer your information to another country, we will also ensure its protection, given it will remain under
        Law 15/199. No data will be used without explicitly consent from your side or for commercial purposes.</p>
    </div>
    <div class="form-group contrast-form">
      <label class="checkbox-inline"><input id="checkBox" type="checkbox" v-model="acceptCheckBox"> I have read and fully agree with the Terms & Conditions</label>
    </div>
  </div>
  <div class="pad col-xs-4"></div>
    <div style="clear:both"></div>

    <modal name="activateEnkiModal">
      <div class="modal-wrapper" @click.stop>

        <div v-if="isFirstView">
          <button type="button" class="close close-button" @click="hideModalAndGoToNextTab" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>

          <div class="modal-header">
            <a class="modal-headline"><strong>Your bank account was created successfully</strong></a>
          </div>
          <div class="modal-body">
            <p>A notification has been sent to your email account. Would you like to save your personal data in Enki?</p>
            <div class="activate-buttons-group">
              <button class="wizard-btn btn-fill wizard-btn-wd" @click="saveToENKI" name="yesButton">Yes</button>
              <button class="wizard-btn btn-fill wizard-btn-wd" @click="hideModalAndGoToNextTab" name="noButton">No</button>
            </div>
          </div>
          <div class="modal-footer">
            <p class="enki-logo">
            <a :href="enkiInfoUrl" target="_blank">Learn more about Enki</a><br>
            </p>
          </div>
        </div>

        <div v-if="isSecondView">
          <button type="button" class="close close-button" @click="hideModal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>

          <div class="modal-header">
            <a class="modal-headline"><strong>{{postResultHeader}}</strong></a>
          </div>
          <div class="modal-body">
            <p>{{postResultText}}</p>
            <p>{{postResultDescription}}</p>
            <div class="activate-buttons-group">
              <button class="wizard-btn btn-fill wizard-btn-wd" @click="hideModal" name="doneButton">Ok</button>
            </div>
          </div>
        </div>
      </div>
    </modal>
  </div>
</template>

<script src="./terms-and-conditions.js"></script>

<style scoped lang="less">
div#data-use-consent.well {
  padding-bottom: 0;
  margin-bottom: 0;
}
.activate-buttons-group {
  margin-top: 30px;
}

.register-link {
  text-align: center;
  color: #8c8c8c;
  text-decoration: underline;
}

.modal-wrapper {
  min-width: 392px;
  min-height: 192px;
}
.close-button {
  margin-right: 10px;
  margin-top: 17px;
  -webkit-align-self: flex-end;
  align-self: flex-end;
}
.form-control label {
  padding-left: 10px;
  font-weight: bold;
  line-height: auto;
  color: inherit;
}

.checkbox-inline {
  width: 100%;
}
</style>
