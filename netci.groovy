// Import the utility functionality.

import jobs.generation.Utilities;

def project = GithubProject
def branch = GithubBranchName

// Standard build jobs

[true, false].each { isPR ->
    ['A', 'B', 'C', 'D', 'E', 'F'].each { letter ->
        def newJob = job(Utilities.getFullJobName(project, "innerloop_${letter}", isPR)) {
            steps {
                batchFile("build.cmd")
            }
        }
        
        Utilities.standardJobSetup(newJob, project, isPR, "*/${branch}")
        if (isPR) {
            Utilities.addGithubPRTriggerForBranch(newJob, branch, "Say Hello${letter}")
        }
        else {
            Utilities.addGithubPushTrigger(newJob)
        }
    }
}