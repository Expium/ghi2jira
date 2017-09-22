; Configuration setting for GHI -> JIRA CSV Export

{
 ; Github login:
 :auth "EMAIL:PASS"

 ; Project owner and name:
 :ghuser "github-user-goes-here"

 ; Github may requires this in all lower case, even if your project name is mixed case.
 :ghproject "project-name-here"

 ; Maximum number of comments per issue
 :maxcmt 25

 ; JIRA project setting - will be used to convert #123 to ABC-123
 ; so that source control integration still works.
 :jira-project "ABC"

 ; The issue offset is used when you need to import to an existing JIRA project,
 ; it is added to the Github issue number. Use 0 if you are starting with an
 ; empty JIRA project.
 :issue-offset 0

 ; Git base URL - used to translate GHI "referenced" events to
 ; comments with a URL pointing to the link.
 :git-base-url "https://bitbucket.org/somenamehere/someprojectthere/changeset/"

 ; Translate user names from
 :user-map
 { "GithubUserName1" "JIRAUserName1"
  "GithubUserName2" "JIRAUserName2"}

 ; Ignore mentions of these names (to avoid spurious comments that cannot be attributed).
 ; An example of this is when there are JSON snippets using @-attributes.
 :ignore-mentions #{"timestamp" "host" "Version"}
}
