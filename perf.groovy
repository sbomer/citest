// Import the utility functionality.
import jobs.generation.*;

def project = GithubProject
def branch = GithubBranchName
def gitUrl = Utilities.calculateGitURL(project)

// Define the nightly workflows that measure the stability of performance machines
// Put these in a folder

def stabilityTestingFolderName = 'stability_testing'
folder('stability_testing') {}

def nativeStabilityJob = workflowJob(Utilities.getFullJobName('native_stability_test', false, stabilityTestingFolderName)) {
    // Add a parameter to specify which nodes to run the stabiliyt job across
    parameters {
        labelParam('NODE_LABEL') {
            allNodes('allCases', 'IgnoreOfflineNodeEligibility')
            description('Nodes to run the stability job across')
        }
    }
    definition {
        cps {
            script("""
                node {
                    echo 'Hello world'
                }
            """
            )
            sandbox()
        }
    }
}