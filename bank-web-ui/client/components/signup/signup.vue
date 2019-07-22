<template>
  <!-- Component template content goes here -->
  <div class="container text-center signup">
    <form-wizard @on-complete="onComplete"
                 error-color="#a94442"
                 title=""
                 subtitle=""
                 v-if="bankTheme"
                 ref="formWizard">
      <tab-content title="Signup start" :before-change="validateSignupStartTab">
        <signup-start :subjectData.sync="model" :theme="bankTheme" ref="signupStartForm"></signup-start>
      </tab-content>
      <tab-content title="Confirm your identity" :before-change="validatePersonalDataTab">
        <personal-data :subjectData.sync="model"  :theme="bankTheme" ref="personalDataForm"></personal-data>
      </tab-content>
      <tab-content title="Activity Information" :before-change="validateActivityInfoTab">
        <activity-info :subjectData.sync="model"  :theme="bankTheme" ref="activityInfoForm"></activity-info>
      </tab-content>
      <tab-content title="Purpose of relationship" :before-change="validatePurposeOfRelationshipTab">
        <purpose-of-relationship :subjectData.sync="model"  :theme="bankTheme" ref="purposeOfRelationshipForm"></purpose-of-relationship>
      </tab-content>
      <tab-content title="Terms and conditions" :before-change="validateTermsAndConditionsTab">
        <terms-and-conditions ref="termsAndConditionsForm" :subjectData.sync="model" :originalData="originalData" @tandcFinished="$refs.formWizard.nextTab()"></terms-and-conditions>
      </tab-content>
      <tab-content title="Done">
      <div class="well">
        <h4 id="thank-you">Thank you for signing up</h4>
        <p>Complete the process with the 'Complete sign up' button below and look out for an email containing your account details</p>
        <div class="panel panel-info" style="display:none">
          <div class="panel-heading">
            <a href="#debug" data-toggle="collapse">Debug: view json</a>
          </div>
          <div id="debug" class="panel-body collapse">
            <transition name="fade" mode="out-in">
              <pre id="debug-info" v-html="prettyJSON(model)"></pre>
            </transition>
          </div>
        </div>
      </div>
      </tab-content>
      <template slot="footer" slot-scope="props">
       <div class="wizard-footer-left">
           <wizard-button v-if="props.activeTabIndex > 0 && !props.isLastStep" @click.native="props.prevTab()" :style="props.fillButtonStyle">Back</wizard-button>
        </div>
        <div class="wizard-footer-right">
          <wizard-button v-if="!props.isLastStep" @click.native="props.nextTab()" id="next-button" class="wizard-footer-right" :style="props.fillButtonStyle">Next</wizard-button>
          <wizard-button v-else @click.native="props.nextTab()" id="finish-button" class="wizard-footer-right finish-button" :style="props.fillButtonStyle">{{props.isLastStep ? 'Complete sign up' : 'Next'}}</wizard-button>
        </div>

      </template>
    </form-wizard>

  </div>
</template>

<style scoped lang="less">
  @import "../../css/variables";

  .text-center {
    text-align: left;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s
  }
  .fade-enter, .fade-leave-to {
    opacity: 0
  }

  </style>

<!-- The view model goes on the separated file -->
<script src="./signup.js"></script>
