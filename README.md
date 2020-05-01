# Introduction
[Documentation](https://jcustenborder.github.io/kafka-connect-documentation/projects/kafka-connect-transform-cobol)

Installation through the [Confluent Hub Client](https://docs.confluent.io/current/connect/managing/confluent-hub/client.html)

The plugin contains transformations that are used to read binary data that is encoded as a Cobol copybook to structured data that is native Kafka Connect. For example this functionality could be used to convert data from Cobol copybooks to JSON or Avro when utilizing the proper Converter.




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