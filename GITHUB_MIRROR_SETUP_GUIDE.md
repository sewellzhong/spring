# Spring Framework 仓库迁移与官方同步操作指南

本文记录本仓库从培训机构私有 Git 仓库迁移到个人 GitHub 仓库，并关联官方 Spring Framework 仓库自动同步的操作过程。

## 当前仓库结构

- 个人 GitHub 仓库：`git@github.com:sewellzhong/spring.git`
- 官方 Spring Framework 仓库：`https://github.com/spring-projects/spring-framework.git`
- 培训机构代码分支：`training-msb`
- 官方源码默认分支：`main`
- 官方其他分支：按官方分支名同名同步，例如 `5.2.x`、`5.3.x`、`6.2.x`、`7.0.x`
- 自动同步 workflow：`.github/workflows/sync-upstream.yml`

## 已执行的迁移步骤

### 1. 暂存本地构建产物

迁移前工作区存在 `buildSrc/.gradle`、`buildSrc/build`、`build/reports` 等构建产物改动。为避免误推，先暂存：

```bash
git stash push -u -m "local build artifacts before github migration"
```

当前 stash 记录：

```bash
stash@{0}: On master: local build artifacts before github migration
```

如需恢复这些本地构建产物：

```bash
git stash pop
```

### 2. 创建培训机构代码分支

```bash
git switch -c training-msb
```

该分支保存培训机构提供的源码和历史提交，后续如需合并官方更新，由你手动操作。

### 3. 将 origin 从培训机构私库改为个人 GitHub 仓库

原远端：

```bash
https://git.mashibing.com/msb-mca/spring.git
```

修改为：

```bash
git remote set-url origin git@github.com:sewellzhong/spring.git
```

推送培训机构分支：

```bash
git push -u origin training-msb
```

### 4. 添加官方 upstream 远端

```bash
git remote add upstream https://github.com/spring-projects/spring-framework.git
git fetch upstream --prune --tags
```

### 5. 同步官方所有非 main 分支

```bash
for branch in $(git for-each-ref --format='%(refname:strip=3)' refs/remotes/upstream | grep -v '^HEAD$' | grep -v '^main$'); do
  git push origin "refs/remotes/upstream/${branch}:refs/heads/${branch}"
done
```

已同步到 GitHub 的官方分支包括：

```text
3.0.x
3.1.x
3.2.x
4.0.x
4.1.x
4.2.x
4.3.x
5.0.x
5.1.x
5.2.x
5.3.x
6.0.x
6.1.x
6.2.x
7.0.x
docs-build
gh-pages
```

### 6. 同步官方 tag

```bash
git push origin --tags
```

### 7. 创建 main 分支并添加自动同步 workflow

基于官方 `upstream/main` 创建本地 `main`：

```bash
git switch -c main upstream/main
```

添加 `.github/workflows/sync-upstream.yml`，提交并推送：

```bash
git add .github/workflows/sync-upstream.yml
git commit -m "Add upstream sync workflow"
git push -u origin main
```

后续修正 workflow 后，使用：

```bash
git commit --amend --no-edit
git push --force-with-lease origin main
```

## 自动同步 workflow 说明

workflow 文件位置：

```text
.github/workflows/sync-upstream.yml
```

运行方式：

- 每天自动运行一次：UTC `20:00`
- 北京/上海时间：每天 `04:00`
- 也可以在 GitHub Actions 页面手动执行 `workflow_dispatch`

同步行为：

- 从官方仓库 fetch 所有分支和 tag。
- 默认分支 `main` 会保留本仓库的 workflow 文件，并合并官方 `upstream/main`。
- 其他官方分支会强制同步到个人 GitHub 的同名分支。
- `training-msb` 被明确保护，不会被自动同步覆盖。
- tag 会同步到个人 GitHub 仓库。

## GitHub 页面还需要手动设置

因为本机没有安装 `gh` CLI，默认分支需要在 GitHub 页面手动设置：

```text
GitHub 仓库 -> Settings -> Branches -> Default branch -> main
```

设置完成后，GitHub 仓库首页默认展示 `main` 分支。

## 日常维护命令

### 查看远端配置

```bash
git remote -v
```

期望结果：

```text
origin    git@github.com:sewellzhong/spring.git
upstream  https://github.com/spring-projects/spring-framework.git
```

### 手动拉取官方更新

```bash
git fetch upstream --prune --tags
```

### 手动将官方分支合并到培训机构分支

示例：将官方 `5.2.x` 合并到 `training-msb`：

```bash
git switch training-msb
git fetch origin
git merge origin/5.2.x
git push origin training-msb
```

如需合并其他官方分支，将 `origin/5.2.x` 替换为目标分支，例如：

```bash
git merge origin/6.2.x
```

### 查看当前分支状态

```bash
git status --short --branch
git branch -vv
```

### 查看 GitHub 远端分支

```bash
git ls-remote --heads origin
```

## 注意事项

- 不要把 `training-msb` 作为自动同步目标分支，否则培训机构代码会被官方分支覆盖。
- `main` 比官方 `main` 多一个 `.github/workflows/sync-upstream.yml` 文件，这是为了让 GitHub Actions 能在本仓库内运行。
- 官方分支同步使用强制推送策略，适合镜像官方分支；不要在这些官方同名分支上提交个人修改。
- 个人修改和培训机构代码应保留在 `training-msb` 或其他自建分支中。
- 如果自动同步失败，优先检查 GitHub Actions 日志和仓库 Actions 权限中的 `Workflow permissions` 是否允许 `Read and write permissions`。
