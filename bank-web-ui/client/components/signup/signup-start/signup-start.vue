<template>
  <div>
    <div class="existing-account">

        <div class="yes-button-group">
          <div class="enki-callout enki-callout-info">
            <h4 class="enki-logo">In partnership with Enki</h4>
            <p>If you have an existing Enki account that we could access to download your information please click 'Yes' below.</p>
            <div class="yes-button-wrapper pull right">
              <a :href="getOauthUrl()" id="use-enki-data-btn" class="btn enki-btn">Yes</a>
            </div>
            <p>Otherwise, please enter your details in the following form.</p>
          </div>

      </div>
    </div>
    <vue-form-generator :model="subjectData"
                        :schema="schema"
                        :options="formOptions"
                        ref="signupStartTabForm">
    </vue-form-generator>
  </div>
</template>

<style scoped lang="less">

.modal-buttons-login {
  display: flex;
  justify-content: space-between;
  width: 80%;
  margin: 12px 0px 6px 0px;
}

</style>

<script src="./signup-start.js"></script>
