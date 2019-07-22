<template>
  <!-- Component template content goes here -->
  <div class="container consent" id="consent-request-display">
    <div class="row">
      <div class="about-enki col-sm-8">
        <h3><span class="enki-logo">Enki</span> requests access to your personal information</h3>
        <div role="alert" v-if="error">
          <div class="alert alert-danger">
            <em>{{error.name}}</em>
            <p>{{error.message}}</p>
          </div>
        </div>
        <div class="inset">
          <p><span class="text-danger">{{error}}</span></p>
          <form role="form" action="/api/consent" method="post">
            <input type="hidden" name="_csrf" :value="csrfToken">
            <p>
              <span class="enki-consumer-logo">Enki</span> communicates securely with its partners to
              log what personal information of yours is held where, and how it's used. By continuing
              you will be sharing your data with Enki.
            </p>
            <span v-for="scope in scopes">
              <input name="allowed_scopes" :value="scope" type="hidden" :id="'scope-' + scope">
            </span>
            <p class="clearfix">
            <input type="hidden" name="challenge" :value="challenge" readonly="true"/>
            <button class="btn btn-primary pull-right" type="submit" id="submit">Continue</button>
            <a href="javascript:history.back()"><button class="btn btn-primary pull-left" type="submit" id="back"><span aria-hidden="true" class="glyphicon glyphicon-arrow-left"></span> Cancel</button></a>
            </p>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
  // your less rules go here. They are scoped to this particular component
  @enki-l: #5DBFCD;
  @enki-h: #89D8E3;
  @enki-d: #3BA4B3;
  @enki-text: #1E8B9A;

  .features-container{
    margin: 0 auto;
    ul{
      display:inline-block;

    }
  }
  .openid-logo input {
     margin-right: 10px;
  }

  fieldset.well {
    border-radius: 0px;
  }

  .about-enki {
    h3 {
    padding-bottom: 0.5em;
    }
    background-color:lighten(@enki-h, 15%);
    color: @enki-text;
    border-radius: 12px;
    padding: 0.3em 1em 1em 1em;
    font-size: 14px;
    margin: 2em 0 2em 0;
    .inset {
      background-color: #fff;
      border-radius: 12px;
      padding: 0.5em 1em 1em 1em;
      a {
        color: @enki-l;
        font-weight: bold;
      }
    }
  }
</style>

<!-- The view model goes on the separated file -->
<script src="./consent.js"></script>
