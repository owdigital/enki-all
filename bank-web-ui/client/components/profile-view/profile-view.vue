<template>
  <!-- Component template content goes here -->
  <div class="container profile-view">
    <h3>Profile view</h3>
    <p>The personal and contact information we have for you is as follows.</p>

    <div class="container">
      <div v-if="isLoggedIn && user" class="row">
        <div class="yes-button-group">
          <div class="enki-callout enki-callout-info">
            <h4 class="enki-logo">In partnership with Enki</h4>
            <p>ENKI makes banking transparent and safe. Save this account in Enki.</p>
            <div v-if="isConnectedToENKI" class="alert alert-success" id="enki-connect-success">
              <strong>Your account has been successfully saved in Enki.</strong>
            </div>
            <div v-if="connectionError" class="alert alert-danger" id="enki-connect-fail">
              <strong>An error occurred.</strong>
            </div>
            <div class="yes-button-wrapper">
              <button id="enki-connect" :disabled="isConnectedToENKI == true" class="btn enki-btn" v-on:click="connectToEnki()">Connect</button>
            </div>
          </div>
        </div>
        <div class="col-sm-12">
          <ul class="list-unstyled">
            <li v-for="(value, key) in user.piiData" v-if="key !== 'id'" class="form-group col-sm-6">
              <label :for="key" class="col-sm-1">{{normaliseKeyString(key)}}</label>
              <input :name="key" class="col-sm-5" readonly :value="value"></input>
            </li>
           </ul>
        </div>
      </div>

      <div v-if="!isLoggedIn" class="row">
        <p>Please log in to view your profile.</p>
        <p><a href="/#/login"><span class="glyphicon glyphicon-log-in" aria-hidden="true"></span>  Log in</a></p>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
  // your less rules go here. They are scoped to this particular component
  .features-container{
    margin: 0 auto;
    ul{
      display:inline-block
    }
  }
</style>

<!-- The view model goes on the separated file -->
<script src="./profile-view.js"></script>
