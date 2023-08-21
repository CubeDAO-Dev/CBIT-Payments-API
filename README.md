# CBIT Payments API

Please see usage documentation and get started here: https://dev.cubedao.com/payments/cbit-payments-api

Java docs reference: https://dev.cubedao.com/payments-javadoc/index.html

## About continuous integration in this repo

Every time you push any commit to `main`, GitHub will automatically build a jarfile and upload it to Actions.

To **create a new release**, update the version number in `pom.xml` and create a tag with the format `vX.Y.Z`. For example:
```bash
git tag v0.0.2
git push origin v0.0.2
```
Again, this tag should match the version number in `pom.xml`.

When a new tag is created GitHub will:
1. Build the CBIT-Payments-API jar.
2. Upload the jar to a public maven repository.
3. Automatically create a [release](https://github.com/CubeDAO-Dev/CBIT-Payments-API/releases/) so users can download the shaded jar to put in their plugins folder.

When incrementing the version number, you should also update the version number in
[this section of the documentation on using CBIT-Payments-API as a maven dependency](https://dev.cubedao.com/payments/cbit-payments-api#include-the-library-with-the-build) 

This was made possible by https://github.com/NFT-Worlds/WRLD-Payments-API
