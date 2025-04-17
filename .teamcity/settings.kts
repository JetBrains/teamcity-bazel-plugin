

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.version

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

project {
    vcsRoot(PullRequestVcs)
    vcsRoot(MasterVcs)

    buildType(PullRequestBuildConfiguration)
    buildType(MasterBuildConfiguration)
}
object PullRequestVcs : GitVcsRoot({
    id("TeamCityBazelPlugin_PullRequestVcs")
    name = "PullRequestVcs"
    url = "https://github.com/JetBrains/teamcity-bazel-plugin.git"
    branchSpec = """
        -:<default>
    """.trimIndent()
})

object PullRequestBuildConfiguration : BuildType({
    id("TeamCityBazelPlugin_PullRequestBuild")
    name = "PullRequestBuild"

    val githubTokenParameter = "GITHUB_TOKEN"
    params {
        password(githubTokenParameter, "credentialsJSON:12772a49-d676-43ec-a107-fad8a57cd852", readOnly = true)
    }

    vcs {
        root(PullRequestVcs)
    }

    features {
        pullRequests {
            vcsRootExtId = PullRequestVcs.id?.value
            provider = github {
                filterTargetBranch = "refs/heads/master"
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER_OR_COLLABORATOR
                authType = token {
                    token = "%$githubTokenParameter%"
                }
            }
        }
        commitStatusPublisher {
            vcsRootExtId = PullRequestVcs.id?.value
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "%$githubTokenParameter%"
                }
            }
        }
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    steps {
        gradle {
            name = "build"
            tasks = "clean build integration serverPlugin"
            // TODO prepare our own image or install bazilisk with TC recipe
            dockerImage = "registry.jetbrains.team/p/bazel/docker/hirschgarten-e2e:latest"
        }
    }
})

object MasterVcs : GitVcsRoot({
    id("TeamCityBazelPlugin_MasterVcs")
    name = "MasterVcs"
    url = "https://github.com/JetBrains/teamcity-bazel-plugin.git"
    branch = "master"
})

object MasterBuildConfiguration : BuildType({
    id("TeamCityBazelPlugin_MasterBuild")
    name = "MasterBuild"

    allowExternalStatus = true

    vcs {
        root(MasterVcs)
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    steps {
        gradle {
            name = "build"
            tasks = "clean build integration serverPlugin"
            // TODO prepare our own image or install bazilisk with TC recipe
            dockerImage = "registry.jetbrains.team/p/bazel/docker/hirschgarten-e2e:latest"
        }
    }
})