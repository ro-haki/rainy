---
description: Stage all changes and commit to the main branch with a reasoned message
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git add:*), Bash(git commit:*), Bash(git branch:*), Bash(git rev-parse:*)
---

Commit the current working-tree changes to the `main` branch.

Steps:
1. Run `git status` and `git diff` (staged and unstaged) to see exactly what changed.
2. Check the current branch with `git rev-parse --abbrev-ref HEAD`. If it is not `main`, stop and tell me which branch I'm on instead of committing — do not switch branches.
3. Stage everything relevant with `git add -A`.
4. Write a commit message you reason out from the actual diff:
   - First line: a concise imperative summary (<=72 chars) of the main change.
   - Then a blank line and 1-4 bullet points explaining WHAT changed and WHY, grounded in the diff — not a generic message.
5Print the resulting `git log -1 --stat` so I can see what was committed.

Extra instructions for this commit: $ARGUMENTS
