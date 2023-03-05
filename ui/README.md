# cy17-starter-react-app

Entire React App. starter that deploy on AWS Cloud with GitHubCI/CD and release actions for both dev/prod environments. Can be applied to anything that use a Dockerfile.

Check both current deployments in AWS cloud here:

DEV: https://react-dev.creativeyann17.com/

PROD: https://react.creativeyann17.com/

## Deployment

The current deployment is done on **AWS Elasticbeanstalk** using **Docker ECS** and two separated envs `dev` and `prod`.

- `cd.yml` will continually deploy every new merge on the main branch to the `dev` env.

- `release.yml` will deploy every new tag to the `prod` env.

_Note: release could be changed to deploy on a pre-prod env for example, letting only the AWS root user in charge of manually deploy prod_

The following secrets have to be set for the repository

Docker-hub access token with `read/write` permissions:

- DOCKER_USERNAME
- DOCKER_PASSWORD

AWS User with the following permission `AdministratorAccess-AWSElasticBeanstalk`:

- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY

### Docker-hub

Both `cd/release` actions will publish to **docker-hub** the built image with the following tag:

- `cd.yml` the current commit sha
- `release.yml` the tag of the release

### Dockerrun.aws.json

`cd/release` actions will replace the content of `Dockerrun.aws.json` file by chaging the `__TAG__` of the docker image with the one being build so that **AWS ECS** will know reading this file which docker image to deploy.

### Env vars

React apps are a little tricky when playing with env vars. This starter **set env vars when building and not at runtime**.

## Libraries

- Rooting `react-router-dom`
- Meta-tags `react-helmet`
- i18n `i18n-next`
- utils `lodash`
- REST `axios-hooks`

### Node

The files should have the same node version:

- `.nvmrc`
- `Dockerfile`
- `ci.yml`

## Actions

- `ci.yml` validate tests + dockerfile
- `cd.yml` build main branch and publish to docker-hub then deploy AWS (dev env)
- `release.yml` build tag and publish to docker-hub then deploy AWS (prod env)

## VSCode

### Extensions

List of recommended extensions:

- Code Spell Checker
- French - Code Spell Checker
- Import cost
- GitLens
- Prettier - Code formatter
- ESLint

### Format

Auto-format will be performed by `husky` on every staged files and on save as defined in `.vscode/settings.json`.

ESLint configuration can be changed with `npx eslint --init`.

### Tabs <=> spaces

There are several places to modify to switch from tabs to spaces (and reverse):

Changes the following files **with consistency**:

- `.vscode/settings.json` editor.tabSize/insertSpaces
- `.eslintrc.js` rules.indent (2 or tab)
- `.prettierrc` tabWidth should be the same as the others

## AWS Cloud

### Domain + DNS + Cert

Do the following to have a custom domain + HTTPs + DNS redirection to sub-domains:

- **route53** request a domain (cost money) and wait for validation.
- **ACM** request a public certificate for that domain. Select DNS validation.
- **route53** a new hosted zone should be created by **ACM** for that domain.
- **EB** When cert is validated, add load-balancer rules for port 443 to use the certificate.
- **route53** in the hosted zone add all the record you want from sub-domain url to the **EB** instance where the docker run.
- **EC2** find the load-balancer, delete the listener 80 and create a new one to redirect 80 to 443
