// Import the utility functionality.

import jobs.generation.*;

def project = GithubProject
def branch = GithubBranchName
def projectFolder = Utilities.getFolderName(project) + '/' + Utilities.getFolderName(branch)

[true,false].each { isPR ->
    ['A'].each { letter ->
        def newJob = job(Utilities.getFullJobName(project, "innerloop_${letter}", isPR)) {
            steps {
                batchFile("echo https://www.google.com > links1.txt")
                batchFile("echo https://www.github.com >> links1.txt")
                batchFile("echo https://www.microsoft.com >> links1.txt")
                
                batchFile("echo https://www.bing.com > links2.txt")
                batchFile("echo https://www.reddit.com >> links2.txt")
                batchFile("echo https://www.facebook.com >> links2.txt")
            }
        }
        
        // Emit summaries
        SummaryBuilder summaries = new SummaryBuilder()
        summaries.addLinksSummaryFromFile('Crash dumps from this run', 'links1.txt')
        summaries.addLinksSummaryFromFile('Other dumps from this run', 'links2.txt')
        summaries.addSummaryLinks('Static summary links', ['www.dotnet-ci.cloudapp.net', 'www.dotnet-ci.cloudapp.net/$BUILD_NUMBER'])
        summaries.emit(newJob)
        
        Utilities.setMachineAffinity(newJob, 'Windows_NT', 'latest-or-auto')
        Utilities.standardJobSetup(newJob, project, isPR, "*/${branch}")
        if (isPR) {
            TriggerBuilder builder = TriggerBuilder.triggerOnPullRequest()
            builder.setGithubContext("Say Hello (dotnet-ci2) ${letter}")
            builder.triggerByDefault()
            builder.triggerForBranch(branch)
            builder.emitTrigger(newJob)
        }
        else {
            Utilities.addGithubPushTrigger(newJob)
        }
    }
}

// Make the call to generate the help job
Utilities.createHelperJob(this, project, branch, 'Welcome to the CITest Repository')