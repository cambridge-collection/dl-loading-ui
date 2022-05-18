#Advanced CUDL Loader Use

##Setup

The new loading process allows an advanced user to setup a direct connection to the s3 bucket that holds the tei data etc to make multiple changes at once.  This involves using the command line and requires the following software installed:

- [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)

- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)

You will also need accounts and permission to access the following

- Bitbucket (https://bitbucket.org/CUDL/cudl-data-source/)

- AWS (staging-cudl-data-source bucket read/write/list access)

Once this is setup you should check out the git repository into your local file system using e.g:

        git clone https://<yourusename>@bitbucket.org/CUDL/cudl-data-source.git

This will create the directory "cudl-data-source" which inside has the "data" directory containing the site data for the digital library.  Under this there are files for collections, TEI items and HTML and images for collection pages etc.

You then need to make sure that the timestamps of your data etc match that of the current version in the s3 bucket.  To do that go into the new directory ('cd cudl-data-source') run the command:

        aws s3 sync --dryrun s3://staging-cudl-data-source data

Now you can confirm that the list of files is going in the correct place you can remove the --dryrun flag to actually run the copy.  This will bring the timestamps and metadata etc in line with the version on s3.

##Making an update

- Before you start you should run the 'sync' command listed above (including removing the dryrun flag) to bring your data up to date.

- Copy the changed files into the correct place in your data directory.

- Commit to git using the commands:

          git pull
          git add .
          git commit -m "my message about the changes"
          git push

- Now to copy the files to s3. For a small number of files it would be best to run the following, for example showing a single TEI file:

      aws s3 cp --dryrun data/items/data/tei/MS-ADD-03995/MS-ADD-03995.xml s3://staging-cudl-data-source/items/data/tei/MS-ADD-03995/MS-ADD-03995.xml

This will ensure you are only changing the files you intend to.  As before remove --dryrun to do the actual copy.

- If you need to copy a large number of files it is possible to use the sync command but BEWARE THIS WILL OVERWRITE WHATEVER IS IN THE BUCKET WITH YOUR LOCAL DATA, even if your data is older etc.  Use with caution.  As before remove --dryrun to do the actual copy.

       aws s3 sync --dryrun data s3://staging-cudl-data-source

If you need to delete files at the bucket that are not in your local directory, you can use the "--delete" flag with the sync command.

##Listing contents of a bucket

If you want to check what's in the s3 bucket, without pulling it to your local file system, you can use the command:

    aws s3 ls s3://staging-cudl-data-source

and append any path if you want a specific directory.
