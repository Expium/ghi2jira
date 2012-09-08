# Export GitHub Issues to JIRA-compatible CSV format

This software communicates with Github's v3 API, to export
the "Issues" from a project in to a format JIRA can import.

The source is set up for both leiningen and Eclipse, as I sometimes
use the latter.

Note that comment import is broken in JIRA import 4.1.4, and works
correctly starting with JIRA import 4.2.  As of June 2012, unfortunately
the version deployed in JIRA OnDemand is 4.1.4.

For background information, see the announcement blog post:

http://kylecordes.com/2012/github-issues-export-jira

## Usage

I expect some users of this tool to start without Clojure knowledge (i.e. just want to export JIRA data, not learn Clojure), so here are hopefully sufficient instructions for that.

Install Java.

You don't need to install Clojure - Leiningen will do that for you.

Install Leiningen:

https://github.com/technomancy/leiningen/

Either the new 2.x version, or old 1.x version, should work. (Typically you just download the lein script, and put it in your ~/bin directory.)

Download ghijira (this software), either with "git clone" or by downloading a ZIP from Github.

Navigate to its directory in your command prompt.

Use "lein deps" to download the dependencies. Leiningen will download all of the various JAR files needed, including Clojure itself.

Copy config.clj to config-project_id.clj and update it with your settings. This is a Clojure file, but you should be able to follow the syntax enough to fill in your project specifics.

Run the ghijira code, using Leiningen: "lein run project_id". This will take a while, depending on the number of issues in your project, because the Github Issues API requires a separate requires for each Issue to download its comments.

Import the resulting JIRA-project_id.csv file into JIRA. To get this right, you need to use a JIRA import configuration file. This ghijira project includes an example. Of particular importance is the comment setting, required for comment import to work. To clarify, this JIRA import configuration file is *not* part of the Clojure code, rather it is something JIRA needs.

Information about such a file is linked in the blog post mentioned earlier.

## License

Copyright (C) 2012 Kyle Cordes

Distributed under the Eclipse Public License, the same as Clojure.
