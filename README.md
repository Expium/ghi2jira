# Export GitHub Issues to JIRA-compatible CSV format

This software communicates with Github's v3 API, to export
the "Issues" from a project in to a format JIRA can import.

The software is written in Clojure, and is primarily intended for
use by people comfortable running a Clojure program, but some users
have gotten good results without any Clojure knowledge.

## 2017 Status Update

Although much of this code was written several years ago and unchanged
since, updates provided by contributors have kept the software able to
run and produce good results with current 2017 versions of Jira and
Clojure. Users are encouraged to give it a try.

## Technical and version details

The source is set up for both leiningen and Eclipse, as I sometimes
use the latter. If you're simply running the software, Leiningen
is easy to set up as described below.

Note that comment import was broken in JIRA import 4.1.4, and worked
correctly starting with JIRA import 4.2.  It is since been updated
to work with versions as high as Jira 7.

For background information, see the announcement blog post:

http://kylecordes.com/2012/github-issues-export-jira

## Usage

I expect some users of this tool to start without Clojure knowledge
(i.e. just want to export JIRA data, not learn Clojure), so her
are hopefully sufficient instructions for that.

### Java

Install Java.

### Clojure

You **don't** need to install Clojure - Leiningen will do that for you.

### Leiningen

Install Leiningen:

https://github.com/technomancy/leiningen/

### This software, ghi2jira

Download ghi2jira (this software), either with "git clone" or by downloading a ZIP from Github.

Navigate to its directory in your command prompt.

Use "lein deps" to download the dependencies. Leiningen will download
all of the various JAR files needed, including Clojure itself.

Copy config.clj to config-project_id.clj and update it with your settings. This is a Clojure file, but you should be able to follow the syntax enough to fill in your project specifics. Note that Github may require the project name in all lower case.

Run the ghijira code, using Leiningen: "lein run project_id". This will take a while, depending on the number of issues in your project, because the Github Issues API requires a separate requires for each Issue to download its comments.

Import the resulting JIRA-project_id.csv file into JIRA. To get this right, you need to use a JIRA import configuration file. This ghijira project includes an example. Of particular importance is the comment setting, required for comment import to work. To clarify, this JIRA import configuration file is *not* part of the Clojure code, rather it is something JIRA needs.

Information about such a file is linked in the blog post mentioned earlier.

## License

Copyright (C) 2012-2017 Kyle Cordes / Expium LLC

Distributed under the Eclipse Public License, the same as Clojure.
