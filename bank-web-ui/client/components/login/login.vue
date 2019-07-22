<template>
  <!-- Component template content goes here -->
  <div class="row">
  <main class="site-main">
    <section class="hero_area">
      <div class="hero_content">
        <div class="container">
          <div class="row">
            <div class="col-sm-6">
            <form role="form" v-on:submit.prevent="login">
              <h1 class="form-signin-heading">Please log in</h1>
              <div class="alert alert-danger" role="alert" v-if="loginFailed">
                Log In failed due to wrong username or password
              </div>
              <div class="panel panel-default">
                <div class="panel-body">

                <div class="form-group has-success field-input">
                  <div class="field-wrap">
                    <label for="inputEmail" class="">Username</label>
                    <input required name="username" type="string" id="inputUsername" class="form-control" placeholder="Username" required autofocus v-model="username">
                    </div>
                  </div>

                <div class="form-group has-success field-input">
                  <div class="field-wrap">
                    <label for="inputPassword" class="">Password</label>
                    <input required name="password" type="password" id="inputPassword" class="form-control" placeholder="Password" required v-model="password">
                    <input id="returnUrl" name="returnUrl" type="hidden">
                  </div>
                </div>
                <button class="btn btn-lg btn-primary" type="submit" id="submit">Log in</button>
                </div>

              </div>
            </form>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>
  </div>
</template>

-->
<!-- The view model goes on the separated file -->
<script src="./login.js"></script>
