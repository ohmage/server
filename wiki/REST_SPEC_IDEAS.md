# Ohmage REST spec

This document is an early attempt to propose a spec for transitioning to a [REST](http://en.wikipedia.org/wiki/Representational_state_transfer) api in Ohmage 3.0. The tables below show how the current API calls would map to a REST API. If not specified otherwise, parameters and functionality are unchanged.

Please anyone feel free to add changes, comments, discussions, etc.

## REST principles

Lots has been written about REST. Some principles that are relevant to our project.

### HTTP Methods

* **GET** is a _safe_ method, which means it should be used for calls not alter server state, typically _reading_ operations. The server should set an appropriate value for responses to GET request for the  `cache-control` response header, that specifies if this call can be cached, by who (proxy/browser), and for how long. Note that requests done by authenticated users should be cached in the browser, but not by any proxies or caching servers. Also note that for GET requests, it is sufficient to provide the `auth_token` in a cookie, as there is no risk for CSRF.

* **POST** is idempotent and is used for server calls that add resources to an existing list of resources.

* **PUT** is also idempotent. It is used for update operations, where the parameters are similar to POST's parameters except that the unique identifier is the URL and the other parameters will modify the existing values. 

* **DELETE** is idempotent as well. It is used to remove a resource. Note that the idempotence implies that when removal of a resource is implemented with HTTP DELETE, the server should not throw an error if the resource is already removed.

It distinction between POST and PUT is not always obvious. This [SO topic](http://stackoverflow.com/questions/630453/put-vs-post-in-rest) is quite helpful. In practice it often depends on your endpoint. If the primary key is decided by the client and part of the endpoint, you would use PUT:

    PUT /ohmage/images/cf0ca2f0-699d-11e2-bcfd-0800200c9a66
    200 OK

    GET /ohmage/images/cf0ca2f0-699d-11e2-bcfd-0800200c9a66

If the primary key is determined by the server, you use POST:

    POST /ohmage/images
    200 OK cf0ca2f0-699d-11e2-bcfd-0800200c9a66

    GET /ohmage/images/cf0ca2f0-699d-11e2-bcfd-0800200c9a66

* **PATCH** was invented in the wake of all of the confusion surrounding POST and PUT, but I am not sure how pervasive it is. It was designed as a more explicit "update", thereby allowing developer to decide on either POST or PUT as the "create" and ignoring the other.

### HTTP Endpoints

The endpoint is the HTTP path that is included in the request. This defines the target resource. Perhaps the most important guideline is that if the set of resources is identified by some sort of **primary key**, this should be in the endpoint and _not a parameter_. E.g:

    DELETE /ohmage/users/jeroen
    GET /ohmage/images/cf0ca2f0-699d-11e2-bcfd-0800200c9a66
    PUT /ohmage/campaigns/lausd_campaign
    GET /ohmage/responses/lausd_campaign/snack_survey?start_date="2012-01-01" 

### HTTP Status codes

HTTP defines many status codes with abstract defined meanings. An important part of implementing REST is discussing how these map to our API. Some obvious examples: 

  * `HTTP 200 OK` Means success and nothing else. The client can assume the request was successful, and returned what was requested.
  * `HTTP 400 Bad Request`. Use when the client is requesting something that does not make sense. E.g. invalid endpoint, missing parameters.
  * `HTTP 401 Permission Denied`. Use when user's authentication token is invalid. If it is missing, a HTTP 400 will be returned.
  * `HTTP 550 Permission Denied`. Use when user is authenticated, but request was blocked by ACL. See John's note below.

Perhaps we should avoid `HTTP 503` and `HTTP 404`, because these can occur when something is wrong with Tomcat?

**John** - I would use HTTP codes even if Tomcat uses them. I have been tossing around an idea for a while now; we should return a 404 for all resources that the user cannot view, even if they exist but the ACL blocks them. The way we conceptualize the content of groups of resources, e.g. /campaigns, is based on visibility, not access; in other words, /campaigns lists all of the campaigns the user can see. It does list not all of the campaigns that exist in the system, where the definition of the "invisible" campaigns is removed or something like that. If the user asks for a specific resource, i.e. /campaigns/my_campaign_id, and it does exist but isn't visible to that user, we should return a 404 rather than 550.

#### HTTP Response Body

The response body can be anything, as long as it matches what is stated in the `content-type` response header. See [here](http://webdesign.about.com/od/multimedia/a/mime-types-by-content-type.htm) for a list.

### Authentication, cookies

If we are still using the authentication token, the server will set this as the `auth_token` cookie using a `setCookie` response header. Safe methods (GET) only have to include this cookie as a request header, which happens automatically in all browsers. Unsafe methods (POST, PUT, DELETE) must add the token as a POST parameter, to prevent XSRF.

## Ohmage Server API Spec

Below some proposals on how the current operations would map to equivalent operations under the REST spec. Feel free to modify, discuss.

**John** - The way I would like to model the "important" objects in our system (not images, videos, documents, prompts, etc.) is like this. The "base" objects are `user`, `survey`, and `stream`. The "composite" objects are `group`, `campaign`, and `observer`.

Anyone can create any of these objects, but certain objects can only be created in their composite form. `campaign`s can be used to create `survey`s, but `survey`s cannot be created by themselves. A `campaign` can reference a `survey` from another campaign. `observer`s work in the same way, except on `stream`s.

A `class` is not really a `group` due to some requirements by Mobilize, where this term was first coined. The big difference is that a `class` contains a group of `user`s as well as a group of `campaign`s and, potentially in the future, a group of `observer`s. I think it's best to think of a `class` as a composite of composites and, maybe, individual `user`s.

### User Manipulation

**User**

<table>
  <tr>
    <th>Operation</th>
    <th>2.xx API</th>
    <th>3.xx API</th>
    <th>Notes</th>
  </tr>
  <tr>
    <td>Create new user</td>
    <td>/user/create?username=jeroen</td>
    <td>POST /users/jeroen</td>
    <td></td>
  </tr>
  <tr>
    <td>List visible users</td>
    <td>/user/read</td>
    <td>GET /users</td>
    <td></td>
  </tr>
  <tr>
    <td>Get a user's  info</td>
    <td>/user_info/read<br />/user_stats/read?username=jeroen</td>
    <td>GET /users/jeroen (info about user) <br />GET /campaigns (campaigns available to current user) <br />GET /classes (classes available to current user)</td>
    <td></td>
  </tr>
  <tr>
    <td>Modify user</td>
    <td>/user/update?username=jeroen</td>
    <td>PUT /users/jeroen</td>
    <td></td>
  </tr>
  <tr>
    <td>Modify password</td>
    <td>/user/change_password?user=jeroen</td>
    <td>PUT /users/jeroen</td>
    <td></td>
  </tr>
  <tr>
    <td>Remove user</td>
    <td>/user/delete?user=jeroen</td>
    <td>DELETE /users/jeroen</td>
    <td></td>
  </tr>
</table>

**Group**

TODO

### Campaign Manipulation

**Campaign**

One thing to consider is to move the naming of the campaign_urn out of the XML and into the API. This seems more sensible, and would allow us to deploy a single campaign XML several times without having to modify the XML. In that case we would use HTTP PUT to create campaigns. If we stick with a urn as defined in the XML file HTTP POST seems more appropriate.

<table>
  <tr>
    <th>Operation</th>
    <th>2.xx API</th>
    <th>3.xx API</th>
    <th>Notes</th>
  </tr>
  <tr>
    <td>Create new campaign</td>
    <td>/campaigns/create</td>
    <td>POST /campaigns</td>
    <td></td>
  </tr>
  <tr>
    <td>List campaigns</td>
    <td>/user_info/read <br /> /campaign/search</td>
    <td>GET /campaigns</td>
    <td></td>
  </tr>
  <tr>
    <td>Read information about a specific campaign</td>
    <td>/campaign/read?campaign_urn_list=my_campaign_urn</td>
    <td>GET /campaigns/my_campaign_urn</td>
    <td></td>
  </tr>
  <tr>
    <td>Modify campaign</td>
    <td>/campaign/update?campaign_urn=my_campaign_urn</td>
    <td>PUT /campaigns/my_campaign_urn</td>
    <td></td>
  <tr />
</table>

**Survey**

TODO

**Prompt**

TODO?

**Survey Response**

TODO

**Prompt Response**

TODO

### Observer manipulation

**Observer**

TODO

**Stream**

TODO

**Data**

TODO

### Class manipulation

~~Perhaps we should consider renaming `class` to `group`. Class is a bit of a misnomer, because it implies some sort of inheritance and makes little sense outside the classroom context. Usually, sets of users are called `groups`.~~

See the comment at the head of this section.

<table>
  <tr>
    <th>Operation</th>
    <th>2.xx API</th>
    <th>3.xx API</th>
    <th>Notes</th>
  </tr>
  <tr>
    <td>Create Class</td>
    <td>/class/create?class_urn=myclass</td>
    <td>POST /classes<\td>
    <td></td>
  </tr>
  <tr>
    <td>List Classes</td>
    <td>/user_info/read <br />/class/search</td>
    <td>GET /classes</td>
    <td></td>
  </tr>
  <tr>
    <td>Information about a class</td>
    <td>/class/read?class_urn=myclass</td>
    <td>GET /classes/myclass<\td>
    <td></td>
  </tr>
  <tr>
    <td>Users in class</td>
    <td>/class/read?class_urn=myclass<br />/class/roster/read?class_urn=myclass</td>
    <td>GET /classes/myclass<\td>
    <td></td>
  </tr>
  <tr>
    <td>Get class roster in CSV</td>
    <td>/class/roster/read?class_urn=myclass</td>
    <td>GET /classes/myclass<\td>
    <td>Request header: Accept: text/csv<br />A convention some people use is to append ".csv" to the path, e.g. "/classes/myclass.csv".</td>
  </tr>
  <tr>
    <td>Modify Class</td>
    <td>/class/update?class_urn=myclass</td>
    <td>PUT /classes/myclass</td>
    <td></td>
  </tr>
  <tr>
    <td>Roster CSV update</td>
    <td>/class/roster/update?class_urn=myclass</td>
    <td>PUT /classes/myclass</td>
    <td>Request header: Content-Type: text/csv</td>
  </tr>
  <tr>
    <td>Remove class</td>
    <td>/classes/delete?class_urn=myclass</td>
    <td>DELETE /classes/myclass</td>
    <td></td>
  </tr>
</table>

### Content Manipulation

I think we should have a new approach for static content. Basically, we allow people to upload this and get an ID for it. Or maybe they just get the ID right away and then upload the content later. Either way, the survey responses aren't uploaded until all of those IDs are found. It decouples the survey responses and their heavier content.

**Images**

TODO

**Videos**

TODO

**Documents**

TODO