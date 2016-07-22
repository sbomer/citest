// Import the utility functionality.
import jobs.generation.*;

def project = GithubProject
def branch = GithubBranchName
def gitUrl = Utilities.calculateGitURL(project)

// Define the nightly workflows that measure the stability of performance machines
// Put these in a folder

def stabilityTestingFolderName = 'stability_testing'
folder('stability_testing') {}

// Defines stability testing for all linux OS's.  It might be possible to unify this into all unixes.
// Can't use pipeline here.  The postbuild step that is used to launch the subsequent builds doesn't work
// with the pipeline project type.  It might be possible to do this other ways.  For instance, evaluating all
// all eligible nodes based on an expression in a pipeline job and launching downstream jobs.

// We run two types:
// 1) Native testing of known binaries (a few parsec benchmarks) that are very stable.
// 2) Managed testing of known binaries that are stable

['unix', 'windows'].each { osFamily ->
    def nativeStabilityJob = job(Utilities.getFullJobName("${osFamily}_native_stability_test", false, stabilityTestingFolderName)) {
        // Add a parameter to specify which nodes to run the stability job across
        parameters {
            labelParam('NODES_LABEL') {
                allNodes('allCases', 'AllNodeEligibility')
                defaultValue("${osFamily} && performance")
                description('Nodes label expression to run the unix stability job across')
            }
        }
        steps {
            if (osFamily == 'windows') {
                batchFile("stability\\windows_native-stability-test.py")
            }
            else {
                shell("stability/linux_native-stability-test.py")
            }
        }
    }

    // Standard job setup, etc.
    Utilities.standardJobSetup(nativeStabilityJob, project, false, "*/${branch}")

    // Create the workspace cleaner
    def nativeMachineOfflineJob = job('workspace_cleaner') {
        publishers {
            postBuildScripts {
                logRotator {
                    daysToKeep(7)
                }

                logRotator {
                    daysToKeep(7)
                }

                scm {
                    git {
                        remote {
                            github('dotnet/dotnet-ci')
                        }
                        branch("*/master")
                    }
                }

                triggers {
                    cron('0 0 * * *')
                }

                steps {
                    systemGroovyScriptFile('jobs/scripts/workspace_cleaner.groovy')
                }

                onlyIfBuildSucceeds(false)
                onlyIfBuildFails()
            }
        }
    }
    
    // Standard job setup, etc.
    Utilities.standardJobSetup(nativeMachineOfflineJob, project, false, "*/${branch}")

    // Set the cron job here.  We run nightly on each flavor, regardless of code changes
    Utilities.addPeriodicTrigger(nativeStabilityJob, "@daily", true /*always run*/)
    
    // Managed stability testing

    def managedStabilityJob = job(Utilities.getFullJobName("${osFamily}_managed_stability_test", false, stabilityTestingFolderName)) {
        // Add a parameter to specify which nodes to run the stability job across
        parameters {
            labelParam('NODES_LABEL') {
                allNodes('allCases', 'AllNodeEligibility')
                defaultValue("${osFamily} && performance")
                description('Nodes label expression to run the unix stability job across')
            }
        }
        steps {
            if (osFamily == 'windows') {
                batchFile("stability\\windows-managed-stability-test.cmd")
            }
            else {
                shell("stability/unix-managed-stability-test.sh")
            }
        }
    }

    // Standard job setup, etc.
    Utilities.standardJobSetup(managedStabilityJob, project, false, "*/${branch}")

    // Create the workspace cleaner
    def managedMachineOfflineJob = job('workspace_cleaner') {
        publishers {
            postBuildScripts {
                logRotator {
                    daysToKeep(7)
                }

                logRotator {
                    daysToKeep(7)
                }

                scm {
                    git {
                        remote {
                            github('dotnet/dotnet-ci')
                        }
                        branch("*/master")
                    }
                }

                triggers {
                    cron('0 0 * * *')
                }

                steps {
                    systemGroovyScriptFile('jobs/scripts/workspace_cleaner.groovy')
                }

                onlyIfBuildSucceeds(false)
                onlyIfBuildFails()
            }
        }
    }
    
    // Standard job setup, etc.
    Utilities.standardJobSetup(managedMachineOfflineJob, project, false, "*/${branch}")
}

// Create a perf job for roslyn testing

def roslynPerfJob = pipelineJob('rosly_perf_proto') {
    definition {
        cpsScm {
            scm {
                // Read the script from source control at execution time
                git(gitUrl)
                // Load it from the appropriate location
                scriptPath('roslyn/roslyn-perf-proto.groovy')
            }
        }
    }
}

// Standard job setup, etc.
Utilities.standardJobSetup(roslynPerfJob, project, false, "*/${branch}")