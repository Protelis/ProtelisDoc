## [3.0.0](https://github.com/Protelis/ProtelisDoc/compare/2.0.0...3.0.0) (2022-03-05)


### ⚠ BREAKING CHANGES

* major change to all internals

### Refactoring

* major change to all internals ([da6d142](https://github.com/Protelis/ProtelisDoc/commit/da6d1420bd91f627f1ae4836bf3461da97c2d117))

## [2.0.0](https://github.com/Protelis/ProtelisDoc/compare/1.0.3...2.0.0) (2022-03-05)


### ⚠ BREAKING CHANGES

* rename the task into 'protelisdoc'

### Features

* rename the task into 'protelisdoc' ([b3709ba](https://github.com/Protelis/ProtelisDoc/commit/b3709baf86b8dd2e7284de5c78e5ac88390340e6))


### Bug Fixes

* defer the protelisdoc task configuration until after project evaluation ([97cb57e](https://github.com/Protelis/ProtelisDoc/commit/97cb57ea84717a1c81c7f0b50b3bcd6e45aa55f1))

### [1.0.3](https://github.com/Protelis/ProtelisDoc/compare/1.0.2...1.0.3) (2022-03-05)


### Bug Fixes

* always add the Kotlin standard library in the protelisdoc configuration ([0fbe137](https://github.com/Protelis/ProtelisDoc/commit/0fbe13725cdd541744a93dce368b8352e502abaa))


### Tests

* test compatibility with the gradle toolchains ([e99a40b](https://github.com/Protelis/ProtelisDoc/commit/e99a40ba3d99e71972b20f8f01d003864627db73))

### [1.0.2](https://github.com/Protelis/ProtelisDoc/compare/1.0.1...1.0.2) (2022-03-05)


### Bug Fixes

* remove disturbing println ([2096d2f](https://github.com/Protelis/ProtelisDoc/commit/2096d2f679c8e9af550031b459b3853fe0261e93))

### [1.0.1](https://github.com/Protelis/ProtelisDoc/compare/1.0.0...1.0.1) (2022-03-05)


### Bug Fixes

* **release:** generate a release ([7b8e483](https://github.com/Protelis/ProtelisDoc/commit/7b8e483e7067c13667f54b51a0320b4614dc716f))


### Dependency updates

* **core-deps:** update kotlin to 1.5.31 ([59ddc98](https://github.com/Protelis/ProtelisDoc/commit/59ddc98b6afbce709368cb07f4c60d4a5cfb5123))
* **core-deps:** update plugin dokka to v1.6.10 ([b7174a6](https://github.com/Protelis/ProtelisDoc/commit/b7174a68eac90c7dc0561b66a3568f5e677f0170))
* **core-deps:** update plugin kotlin-jvm to v1.6.10 ([06a7ce5](https://github.com/Protelis/ProtelisDoc/commit/06a7ce534b53b977f5e7af68499d2723745464eb))
* **deps:** update plugin com.gradle.enterprise to v3.6.4 ([0ff4f44](https://github.com/Protelis/ProtelisDoc/commit/0ff4f4418a0cf8bf85ec3a0683c6ff6398adc957))


### Build and continuous integration

* **deps:** update danysk/build-check-deploy-gradle-action action to v1.2.4 ([c4d5020](https://github.com/Protelis/ProtelisDoc/commit/c4d50207c8976dd8c19fda8e9f675a8d607d286b))
* enable renovate ([92e59ee](https://github.com/Protelis/ProtelisDoc/commit/92e59ee91a26ee5ee661d315e14501456a425859))
* only run the highest priority workflow ([364d147](https://github.com/Protelis/ProtelisDoc/commit/364d147df9a8b383c266e3a4a1003cabf7979a79))
* **renovate:** include forks ([7cf7d0e](https://github.com/Protelis/ProtelisDoc/commit/7cf7d0eca9677dff7b13de212028b242de590685))

## [1.0.0](https://github.com/Protelis/ProtelisDoc/compare/0.4.2...1.0.0) (2022-03-05)


### ⚠ BREAKING CHANGES

* rename root project

### Features

* **ci:** enable semantic release ([6e044d0](https://github.com/Protelis/ProtelisDoc/commit/6e044d025ab01943f409f8782c2d76f5445afe72))


### Bug Fixes

* remove unneeded cast ([0d1e4df](https://github.com/Protelis/ProtelisDoc/commit/0d1e4df321587a768869176b01140d61c0e41f98))


### Refactoring

* rename root project ([04e22d8](https://github.com/Protelis/ProtelisDoc/commit/04e22d85ce035979636c82733031ce6c7c08b9d0))


### Tests

* switch to kotest ([3ca4354](https://github.com/Protelis/ProtelisDoc/commit/3ca4354f2f29bdb1cc657569bf45ab343f91d18e))


### Style improvements

* replace some expressions with more idiomatic ones ([5653423](https://github.com/Protelis/ProtelisDoc/commit/5653423cd105b674d108bf301118e73809300deb))


### General maintenance

* enable secure hashes ([e94bf5c](https://github.com/Protelis/ProtelisDoc/commit/e94bf5c66aa4176ad39fefbc7c9f3d5df1f73f59))


### Dependency updates

* **deps:** update Gradle to 7.4 ([2725771](https://github.com/Protelis/ProtelisDoc/commit/2725771ae3ba9f8d2d587f4f73697bb15ab453be))
* **deps:** update publishOnCentral to 0.7.13 ([d4c5d30](https://github.com/Protelis/ProtelisDoc/commit/d4c5d30c59d17ed720c7cec332f98724f6a29652))


### Build and continuous integration

* cleanup unused scripts ([d8d5f01](https://github.com/Protelis/ProtelisDoc/commit/d8d5f01560d28ac9d4d735bc1164483a4b07df73))
* delete automerge ([5658fe8](https://github.com/Protelis/ProtelisDoc/commit/5658fe8a745ef8febf24aa7e4b3e6218d8bdb589))
* disable cronjob ([0502fa7](https://github.com/Protelis/ProtelisDoc/commit/0502fa7af0251ef3d2ee6e0f34ce408670996eb3))
* drop refreshVersions ([970e467](https://github.com/Protelis/ProtelisDoc/commit/970e46738c87e699467527c478835b06922e55c4))
* enable multi-jvm testing ([4ea27a4](https://github.com/Protelis/ProtelisDoc/commit/4ea27a4ea163757a1444d0fd21f7d6b9d5dc67f8))
* enable the QA ([efd630d](https://github.com/Protelis/ProtelisDoc/commit/efd630da567be51639bc19c2763fe261248e8254))
* improve test task configuration ([31cecc8](https://github.com/Protelis/ProtelisDoc/commit/31cecc83d84e17470e194e8b45a14cd292ebb225))
* move dokka to the catalog ([035e398](https://github.com/Protelis/ProtelisDoc/commit/035e3981dc32cf26659a1aa961ce04d814874a81))
* move gitsemver to the catalog ([8d93527](https://github.com/Protelis/ProtelisDoc/commit/8d93527009c2a0b46e783b68c6cdc16e7c0b5ecc))
* move kotlin to the catalog ([1e3007b](https://github.com/Protelis/ProtelisDoc/commit/1e3007b6fc547861291fc39de42e5baa4c14588e))
* move publish-on-central to the catalog ([8ff81bd](https://github.com/Protelis/ProtelisDoc/commit/8ff81bd0999ba7e3e78769da3cd15e0528dd5786))
* move the gradle-plugin-publish-plugin to the catalog ([c03b7e2](https://github.com/Protelis/ProtelisDoc/commit/c03b7e2f774053b478da8b7ed5df55edc4014311))
* suppress instable API usage warning ([779f366](https://github.com/Protelis/ProtelisDoc/commit/779f366aecc44d11d7ca8f9a35cdbc78cc78da2d))
* temporarily disable the QA ([ec982ea](https://github.com/Protelis/ProtelisDoc/commit/ec982eae0e1ea7f621dbfa8875cbab31b0c3c271))
