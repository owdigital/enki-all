<template>

    <header class="site-header">
      <div class="top">
        <div class="container">
          <div class="row">
            <div class="col-sm-6">
              <p><!--Strap line--></p>
            </div>
            <div class="col-sm-6">
            </div>
          </div>
        </div>
      </div>

      <nav class="navbar navbar-default">
        <div class="container">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-navbar-collapse">
            <span class="sr-only">Toggle Navigation</span>
            <span class="glyphicon glyphicon-menu-hamburger"></span>
          </button>
          <a href="/" class="navbar-brand">
              <h3><span class="glyphicon glyphicon-piggy-bank"></span> {{$store.getters.config.bankName}}</h3>
          </a>
          <div class="collapse navbar-collapse" id="bs-navbar-collapse">
            <ul class="nav navbar-nav">
              <router-link tag="li" v-for="route in routes" v-if="!route.omitFromNav" v-show="!(route.omitIfLoggedIn && isLoggedIn)" :id="route.name" :key="route.name" :to="{ name: route.name }"><a>{{route.linkText}}</a></router-link>
            </ul>

            <ul class="nav navbar-nav navbar-right">
              <li><a href="#" v-on:click.prevent="logout" v-show="isLoggedIn"><span class="glyphicon glyphicon-log-out" aria-hidden="true"></span> Log out {{username}}</a></li>
              <router-link tag="li" v-show="!isLoggedIn" id="login-link" :to="{ name: 'login' }"><a><span class="glyphicon glyphicon-log-in" aria-hidden="true"></span> Log in</a></router-link>
              <li>
                <a href="#">Language
                  <flag squared iso="es"></flag>
                  <flag squared iso="gb"></flag>
                </a>
              </li>
            </ul>
          </div>

        </div>
      </nav>

      <transition name="nav-slide">
        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-navbar-collapse">
          <ul class="nav navbar-nav main-navbar-nav" data-bind="foreach: { data: navActivePrimaryPage().children, as: 'subpage' }">
            <router-link tag="li" v-for="route in childRoutes" v-if="!route.omitFromNav"
                         :to="{ name: route.name }" :key="route.name"><a>{{route.linkText}}</a></router-link>
          </ul>

        </div>
      </transition>

    </header>

</template>

<style scoped lang="less">
  @import "../../css/variables";
  .flag-icon {
    width: 1.5em;
  }

  </style>

<script src="./ow-navbar.js"></script>
