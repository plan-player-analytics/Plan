# Writing a good issue report
In order to be useful for the maintainers issues should include as much information as possible about the issue.  
The information is crucial for reproducing the issue.

- Tickets about Exceptions should include a full stack trace if at all possible. (Usually found in the Errors.txt file or console logs)
- Tickets about Visual issue such as wrong characters or typos should include a screenshot of the affected window as well as what should be displayed instead.
- Tickets about odd behaviour should include a detailed turn of events how this issue occurred, if at all possible.

Additional information that might be useful:

- Possible logs, debug logs, error logs
- Plan and Server version & type
- Database in use (SQLite / MySQL)
- Possible config settings related to the issue
- Information related to the issue in the database
- Table structure in the database if related to the issue

# Writing a good Pull Request
Good pull requests make the work of maintainers easier, which will make the approval of the pull requests quicker.

New code should be created in a new feature / bugfix / improvement branch.
After done with the feature (Or prior) a PR can be opened.

Good practices that make PR easier to approve:

- Commit often with detailed commit messages: Commits are cheap and can be easily reversed if neccessary.
- Before marking an issue solved in a commit message ("Fixed #issueN.o.") it should be ensured that the issue is fixed. (By manual testing or with Unit Tests.)
- Name PR related to what it is attempting to accomplish. "Implemented improved graph creation", "Bugfix PR for Version 4.0.2"
- In case the feature in PR is not self explanatory an attempt to explain the feature should be made in the message / comments of the PR.
- Code follows similar style as rest of the code and is easy to read. (Brackets used always, Classes with BigFirstLetter, variablesCamelCase)

IF you do not want your PR to be merged yet, include WIP in the title of the PR.

PRs are never merged directly to the `master`-branch, and are instead merged into next version specific branch.  
IF no version specific branch is available when making a PR, select master and notify in the comments about the fact - Maintainers will create a new version specific branch and change the branch of the PR.
