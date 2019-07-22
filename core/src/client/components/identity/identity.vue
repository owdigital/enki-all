<template>
  <div v-if="isLoggedIn" class="panel panel-default enki-bg">
    <h3 class="enki-header">Identity</h3>
    <p v-for="service in services">
      <button 
        v-if="service.proof_url===null" class="btn btn-lg btn-primary" v-on:click="showLinkModal(service)">Link to {{service.name}}
      </button>
      <a :href="service.proof_url" v-else>Linked to <span :class="service.name">{{service.name}}</span></a>  
    </p>

    <modal name="modal-edit" classes="modal-outer" :width="620" :adaptive="true">
      <form class="form-horizontal modal-dialog modal-edit" v-on:submit.prevent="editItem" v-if="editedItem">
        <div class="modal-content">
          <div class="modal-header"><h3>Link with {{editedItem.name}}</h3></div>
          <div class="modal-body">
            <p>To link with {{editedItem.name}}, past the following text into a public post, and copy the link to that post
            into the box below:</p>
            <pre>Enki link proof: {{getUUID()}}</pre>
            <div class="form-group">
              <label for="urlInput" class="col-sm-3 control-label">Link</label>
              <div class="col-sm-9">
                <input type="text" class="form-control" id="urlInput" placeholder="https://example.com/posts/32243" 
                v-focus v-model="editedItem.proof_url" required>
              </div>
            </div>

            <div class="modal-footer text-right">
              <button type="button" class="btn btn-lg btn-default" @click="hideLinkModal()">Cancel</button>
              <button type="submit" class="btn btn-lg btn-primary">Save</button>
            </div>
          </div>
        </div>
      </form>
    </modal>
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
<script src="./identity.js"></script>
