

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.kotlinFile
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
    vcsRoot(TagReleaseVcs)
    vcsRoot(PullRequestVcs)
    vcsRoot(MasterVcs)

    buildType(ReleaseBuildConfiguration)
    buildType(PullRequestBuildConfiguration)
    buildType(MasterBuildConfiguration)
}

object TagReleaseVcs : GitVcsRoot({
    id("TeamCityBazelPlugin_TagReleaseVcs")
    name = "TagReleaseVcs"
    url = "https://github.com/JetBrains/teamcity-bazel-plugin.git"
    branch = "master"
    useTagsAsBranches = true
    branchSpec = """
        +:refs/(tags/*)
        -:<default>
    """.trimIndent()
})

object ReleaseBuildConfiguration : BuildType({
    id("TeamCityBazelPlugin_ReleaseBuild")
    name = "ReleaseBuild"

    params {
        password("env.ORG_GRADLE_PROJECT_jetbrains.marketplace.token", "credentialsJSON:136993d7-7e35-4870-8f5c-6ec9d6564ad4", readOnly = true)
    }

    vcs {
        root(TagReleaseVcs)
    }

    steps {
        kotlinFile {
            name = "calculate version"
            path = "./.teamcity/steps/calculateVersion.kts"
            arguments = "%teamcity.build.branch%"
        }
        gradle {
            name = "build"
            tasks = "clean build serverPlugin"
        }
        gradle {
            name = "publish to marketplace"
            tasks = "publishPlugin"
        }
    }

    artifactRules = "+:./plugin-bazel-server/build/distributions/teamcity-bazel-plugin.zip"


    triggers {
        vcs {
            branchFilter = "+:tags/v*"
        }
    }
})

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
            tasks = "clean build test serverPlugin"
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
            tasks = "clean build test serverPlugin"
        }
    }
})