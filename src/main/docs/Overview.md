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

      Changes made here are automatically updated on s3 when they are saved, and data is regularly pulled from s3
      and reflected in the interface.  The UI is connected to s3 via the s3fs mount.

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


  - ##Deployment:
      This is simply a button that runs the equivalent of an aws sync command copying the data from the staging s3
      releases bucket to the production s3 releases bucket.  Once the data is copying lambda processes are triggered
      that automatically update the database. In this way the data is published to live. THIS MAY CURRENTLY TAKE A LONG
      TIME TO EXECUTE.  There are improvements that could be made to this process.

  - ##User Management:
      This section allows the user to create workspaces and control who has access.  It also allows users to be
       assigned ROLEs. This section is only available only to people with the correct ROLE.
