<template>

  <div class="">
    <ul class="navbar-nav">
      <li class="nav-item">
        <a class="nav-link" :href="getLoginStatusAction" @mouseover="hover" @mouseout="hoverOut" id="navbar-login-link">
          <span class="fa fa-sign-out" aria-hidden="true"></span>
          {{getLoginStatusText}}
        </a>
      </li>
    </ul>

    <div class="user-popover" v-show="shouldShowPopup" transition="fade" @mouseover="hoverActive" @mouseout="hoverInactive">
      <p><b>Login:</b> {{ user.name }}</p>
      <p><b>Id:</b> {{ user.id }}</p>
    </div>

  </div>

</template>

<style scoped lang="scss">

  .inline {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    justify-content: flex-start;
    margin-top: 5px;
  }

  .navbar-collapse.collapse.in .inline {
      align-items: flex-start;
  }

  .user-popover {
    position: absolute;
    min-width: 200px;
    max-width: 500px;
    background: #fff;
    border: 1px solid #85d6e1;
    padding: 5px 10px 0px 10px;
    box-shadow: 0 6px 6px rgba(16, 16, 16, 0.04), 0 6px 6px rgba(0, 0, 0, 0.05);
    margin-top: 35px;
    z-index:999;

    p {
      color: #807777;
    }
  }
</style>

<script src="./user-status-button.js"></script>
