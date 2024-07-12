# epps-bootstrap-poc

This is a simple poc code to convert the Assertion from EHDSI to a bootstrap token that can be used in SOSI STS for a ticket for NSP systems.
Cuurrent implementation takes the provided testfile `ncp-assertion2.xml` and a header (currently not implemented) which will contain metadata such as the country of origin that can be used in logging and tracing later on.

The output file `BST.xml` contain the extracted fields. This output file have to be updated to be similar to bootstraptokens that already can be used in SOSI however the current version should give simplified overview of the data that the token will contain.

## Examples

The examples folder contain two test XMLs from SEB that can be looked at as inspiration. This POC code will write a similar file once final design is determined. The `BST.xml` file contain just the attributes that will eventually go in the final version of the file.

## Next steps

1. Expand the export method to write real SOSI BST xml files
2. Add the json the NCP will provide to the service with metadata and add these to the BST

## How to run

After cloning this repository install dependencies using maven:

`mvn clean install`

Then the output jar will be accessible in the target folder and can be run providing two arguments:

`java -jar AuthenticationFlow.jar <xml-assertion> <soap-header>`

Note that the soap header file is currently not used, but will be added later.
