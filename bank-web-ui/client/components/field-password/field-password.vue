<!-- field-password.vue -->
<template>
  <div class="wrapper" id="password-wrapper">
    <vue-password
      type="text"
      v-model="value"
      :maxlength="schema.max"
      :placeholder="schema.placeholder"
      :readonly="schema.readonly" >
    </vue-password>
  </div>
</template>

<script src="./field-password.js"></script>