; Configuration setting for GHI -> JIRA CSV Export

{
 ; Github login:
 :auth "EMAIL:PASS"
 
 ; Project owner and name:
 :ghuser "github-user-goes-here"
 :ghproject "project-name-here"

 ; Maximum number of comments per issue
 :maxcmt 25

 ; Translate user names from 
 :user-map
 { "GithubUserName1" "JIRAUserName1"
  "GithubUserName2" "JIRAUserName2"}
}
