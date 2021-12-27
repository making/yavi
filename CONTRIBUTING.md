# YAVI contribution guidelines

Great to hear you would like to contribute to YAVI! 🥳👍🏽

## How to contribute

Before you contribute your changes please make sure you follow the guidelines in this file.

* Please run `./mvnw formatter:format` to make sure your follow the code style guidelines.
* Make sure you follow the [Commit messages](#commit-messages) guidelines.
* Make sure you describe your changes inside the pull request, so we get a better overview why this change is needed.
* Add few test cases (unit or integration tests) that back your changes.
   
## Source Code style

In this section we will provide you with information on how to format your code and import the styles of this project.

* You can find IDE settings to import at `src/etc/`.
* This project uses a maven formatter plugin that will autoformat your code. You can expect that
  calling `./mvnw formatter:format` is the correct code style. Therefore, please run it before commiting.

## Commit messages
The commit message should follow the following style:

* First line contains the summary starting with a capital letter.
* Finish summary with a dot.
* In the description, don’t use single line breaks. No manual wrapping. Separate paragraphs by a blank line.
* During merges, add a reference to the original pull request.
* To close a GitHub issue, use GitHub syntax to reference issues.
* Add related references at the very bottom (also see the section on pull requests below).


```
Summary of the commit (try to stay under 80 characters).

Additional explanations if necessary.

Original pull request #?? (can be omitted when there's no ticket using Closes #?? syntax)
Closes #?? (optionally close tickets)
Related tickets #??? (optionally refer to related tickets)
```