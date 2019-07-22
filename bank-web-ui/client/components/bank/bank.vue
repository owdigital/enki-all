<template>
<div class="row">
  <main class="site-main">
    <section class="hero_area">
      <div class="hero_content">
        <div class="container">

          <div class="row">
            <div class="col-sm-12">
              <h1 id="hero"><span class="glyphicon glyphicon-piggy-bank"></span> {{$store.getters.config.bankName}}</h1>
            </div>
          </div>
          <div class="row">

        <div v-if="isLoggedIn && user" class="col-sm-12">
          <div class="well well-sm">
            <div id="welcome-info">
              <h4>Hello {{user.piiData.firstName}} {{user.piiData.lastName}}, welcome back to {{bankName}}</h4>
              <p>Current account balance: 3.132,87 €</p>
              <p>Savings account: 6.450,00 €</p>
              <p v-if="user.isInEnki" class="enki-logo">Your account is connected to Enki</p>
              <p v-if="!user.isInEnki" class="enki-logo">You are currently not connected to Enki.</p>
            </div>
          </div>
        </div>
        <div v-else>
          <a href="/#/login" class="btn btn-large">Log in</a>
          <br/>
          <a href="/#/signup" class="btn btn-large">Open a bank account</a>
        </div>

          </div>

      </div>
      </div>
    </section>

  </main>
</div>
</template>

<script src="./bank.js"></script>
