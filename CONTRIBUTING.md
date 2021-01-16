## Contributing

First off, thank you for considering contributing to OpenAir.

### Where do I go from here?

If you've noticed a bug or have a feature request, [make one][new issue]! It's
generally best if you get confirmation of your bug or approval for your feature
request this way before starting to code.

If you have a general question about OpenAir please [ask us], the issue tracker is only for bugs and feature requests.

### Fork & create a branch

If this is something you think you can fix, then [fork OpenAir] and create
a branch with a descriptive name.

A good branch name would be (where issue #325 is the ticket you're working on):

```sh
git checkout -b 325-add-japanese-translations
```

### Implement your fix or feature

At this point, you're ready to make your changes! Feel free to ask for help;
everyone is a beginner at first :wink:

### View your changes in Android Studio

This should go without saying, but make sure to test that your changes do work as intended.

### Get the style right

Your patch should follow the same conventions & pass the same code quality
checks as the rest of the project.

### Make a Pull Request

At this point, you should switch back to your develop branch and make sure it's
up to date with OpenAir's develop branch:

```sh
git remote add upstream git@github.com:openairapp/OpenAir-Android.git
git checkout develop
git pull upstream develop
```

Then update your feature branch from your local copy of develop, and push it!

```sh
git checkout 325-add-japanese-translations
git rebase develop
git push --set-upstream origin 325-add-japanese-translations
```

Finally, go to GitHub and [make a Pull Request][] :D

Github Actions will run our test suite against your new changes, if these don't all pass you might need to make some changes

### Keeping your Pull Request updated

If a maintainer asks you to "rebase" your PR, they're saying that a lot of code
has changed, and that you need to update your branch so it's easier to merge.

To learn more about rebasing in Git, there are a lot of [good][git rebasing]
[resources][interactive rebase] but here's the suggested workflow:

```sh
git checkout 325-add-japanese-translations
git pull --rebase upstream develop
git push --force-with-lease 325-add-japanese-translations
```

### Merging a PR (maintainers only)

A PR can only be merged into develop by a maintainer if:

* It is passing CI.
* It has been approved by at least two maintainers. If it was a maintainer who
  opened the PR, only one extra approval is needed.
* It has no requested changes.
* It is up to date with current develop.

Any maintainer is allowed to merge a PR if all of these conditions are
met.



[new issue]: https://github.com/openairapp/OpenAir-Android/issues/new
[ask us]: https://github.com/openairapp/OpenAir-Android/discussions
[fork OpenAir]: https://help.github.com/articles/fork-a-repo
[make a pull request]: https://help.github.com/articles/creating-a-pull-request
[git rebasing]: http://git-scm.com/book/en/Git-Branching-Rebasing
[interactive rebase]: https://help.github.com/en/github/using-git/about-git-rebase
