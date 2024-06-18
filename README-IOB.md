### Work with github repository

#### Install new github origin

`git remote add origin-github git@github.com:IoBuilders/eventeum.git`


#### List remotes

`git remote -v`

#### Pull all remotes

`git pull --all`


#### Track other origins branches

`git checkout  origin-github/development`


#### Copy commits from other branches

Get the commit id: `git log`
Run in the new branch: `git cherry-pick {commitId}`


### List of files that we have different that the origin Consensys/Eventeum

- `web3jService`
- `web3jServiceTest`
- `Interfaz ContractEventDetailsFactory`
- `ContractEventDetailsFactory`
- `DefaultContractEventDetailsFactoryTest`
