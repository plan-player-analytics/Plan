# Writing a good issue
In order to be useful for the maintainers issues should include as much information as possible about the issue.  
The information is crucial for reproducing the issue.

## :warning: Good exception report

- [Example of a good issue report for an Exception](https://github.com/plan-player-analytics/Plan/issues/945)

### Contains

- Description about what is wrong, or what was done before the issue happened.
- Version information, Server information and what database is in use
- Exception pastebin/gist link or inside code block (Surrounded by 3 ``` characters)

## :eyeglasses: Good visual bug report

### Contains

- Description about what is wrong
- Full page screenshot containing the visual issue, with issue possibly highlighted 
- Version information, are any custom html files being used

## :coffee: Good feature request

- [Example of a good feature request](https://github.com/plan-player-analytics/Plan/issues/872)

### Contains

- Description about what the feature solves (How can this be used?)
- Description about the feature
- Alternative features that would solve the same use case you have considered

# Writing a good Pull Request

In order to make it easier to contribute two wiki articles are available:
- [Project Setup](https://github.com/plan-player-analytics/Plan/wiki/Project-Setup) on setting up the build environment
- [Project Architecture](https://github.com/plan-player-analytics/Plan/wiki/Project-Architecture) on structure of the project

## :tophat: Good Pull Request

### Contains

- 1 to 750 changes
- Descriptive commit messages
- Summary of what the PR contains
- Automatically or manually tested code
  - (Summary of manual tests)

### Code

You can code with whichever style you find most comfortable, as long as it is readable, but be aware that it will be formatted after your PR has been merged.

- Variables describe what they contain
- Methods do one thing (Single Responsibility Principle)
- Comments
  - When reason for doing something might be unclear, it is commented ("The value might be over 100, so it has to be checked")
  - When something might break in the future, it is commented ("MySQL v5.6.2 might optimize this fix away")
  - When something is unclear, it is commented ("I don't know if this works")
- Magic numbers are named with variables (`getItem(id)` instead of `API.getItem(5)`)

### Testing

Sometimes things do not work how they should, so testing is a good practice.

- [Instructions for running build and test goals](https://github.com/plan-player-analytics/Plan/wiki/Project-Setup#building-and-testing)

Because plugins often require mocking for tests, I do not require automatic tests for PRs.

## :chart_with_upwards_trend: Good Review on a Pull Request

Good reviews should leave both the reviewer and the contributor positive feeling about the interaction.

### Contains

- Questions about possible concerns about the code
- Notes about possible bugs or mistakes
- Positive encouragement
  - (Advice)

### Does not contain

- Comments against the person submitting the PR
- Fast to fix things
  - Formatting advice about small things ("If blocks should use brackets")
  - Nitpicking ("This variable should be called bar instead of foo")
  - Typos
