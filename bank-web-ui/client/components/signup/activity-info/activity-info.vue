<template>
  <div>
      <vue-form-generator :model="subjectData"
                          :schema="schema"
                          :options="formOptions"
                          ref="activityInfoTabForm">
      </vue-form-generator>
      <div class="button-group">
        <button class="btn" v-on:click="show">UPLOAD</button>
      </div>
      <modal class="modal-content" name="uploadModal">
        <div class="modal-header">
          <p>Upload a document</p>
        </div>
        <upload-box ref="uploadBox" v-on:statusChanged="statusChanged"></upload-box>
          <div class="modal-buttons-group">
            <button class="btn-round btn-default btn-lg" v-on:click="addFiles" v-if="!uploadButtonHidden">+</button>
            <div class="upload-buttons">
              <button class="btn" v-on:click="hide" v-if="!uploadButtonHidden">Cancel</button>
              <button class="btn" v-on:click="upload" :disabled="uploadButtonDisabled" v-if="!uploadButtonHidden">Upload</button>
              <button class="btn" v-on:click="hide" v-if="uploadButtonHidden">Done</button>
            </div>
          </div>

      </modal>
  </div>
</template>

<style scoped lang="less">
  .v--modal-box {
    max-height: 600px;
  }
</style>
<script src="./activity-info.js"></script>
