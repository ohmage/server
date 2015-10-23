Both [surveys](https://github.com/ohmage/server/wiki/3.x-Survey-APIs) and [streams](https://github.com/ohmage/server/wiki/3.x-Stream-APIs) are schemas. 

#### API Calls

| | GET (read) | POST (update) | DELETE (delete)
| --- | --- | --- | --- |
| `/schemas` | Get the IDs of visible schemas. | Not allowed. | Not allowed.
| `/schemas/<id>` | Get the versions of the schema. | Not allowed. | Not allowed.
| `/schemas/<id>/<version>` | Get the definition of the schema. | Not allowed. | Not allowed.
| `/schemas/<id>/<version>/data` | Get the visible data for the schema. | Not allowed. | Not allowed.
| `/schemas/<id>/<version>/data/<point_id>` | Get the point's data. | Change the privacy state of the point. | Delete the point.
