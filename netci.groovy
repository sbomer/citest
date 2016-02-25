// Import the utility functionality.

import jobs.generation.Utilities;

def project = GithubProject
def branch = GithubBranchName

// Standard build jobs

[true, false].each { isPR ->
    def newJob = job(Utilities.getFullJobName(project, 'innerloop', isPR)) {
        steps {
            batchFile("build.cmd")
        }
    }
    
    Utilities.setMachineAffinity(newJob, 'Windows_NT', 'latest-or-auto')
    Utilities.standardJobSetup(newJob, project, isPR, "*/${branch}")
    if (isPR) {
        Utilities.addGithubPRTriggerForBranch(newJob, branch, "Say Hello")
    }
    else {
        Utilities.addGithubPushTrigger(newJob)
    }
}