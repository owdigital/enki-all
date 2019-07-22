# Agent API

The agent's default serving port is 3010.

## Adding data

All data sending requests should be sent to /data as a POST. All message formats below (encoded as JSON) should be accepted. See [/test/clj/enki_agent/bletchley/data](../test/clj/enki_agent/bletchley/data) for examples.

The return value is a JSON doc and will always include a `type` field. Other keys depend on `type`

- type `error`
    - `error` - containing a text reason for the error
- type `consus`
    - `location` - a Consus path where the data was stored (locations should be generally regarded as opaque values by other services)
- type `enki`
    - No other fields
	
## Supported message formats

A data processor e.g. bank
```
{ 
  type: 'data-processor',
  id: 'beta',
  displayName: 'Foo Bank'
}
```
A data subject, e.g. customer. "id" is a 
```
{ 
  type: 'data-subject',
  id: 'alpha', // subjectId, unique within a processor
  subjectType: 'user-bank-customer',
  processorId: 'beta'
}
```
A PII type. 
``` 
{
  type: 'pii-type',
  id: 'surname', // short string without spaces to meet OAuth restrictions
  description: 'Customer surname'
}
```
A piece of PII data. 
```
{
  type: 'pii-data',
  id: 'delta', // some GUID
  piiType: 'surname',
  subjectId: 'alpha',
  processorId: 'beta'
  value: 'foo'
}
```
A Sharing Purpose.
```
{
  type: 'sharing-purpose',
  id: 'purpose_101', // GUID
  description: 'For creating a private bank account',
}
```
A metadata assertion 
```{
  type: 'metadata-assertion',
  id: 'gamma', // GUID
  subjectId: 'alpha', // who
  piiType: 'surname' // what is the PII type
  processorId: 'beta', // processor that provided the subject
  location: 'abc/def',  // where is the PII
  createdAt: '2012-04-23T18:25:43.511:00' // ISO 8601 timestamp
}
```
An assertion of consent to share data
```{
  type: 'share-assertion',
  id: 'epsilon', // GUID
  metadataId: 'gamma',     // what assertion is shared
  sharingProcessorId: 'beta',  // with whom
  purposeId: 'purpose_101',  // for what purpose
  createdAt: '2012-04-23T18:25:43.511:00', // ISO 8601 timestamp
  consentStart: '2012-04-23T18:25:43.511:00', // for how long (ISO 8601)
  consentEnd: '2012-12-13T12:00:00.000:00' // end of consent. Always has a value, can't be open ended
}
```
	
## Retrieving data

Send a GET request to /data with "file" set to the Consus file to retrieve
e.g. curl -vsi http://localhost:3010/data?file=test-agent@test.labshift.io/processors/beta/subjects/alpha/pii-data/delta

Return options:
* 200 - {"type":"consus","data": {"keys":"Whatever was in consus"}}
* 404 - {"type":"error","error":"Not found"}


Send a GET request to /data with "dir" set to a Consus directory to retrieve
e.g. curl -vsi http://localhost:3010/data?dir=test-agent@test.labshift.io/processors/beta/subjects/alpha/pii-data/

Return options:
* 200 - {"type":"consus","items":[{"short":"delta", "long": {"test-agent@test.labshift.io/processors/beta/subjects/alpha/pii-data/delta"}]}
* 404 - {"type":"error","error":"Not found"}

## Giving access to an additional user

By default any data added is accessible by the user for this agent (CONSUS_USER, or "test-agent@test.labshift.io" as a default). If you want to give access to another user, do the following:

POST request to /access with a JSON dictionary containing two keys:

* `location`: Consus path (as per `location` field from adding data)
* `user`: Additional user to give access to.

