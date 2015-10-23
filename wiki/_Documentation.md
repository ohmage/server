### Hello.

For those readers familiar with ohmage 2.x, ohmage 3.0 has been almost completely rewritten. For the server APIs, JSON is still the data format, but the APIs are now much more inline with general REST and HTTP principles. 

### Study Setup

For research coordinators that would like to setup a group of users to collect data, see the [standard clinical study setup workflow](https://github.com/ohmage/server/wiki/Clinical-Study-Setup-Flow).

### Create Account

These are the flows for creating a user account in ohmage either using an email address or a third-party authentication provider. 

#### Self-registration with Email

Invoke the user creation API call. The user will be sent an activation email. Until the user activates their account, the [auth token API](https://github.com/ohmage/server/wiki/3.x-Authentication-APIs#ohmage-account-request) will return an HTTP 409. Because a 409 is returned, clients may allow the user to browse through system entities that are visible to non-authenticated users.

#### Self-registration with Google Account

Check out the [Google documentation](https://developers.google.com/+/) for your particular client.

Once your client receives the Google authentication token, it can be posted to the ohmage [auth token API](https://github.com/ohmage/server/wiki/3.x-Authentication-APIs#provider-account-request) and the user will be successfully logged in.

### Authenticate

Invoke the [authentication token API](3.x-Authentication-APIs). If successful, the server will return an authentication token, a refresh token, and an expiration date for the authentication token. Use the refresh token on the [refresh token API call](https://github.com/ohmage/server/wiki/3.x-Authentication-APIs#refresh-token-request) in order to get new authentication and refresh tokens. The refresh token is valid for one-time use only. 

### General API Considerations

* Clients should pass an authentication token to non-authenticated calls, when a token is available, in order to more effectively track the user and help server admins debug.

* When communicating with server, the authentication token is always sent as a header.
> `Authorization: ohmage $TOKEN`

* ohmage 3.x utilizes the HTTP methods for verbs and URIs to describe nouns in the system.
    * PUT - not allowed
    * POST - update an entity's information or adds a new entity to a particular list
    * GET - read an entity
    * HEAD - return the associated HTTP headers for an entity
    * DELETE - delete or invalidate an entity (not allowed for all API calls)

* All successful requests will see a 2xx HTTP response code. Unless otherwise specified, the response body is a JSON blob (object or array). The `Content-Type` will be `application/json`.

* All failed requests will see a 4xx or 5xx HTTP response code. Unless otherwise specified, the response body is a user friendly text string. The `Content-Type` will be `text/plain`.

* Clients may request compressed responses by sending the `Accept-Encoding` header. Clients can also send the `Content-Encoding` header and compress their requests. Currently only `gzip` is supported. The same is true for the `Content-Transfer-Encoding` header for parts in `multipart` form uploads.

#### Paging Semantics. 

Any API call that returns a list of objects supports the following query parameters:

* `num_to_skip`: the number of entities to be skipped (i.e., fast-forward). If not provided, the default is zero.
* `num_to_return`: the maximum number of entities to return. If not provided, the default is 100. The maximum allowed value is 100.

For returned data, user-generated data points are sorted by timestamp descending. All other data is sorted by name lexicographically.

Any API call that returns a list of user-generated data points supports the following query parameters.

* `start`: a timestamp that limits the results to only those data points with a timestamp in their metadata that is on or after this timestamp.
* `end`: a timestamp that limits the results to only those data points with a timestamp in their metadata that is on or before this timestamp.

Any API call that returns a list of non-user-generated data points supports a search function that uses the following parameter.

* `query`: a string that filters the resulting output by performing a match against the name and description of the entity. If the query value contains more than one word, e.g., "paper towel", each word is used as a match. In this example, all names and descriptions containing "paper" or "towel" will be returned. 


### Top-Level System Entities

* [Media](3.x Media APIs)
* [Ohmlets](3.x Ohmlet APIs)
* [People](3.x People APIs)
* [Schemas](3.x Schema APIs)
* [Streams](3.x Stream APIs)
* [Surveys](3.x Survey APIs)


