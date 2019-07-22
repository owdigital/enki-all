<template>
  <header class="" id="">
    <div id="home-intro">
      <div class="panel panel-default enki-bg">
        <h2>Welcome to Enki</h2>
        <div v-show="!isLoggedIn">
          <h3>Control access to your personal information</h3>
          <ul class="about-enki">
            <li>Easily and securely access your data and documents through one gateway</li>
            <li>Manage who has your data</li>
            <li>Monitor how and when your data is being used</li>
            <li>Save time in future banking operations with your bank and other organisations</li>
          </ul>


              <a id="register-link" href="/register" class="btn btn-outline-info">Sign up</a>
              or
              <a id="login-link" href="/login" class="btn btn-outline-info">Sign in</a>
            </div>

        <div v-show="isLoggedIn">
          <h3>Link accounts</h3>
          <p>If you have any accounts with our partners, get the most out of our services by linking them up  </p>
          <div class="features-container">
              <p v-for="bank in banks" :key="bank.name">
                <a :href="getFullLinkUrl(bank.name)" v-if="!linkedBanks.includes(bank.id)" :id="'link-'+bank.name" class="btn btn-outline-info">Link with <span :class="bank.name">{{bank.name}}</span></a>
                <a href="#" class="btn btn-outline-info disabled" v-else>Linked to <span :class="bank.name">{{bank.name}}</span></a>
              </p>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped lang="scss">

  .features-container{
    margin: 0 auto;
    ul{
      display:inline-block;
      a {
        text-decoration: underline;
      }
    }
  }
  .about-enki {
    padding-left: 0;
    margin-bottom: 1.4em;
    li{
      margin-left: 1.4em;
      }
  }
</style>
<!-- The view model goes on the separated file -->
<script src="./home.js"></script>
