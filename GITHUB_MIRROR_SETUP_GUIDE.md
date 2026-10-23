# Spring Framework Mirror Maintenance Guide

This repository mirrors selected Spring Framework upstream branches into
`sewellzhong/spring` and keeps the training source branch separate.

## Repository Layout

- Personal mirror: `git@github.com:sewellzhong/spring.git`
- Upstream source: `https://github.com/spring-projects/spring-framework.git`
- Training source branch: `training-msb`
- Mirror default branch: `main`
- Mirror sync workflow: `.github/workflows/sync-upstream.yml`
- Managed upstream branch list: `.github/upstream-mirror-branches.txt`

## GitHub Actions Policy

This mirror intentionally keeps only the sync workflow on `main`.

The official Spring Framework repository contains workflows for CI, releases,
snapshot deployment, documentation deployment, and Antora UI updates. Those
workflows depend on `spring-io`, `gradle`, and `jfrog` actions and on official
Spring secrets. They are not needed in this mirror and can fail under the
repository policy that only allows GitHub-created actions or actions owned by
`sewellzhong`.

Keep the repository Actions settings strict:

- Allowed actions: GitHub-created actions and actions owned by `sewellzhong`.
- Workflow permissions: read and write, so the sync workflow can push branches
  and tags.

## Mirror Deploy Key

The built-in `GITHUB_TOKEN` cannot create or update workflow files on mirrored
branches because it does not expose the separate Workflows permission. The
mirror therefore uses a dedicated Ed25519 deploy key that can write only to
`sewellzhong/spring`.

The public key is registered under:

```text
Settings -> Deploy keys
Title: spring-mirror-github-actions
Allow write access: enabled
```

The matching private key is stored as this repository Actions secret:

```text
Settings -> Secrets and variables -> Actions -> New repository secret
Name: MIRROR_SSH_PRIVATE_KEY
Value: <dedicated deploy key private key>
```

The workflow checks for this secret before it performs any push. When the key
is rotated or revoked, update both the deploy key and the matching secret. Never
reuse a personal account SSH key for this purpose.

## Sync Behavior

The sync workflow runs daily at `20:00` UTC and can also be started manually
with `workflow_dispatch`.

It performs these steps:

- Fetches all upstream branches and tags.
- Rebuilds mirror `main` from the latest `upstream/main`.
- Restores the mirror sync workflow, managed branch list, and this maintenance
  guide from `origin/main`.
- Deletes all workflow files except `.github/workflows/sync-upstream.yml`.
- Commits upstream changes and workflow cleanup only when the resulting tree
  changed.
- Updates `main` with `force-with-lease` to avoid merge conflicts caused by
  upstream workflow changes while protecting against concurrent updates.
- Syncs every upstream branch to a same-named branch, except `main` and
  `training-msb`. Branches containing workflow files use the dedicated SSH
  deploy key; branches without workflow files use `GITHUB_TOKEN` so their push
  cannot start another workflow or a GitHub Pages build.
- Adds an empty mirror marker commit above each current upstream branch HEAD.
  Its tree is exactly the upstream tree, its parent is the upstream HEAD, and
  its message contains `[skip ci]` so the SSH-authenticated push does not run
  upstream CI, release, documentation, or backport workflows in this mirror.
- Creates a new marker only after the corresponding upstream HEAD changes. The
  first run of this version normalizes every existing managed branch.
- Deletes branches that were previously managed upstream but were later
  deleted upstream.
- Preserves personal branches that are not recorded in the managed upstream
  branch list.
- Leaves existing tags unchanged when their Git object already matches
  upstream. New or changed upstream tags point to an empty `[skip ci]` marker
  commit whose tree matches and whose parent is the upstream tagged commit.
- Deletes mirror tags that no longer exist upstream.

## Maintenance Notes

- Do not add `training-msb` to automatic upstream sync targets.
- Do not manually edit `.github/upstream-mirror-branches.txt`; the sync
  workflow maintains it from upstream branch state.
- Do not make personal changes on official mirror branches such as `6.2.x` or
  `7.0.x`; they are overwritten by sync.
- A mirrored branch HEAD intentionally differs from upstream by one empty
  marker commit. Verify a branch with
  `git rev-parse origin/7.0.x^ upstream/7.0.x`; the two output SHAs must match.
- Do not remove `[skip ci]` from mirror marker commit messages. Pushes made with
  a personal access token can otherwise start workflows stored on the upstream
  branch.
- Put personal or training changes on `training-msb` or another self-owned
  branch.
- Do not create personal tags in this mirror; tag sync deletes tags that do not
  exist upstream.
- Newly mirrored tags intentionally use marker commits so SSH-authenticated tag
  pushes cannot start upstream release workflows. Existing matching historical
  tags retain their original upstream Git objects.
- If sync reports that `MIRROR_SSH_PRIVATE_KEY` is required or cannot access
  the repository, check that the secret contains the matching private key and
  that `spring-mirror-github-actions` still has write access under Deploy keys.
