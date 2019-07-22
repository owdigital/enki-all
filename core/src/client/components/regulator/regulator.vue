<template>
  <div v-if="isLoggedIn" class="panel panel-default enki-bg">
    <h3 class="enki-header">Regulator</h3>
    <p>What the regulator knows about you</p>
    <p v-for="service in services">
      <button 
        v-if="service.proof_url===null" class="btn btn-lg btn-primary" v-on:click="showLinkModal(service)">Link to {{service.name}}
      </button>
      <a :href="service.proof_url" v-else>Linked to <span :class="service.name">{{service.name}}</span></a>  
    </p>

  </div>
</template>

<style scoped>

.list-group-item.active {
  background: #f7f7f7;
  border-color: #f7f7f7;
  color: black;
}

.enki-bottom {
  margin-bottom: 20px;
}

.enki-top-bottom {
  margin-top: 20px;
  margin-bottom: 20px;
}

.enki-header {
}

.panel-heading {
  border-color: transparent;
  border-radius: 0px;
}

.enki-line {
  background: #d4d4d4;
  height: 2px;
}

div.content {
  all: revert;
}
</style>

<!-- The view model goes on the separated file -->
<script src="./regulator.js"></script>
