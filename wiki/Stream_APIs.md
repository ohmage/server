Streams are definitions for passively collected data such as ohmage mobility, which captures sensor readings in order to determine a person's ambulatory mode.

#### API Calls

| | GET (read) | POST (update) | DELETE (delete)
| --- | --- | --- | ---| 
| `/streams` | Unauthenticated call. Get the latest version of each stream. Each list item will contain the version's name, description and version identifier. | Create a new stream. | Not allowed.
| `/streams/<id>` | Unauthenticated call. Get the versions of the stream. | Update the stream with a new version. | Not allowed.
| `/streams/<id>/<version>` | Unauthenticated call. Get the name, description and schema for the stream. | Not allowed. | Not allowed.
| `/streams/<id>/<version>/data` | Get the visible data for the stream. | Upload new stream data. | Not allowed.
| `/streams/<id>/<version>/data/<point_id>` | Get the point's data. | Change the privacy state of the point. (Future) | Delete the point.
