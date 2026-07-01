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
- Force-syncs upstream branches to same-named branches, except `main` and
  `training-msb`.
- Deletes branches that were previously managed upstream but were later
  deleted upstream.
- Preserves personal branches that are not recorded in the managed upstream
  branch list.
- Makes mirror tags exactly match upstream tags, including tag deletions.

## Maintenance Notes

- Do not add `training-msb` to automatic upstream sync targets.
- Do not manually edit `.github/upstream-mirror-branches.txt`; the sync
  workflow maintains it from upstream branch state.
- Do not make personal changes on official mirror branches such as `6.2.x` or
  `7.0.x`; they are overwritten by sync.
- Put personal or training changes on `training-msb` or another self-owned
  branch.
- Do not create personal tags in this mirror; tag sync deletes tags that do not
  exist upstream.
- If sync fails, first check that GitHub Actions workflow permissions allow
  read and write access.
