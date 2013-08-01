# Welcome

ohmage is a mobile data collection system for collecting data given explicitly
by a user (active data) and data that is collected by backgrounded applications
(passive data). This repository houses the server-side application. The Android
application can be found at [https://github.com/ohmage/ohmageAndroidLib](here).

A description of the high-level entities can be found at 
[https://github.com/ohmage/ohmageServer/wiki/About-Users,-Classes-and-Campaigns](here),
and an introduction into how to read and write from an up-and-running system
can be found at [https://github.com/cens/ohmageServer/wiki/About-the-Client-Server-Protocol-and-System-Entities](here).

This server is an Open mHealth DSU reference implementation for the 0.1 version
of the 
[https://github.com/openmhealth/developer/wiki/DSU-API-0.1](specification). The specification
has evolved but the implementation has not kept completely up-to-date. More
information can be found at the Open mHealth [https://github.com/openmhealth/developer/wiki](developer wiki).

# Compiling

There are two primary ant build targets, `dist` and `dist-no_ssl`, which should
be used based on whether or not a SSL should be required respectively. The
resulting

# Collaboration

The source is currently undergoing a major overhaul, so, unless a patch is
already in the works, it might be best to wait until the 3.0 version is
released.

The coding rules are loose, and the best reference would be other parts of the
code. A few rules we do have are:
- 4 space indents (no tabs).
- Always use curly braces even if the conditional or loop is one line.
- Opening curly braces go on the same line of the loop or conditional
declaration.
- All comments must be no more than 79 characters (with the 80th character
being a new line). Code should try to adhere to this as best as possible.
