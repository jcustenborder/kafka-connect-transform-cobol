# Introduction
[Documentation](https://jcustenborder.github.io/kafka-connect-documentation/projects/kafka-connect-transform-cobol) | [Confluent Hub](https://www.confluent.io/hub/jcustenborder/kafka-connect-transform-cobol)

The plugin contains transformations that are used to read binary data that is encoded as a Cobol copybook to structured data that is native Kafka Connect. For example this functionality could be used to convert data from Cobol copybooks to JSON or Avro when utilizing the proper Converter.

# Installation

## Confluent Hub

The following command can be used to install the plugin directly from the Confluent Hub using the
[Confluent Hub Client](https://docs.confluent.io/current/connect/managing/confluent-hub/client.html).

```bash
confluent-hub install jcustenborder/kafka-connect-transform-cobol:latest
```

## Manually

The zip file that is deployed to the [Confluent Hub](https://www.confluent.io/hub/jcustenborder/kafka-connect-transform-cobol) is available under
`target/components/packages/`. You can manually extract this zip file which includes all dependencies. All the dependencies
that are required to deploy the plugin are under `target/kafka-connect-target` as well. Make sure that you include all the dependencies that are required
to run the plugin.

1. Create a directory under the `plugin.path` on your Connect worker.
2. Copy all of the dependencies under the newly created subdirectory.
3. Restart the Connect worker.




# Transformations
## [FromCopybook](https://jcustenborder.github.io/kafka-connect-documentation/projects/kafka-connect-transform-cobol/transformations/FromCopybook.html)

*Key*
```
com.github.jcustenborder.kafka.connect.cobol.FromCopybook$Key
```
*Value*
```
com.github.jcustenborder.kafka.connect.cobol.FromCopybook$Value
```

The FromCopybook transformation is used to convert binary data that is stored as a Cobol Copybook and converts the data to a Kafka Connect Structure.
### Tip

This transformation expects that the incoming data will be formatted as BYTES.
### Configuration

#### General


##### `cobol.copybook`

The text of the Cobol copybook structure inline as a string.

*Importance:* HIGH

*Type:* STRING



##### `charset`

The charset to use when the copybook data is written as a String.

*Importance:* HIGH

*Type:* STRING

*Default Value:* UTF-8

*Validator:* Valid values: 'Big5', 'Big5-HKSCS', 'CESU-8', 'EUC-JP', 'EUC-KR', 'GB18030', 'GB2312', 'GBK', 'IBM-Thai', 'IBM00858', 'IBM01140', 'IBM01141', 'IBM01142', 'IBM01143', 'IBM01144', 'IBM01145', 'IBM01146', 'IBM01147', 'IBM01148', 'IBM01149', 'IBM037', 'IBM1026', 'IBM1047', 'IBM273', 'IBM277', 'IBM278', 'IBM280', 'IBM284', 'IBM285', 'IBM290', 'IBM297', 'IBM420', 'IBM424', 'IBM437', 'IBM500', 'IBM775', 'IBM850', 'IBM852', 'IBM855', 'IBM857', 'IBM860', 'IBM861', 'IBM862', 'IBM863', 'IBM864', 'IBM865', 'IBM866', 'IBM868', 'IBM869', 'IBM870', 'IBM871', 'IBM918', 'ISO-2022-CN', 'ISO-2022-JP', 'ISO-2022-JP-2', 'ISO-2022-KR', 'ISO-8859-1', 'ISO-8859-13', 'ISO-8859-15', 'ISO-8859-2', 'ISO-8859-3', 'ISO-8859-4', 'ISO-8859-5', 'ISO-8859-6', 'ISO-8859-7', 'ISO-8859-8', 'ISO-8859-9', 'JIS_X0201', 'JIS_X0212-1990', 'KOI8-R', 'KOI8-U', 'Shift_JIS', 'TIS-620', 'US-ASCII', 'UTF-16', 'UTF-16BE', 'UTF-16LE', 'UTF-32', 'UTF-32BE', 'UTF-32LE', 'UTF-8', 'X-UTF-32BE-BOM', 'X-UTF-32LE-BOM', 'windows-1250', 'windows-1251', 'windows-1252', 'windows-1253', 'windows-1254', 'windows-1255', 'windows-1256', 'windows-1257', 'windows-1258', 'windows-31j', 'x-Big5-HKSCS-2001', 'x-Big5-Solaris', 'x-COMPOUND_TEXT', 'x-EUC-TW', 'x-IBM1006', 'x-IBM1025', 'x-IBM1046', 'x-IBM1097', 'x-IBM1098', 'x-IBM1112', 'x-IBM1122', 'x-IBM1123', 'x-IBM1124', 'x-IBM1166', 'x-IBM1364', 'x-IBM1381', 'x-IBM1383', 'x-IBM300', 'x-IBM33722', 'x-IBM737', 'x-IBM833', 'x-IBM834', 'x-IBM856', 'x-IBM874', 'x-IBM875', 'x-IBM921', 'x-IBM922', 'x-IBM930', 'x-IBM933', 'x-IBM935', 'x-IBM937', 'x-IBM939', 'x-IBM942', 'x-IBM942C', 'x-IBM943', 'x-IBM943C', 'x-IBM948', 'x-IBM949', 'x-IBM949C', 'x-IBM950', 'x-IBM964', 'x-IBM970', 'x-ISCII91', 'x-ISO-2022-CN-CNS', 'x-ISO-2022-CN-GB', 'x-JIS0208', 'x-JISAutoDetect', 'x-Johab', 'x-MS932_0213', 'x-MS950-HKSCS', 'x-MS950-HKSCS-XP', 'x-MacArabic', 'x-MacCentralEurope', 'x-MacCroatian', 'x-MacCyrillic', 'x-MacDingbat', 'x-MacGreek', 'x-MacHebrew', 'x-MacIceland', 'x-MacRoman', 'x-MacRomania', 'x-MacSymbol', 'x-MacThai', 'x-MacTurkish', 'x-MacUkraine', 'x-PCK', 'x-SJIS_0213', 'x-UTF-16LE-BOM', 'x-euc-jp-linux', 'x-eucJP-Open', 'x-iso-8859-11', 'x-mswin-936', 'x-windows-50220', 'x-windows-50221', 'x-windows-874', 'x-windows-949', 'x-windows-950', 'x-windows-iso2022jp'



##### `cobol.context.type`

The type of Cobol context to create. This is used to determine how data is encoded. `Ascii` - An ASCII based context will be used for data encoding., `Ebcdic` - An EBCDIC based context will be used for data encoding.

*Importance:* HIGH

*Type:* STRING

*Default Value:* Ebcdic

*Validator:* Matches: ``Ebcdic``, ``Ascii``



##### `namespace`

Namespace for the generated schemas

*Importance:* HIGH

*Type:* STRING

*Default Value:* com.github.jcustenborder.kafka.connect.cobol





# Development

## Building the source

```bash
mvn clean package
```

## Contributions

Contributions are always welcomed! Before you start any development please create an issue and
start a discussion. Create a pull request against your newly created issue and we're happy to see
if we can merge your pull request. First and foremost any time you're adding code to the code base
you need to include test coverage. Make sure that you run `mvn clean package` before submitting your
pull to ensure that all of the tests, checkstyle rules, and the package can be successfully built.