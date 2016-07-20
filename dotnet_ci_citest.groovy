// Import the utility functionality.

import jobs.generation.*;

def project = GithubProject
def branch = GithubBranchName

[true, false].each { isPR ->
    ['A', 'B', 'C', 'D', 'E', 'F'].each { letter ->
        def newJob = job(Utilities.getFullJobName(project, "innerloop_${letter}", isPR)) {
            steps {
                batchFile("build.cmd")
            }
        }
        
        Utilities.setMachineAffinity(newJob, 'Windows_NT', 'latest-or-auto')
        Utilities.standardJobSetup(newJob, project, isPR, "*/${branch}")
        if (isPR) {
            TriggerBuilder builder = new TriggerBuilder.triggerOnPullRequest()
            builder.setGithubContext("Say Hello${letter}")
            builder.triggerByDefault()
            builder.triggerForBranch('master')
        }
        else {
            Utilities.addGithubPushTrigger(newJob)
        }
    }
}