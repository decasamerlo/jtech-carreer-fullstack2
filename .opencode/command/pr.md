---
description: Create a pull request with full lifecycle
---

<summary>
You MUST check for existing open PRs first, then walk through the complete PR lifecycle: branch decision, commit, push, and PR creation.
You SHOULD load relevant skills and gather git context.
You MUST ask for user approval at each stage before proceeding.
</summary>

<user_guidelines>
$ARGUMENTS
</user_guidelines>

<objective>
You MUST orchestrate the full PR creation workflow: check for existing PRs, branch decision, commit, push, and PR creation — asking for user approval at each decision point.
</objective>

## Workflow

### 0. Existing PR Check

Check if there is already an open PR for the current branch:

```
!git branch --show-current
```

```
!gh pr list --head $(git branch --show-current) --state open --json number,title,url --limit 1
```

If an open PR exists, use the `question` tool to ask the user:

- **Update existing PR** — push a new commit and optionally update the PR title/description
- **Create new branch + PR** — create a new branch from the current one, commit, and open a fresh PR

If **Update existing PR** is chosen:
- Skip to step 3 (Commit Draft), then push, then ask for new title/description
- Update the PR via `gh pr edit <number> --title "..." --body "..."`
- Show the updated PR URL and stop

If **Create new branch + PR** is chosen, continue to step 1.

If no open PR exists, continue to step 1.

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

Use the `question` tool to let the user pick a branch:

- Suggest 2-3 conventional branch names based on the detected changes (use prefixes: `feat/`, `fix/`, `docs/`, `refactor/`, `test/`, `chore/`)
- Offer option to **keep using the current branch** (show the current branch name)

If a new branch is chosen, create it.

### 3. Commit Draft

Review the changes:

```
!git diff
!git diff --cached
```

Draft a conventional commit message (caveman-commit style: subject ≤50 chars, body only when the "why" isn't obvious).

Show the draft to the user using the `question` tool with options to **approve** or **edit**. The tool already includes a free-form "Type your answer" field — if the user types their own message there, use it directly.

### 4. Push

Push the branch to remote (set upstream if needed). Do not ask for approval.

### 5. Pull Request Draft

Draft a PR summary with:
- **Title**: derived from the commit message
- **Body**: summary of changes, breaking changes (if any), related issues

Show the draft to the user using the `question` tool with options to **approve** or **edit**. The tool already includes a free-form "Type your answer" field — if the user types their own version there, use it directly.

If approved, create the PR via `gh pr create` and show the URL.

## PR Body Template

```markdown
## Summary

<1-3 sentence overview of what this PR does and why>

## Changes

### <Category 1>
- Change 1
- Change 2

### <Category 2>
- Change 1

## Breaking Changes

<List any breaking changes, or "None.">

## Related Issues

<Closes #X, Refs #Y, or "No related issues">
```

## Commit Message Style (caveman-commit)

```
<type>(<scope>): <imperative summary>

<optional body — only when the "why" isn't obvious from the diff>

Closes #X
```

Types: `feat`, `fix`, `refactor`, `perf`, `docs`, `test`, `chore`, `build`, `ci`, `style`, `revert`
