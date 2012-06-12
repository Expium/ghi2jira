; Configuration setting for GHI -> JIRA CSV Export

; Github login:
(def auth "EMAIL:PASS")

; Project owner and name:
(def ghuser "github-user-goes-here")
(def ghproject "project-name-here")

; Maximum number of comments per issue
(def MAXCMT 25)

; Translate user names from 
(def user-map
  { "GithubUserName1" "JIRAUserName1"
    "GithubUserName2" "JIRAUserName2"
   })
