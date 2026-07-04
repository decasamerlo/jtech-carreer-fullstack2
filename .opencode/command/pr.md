---
description: Create PR with branch, commit, and push
subtask: true
---

<summary>
You MUST orchestrate the full PR creation workflow: branch decision, commit, push, and PR creation.
You SHOULD load relevant skills and gather git context.
You MUST ask for user approval at each stage before proceeding.
</summary>

<skill>
using-git-worktrees
</skill>

<skill>
caveman-commit
</skill>

<skill>
finishing-a-development-branch
</skill>

<skill>
requesting-code-review
</skill>

<skill>
verification-before-completion
</skill>

<user_context>

</user_context>

<objective>
You MUST walk through the complete PR lifecycle: determine branch strategy, stage and commit changes, push, and create a pull request — asking for user approval at each decision point.
</objective>

## Workflow

### 1. Environment Check

Determine if working in a simple branch or a separate worktree:

- Show current branch name
- Check if a remote tracking branch exists
- Check if there are any git worktrees listed

Git context:

```
!git status
```

Recent history:

```
!git log --oneline -5
```

### 2. Branch Decision

Ask the user to choose a branch strategy:

- Suggest 2-3 conventional branch names based on the detected changes (use prefixes: `feat/`, `fix/`, `docs/`, `refactor/`, `test/`, `chore/`)
- Offer option to **keep using the current branch** (show the current branch name)
- Let the user pick

If a new branch is chosen, create it.

### 3. Commit Draft

Review the changes:

```
!git diff
!git diff --cached
```

Draft a conventional commit message (caveman-commit style: subject ≤50 chars, body only when the "why" isn't obvious).

Show the draft to the user and ask for approval. Allow them to suggest edits.

Once approved:
- Stage all relevant files
- Commit with the approved message

### 4. Push

Push the branch to remote (set upstream if needed).

### 5. Pull Request Draft

Draft a PR summary with:
- **Title**: derived from the commit message
- **Body**: summary of changes, breaking changes (if any), related issues

Show the draft to the user and ask for approval. Allow edits.

Once approved:
- Create the PR via `gh pr create`
- Show the PR URL to the user
