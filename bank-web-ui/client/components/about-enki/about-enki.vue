<template>
  <!-- Component template content goes here -->
  <div class="container">
    <div class="about-enki col-sm-8">
      <h3>What is <span class="enki-logo">Enki</span>?</h3>
      <div class="inset">
        <p>Enki is a service created to make your online experience more convenient and secure.<p>

        <p>With Enki, you will be able to keep all the information you need for your banking needs
        in a secure location. Organisations will be able to access data uploaded to Enki only with
        your explicit permission.</p>

        <p>With Enki you will be able to:</p>

        <ul>
        <li>Store your data and documents in one place</li>
        <li>Manage who can access your data</li>
        <li>Monitor the usage of and access to your data</li>
        <li>Save time in future with your bank, other financial institutions and organisations you choose.</li>
        </ul>

        <!-- todo: how do we link to enki on localdev/stage/production -->
        <p><a href="#">Find out more</a></p>
        <p><a href="javascript:history.back()"><span aria-hidden="true" class="glyphicon glyphicon-arrow-left"></span> Go back</a></p>
      </div>
    </div>

    </div>
  </div>
</template>

<style scoped lang="less">
  @import "../../css/brand-colors";

  .features-container{
    margin: 0 auto;
    ul{
      display:inline-block
    }
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
