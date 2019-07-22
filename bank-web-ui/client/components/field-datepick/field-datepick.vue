<!-- Custom Datepicker using Vue datepicker
     https://icebob.gitbooks.io/vueformgenerator/content/fields/custom_fields.html
-->

<template>
  <div class="wrapper">
    <datepicker
      v-model="value"
      type="text"
      :maxlength="schema.max"
      :initial-view="schema.initialView"
      :readonly="schema.readonly">
    </datepicker>
  </div>
</template>

<script src="./field-datepick.js"></script>