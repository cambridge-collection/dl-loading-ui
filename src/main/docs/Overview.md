#Overview of the Loading UI

It is still in development so there will be sections that are missing and there is still a fair amount of work to do.
This application is designed to allow users to add/edit collection and items within their Digital Library in a simple
interface.

Once they are happy with the changes they can package them up and release them into one or more of their chosen Digital
Library instances.

Authentication is using SAML2 and for local testing requires an Idp to connect to.  See the README for more details on
this.

##Sections:

  - ##Editing
      This section allows the user to view the collections and items that they are allowed to edit. What they can see
      here depends on the ROLEs they have been assigned, and what workspaces they have access to.

      Changes made here are automatically committed to git when they are saved, and data is regularly pulled from git
      and reflected in the interface.  More work needs to be done on the handling of conflicts, as at the moment these
      will need to be resolved manually in git.

      The data that is being edited is specified in the 'git.sourcedata.url' and should be in the format specified in
      the schema at: https://bitbucket.org/CUDL/cudl-package-schemas/src/master/JSON-package-format/

      There is one version of the content that is being edited so that if an item is in more than one collection, or
      more than one person is editing the same item their changes may be overwritten.

      If an item has been removed from a collection, and is no longer in any other collection the data for that item is
      deleted.  Items cannot be reordered at the moment in this interface.

      Collections at the moment cannot be deleted, or reordered in this interface.

      The HTML editing is implemented using CKEditor. At the moment the collection HTML can be edited but the HTML for
      the FAQ, help, news etc pages cannot be edited through this interface.

      We wanted this to be an external API however at the moment this functionality is rolled into the dl-loading-ui,
      but can be separated out in future.

  - ##Packaging:
      This section allows the user to package content for release. This should be available only to people with the
      correct ROLE.

      It will show the user a summary of all the edits that have been made to the source data from all of the users
       since the last package.  You can then choose if you want to build a new package.

      It currently works by triggered a pipeline to run on the BitBucket instance where the source data is.  That
      pipeline is setup to use the cudl-pack (https://bitbucket.org/CUDL/cudl-pack/) application to package up the
      source data into a format that is suitable for the CUDL-Viewer to ingest. Note: that this format may change
      and is designed to be an internal format for the CUDL-Viewer, so any changes should not matter so long as the
      package version and the cudl-viewer version are in sync.

      It uses the Bitbucket API to interact with the pipeline.  At the moment logs for the status of the packaging
       process from bitbucket are not displayed by the interface but these can be viewed on the bitbucket site. The
        API does allow the reading of these logs but the display has just not yet been implemented.

      This can easily be setup as an external API instead of using BitBucket pipelines. It would just need to be on a
      server that could run the commands listed in:
        https://bitbucket.org/CUDL/mdiv-data-source/src/master/bitbucket-pipelines.yml

      Once the packaging is complete a new tag should be created for the source data, and for the packaged data. These
      tags should match.

  - ##Deployment:
      This section allows the user to view and release content to any of the configured servers/instances (e.g. dev
      , staging, live). This should be available only to people with the correct ROLE.

      Deployment uses the deployment API which is a separate application available here:
      https://bitbucket.org/CUDL/dl-deployment-api/

      This publishes a swagger api, available on swagger-ui.html where it is deployed.  This allows functions to view
      the instances available (read from a database), details of each instance and status. It also allows you to
      deploy a new instance.

      The API is polled for updates, but we did want to implement a system where it was possible to subscribe to
      changes in an instance (for example after you have made a deployment) but this is not currently implemented,
      although you can see it in the swagger API.

      When the user navigates to deployment they can see a list of instances available and the currently deployed
      data tag for each instance.  If the user want to deploy a new version they can select that version from a drop
      down list of version (which correspond to the tags on the source-data/packaged-data git repos).

      When they select a new version and confirm deployment, the deployment API is sent a request to deploy a new
      version of the data, which updates the database which holds instances->version mappings. Then this triggers
      a Puppet Kick operation to remotely trigger a puppet run on that machine.

      That puppet run will look at the database, and see if the version of data it has currently got installed
      matches the version in the database.  If they are different it checks out the required version from the
      packaged-data repository and deploys that (restarting cudl-viewer etc if required).

      The version of the data currently deployed to the servers is checked through the deployment API (this uses
      files stored on the server that record the currently deployed version).

      Note: When configuring a server, you may need to check that the required status files are available through
      apache.

      There is still work to be done on the display for this section, and especially on the errors when not connected
      to a correctly setup instance.

      Once the puppet run is completed the new data should be visible on the server/instance.

  - ##User Management:
      This section allows the user to create workspaces and control who has access.  It also allows users to be
       assigned ROLEs. This section should be available only to people with the correct ROLE.

      To be implemented.
